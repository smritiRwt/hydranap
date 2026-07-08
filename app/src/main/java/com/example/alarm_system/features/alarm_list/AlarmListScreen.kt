package com.example.alarm_system.features.alarm_list

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.core.common.PermissionUtils
import com.example.alarm_system.core.common.toAbbreviatedDays
import com.example.alarm_system.ui.components.GlassBackground
import com.example.alarm_system.ui.components.GlassCard
import com.example.alarm_system.ui.components.GlassFloatingActionButton
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmListViewModel,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Int) -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                if (PermissionUtils.canScheduleExactAlarms(context)) {
                    onAddAlarm()
                } else {
                    PermissionUtils.openExactAlarmSettings(context)
                }
            } else {
                // Handle permission denial - maybe show a snackbar or dialog
            }
        }
    )

    var showPermissionRationale by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionUtils.hasNotificationPermission(context)) {
                showPermissionRationale = true
            }
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Notification Permission Required", color = Color.White) },
            text = { 
                Text(
                    "This app needs notification permission to alert you when your alarms go off. Please allow notifications.",
                    color = Color.White.copy(alpha = 0.7f)
                ) 
            },
            confirmButton = {
                Button(onClick = {
                    showPermissionRationale = false
                    PermissionUtils.openNotificationSettings(context)
                }) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPermissionRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text("Allow", color = Color.White.copy(alpha = 0.5f))
                }
            },
            containerColor = Color(0xFF1A1A2E)
        )
    }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                GlassFloatingActionButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (PermissionUtils.hasNotificationPermission(context)) {
                                if (PermissionUtils.canScheduleExactAlarms(context)) {
                                    onAddAlarm()
                                } else {
                                    PermissionUtils.openExactAlarmSettings(context)
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            if (PermissionUtils.canScheduleExactAlarms(context)) {
                                onAddAlarm()
                            } else {
                                PermissionUtils.openExactAlarmSettings(context)
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 100.dp) // Leave space for GlassPillNav
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    GreetingHeader()
                }

                item {
                    NextAlarmInfo(alarms)
                }

                items(alarms, key = { it.id }) { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        onToggle = { viewModel.toggleAlarm(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onClick = { onEditAlarm(alarm.id) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
fun GreetingHeader() {
    val now = LocalDateTime.now()
    val greeting = when (now.hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.sp
        )
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compact centered digital clock
        Text(
            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 48.sp,
            letterSpacing = (-1).sp
        )
    }
}

@Composable
fun NextAlarmInfo(alarms: List<Alarm>) {
    val nextAlarm = alarms.filter { it.isEnabled }.minByOrNull { it.nextTriggerTime }
    
    if (nextAlarm != null) {
        val remainingMillis = nextAlarm.nextTriggerTime - System.currentTimeMillis()
        val hours = remainingMillis / (1000 * 60 * 60)
        val minutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)
        
        val remainingText = when {
            hours > 0 -> "in ${hours}h ${minutes}m"
            minutes > 0 -> "in ${minutes}m"
            else -> "now"
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next Alarm",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nextAlarm.title.ifBlank { "Alarm" },
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = remainingText,
                color = com.example.alarm_system.ui.theme.AccentBlue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 16.dp,
        useShimmer = alarm.isEnabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = alarm.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (alarm.isEnabled) Color.White else Color.White.copy(alpha = 0.4f),
                letterSpacing = (-1).sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (alarm.title.isNotBlank()) alarm.title else "Alarm",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = alarm.repeatDays.toAbbreviatedDays(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = com.example.alarm_system.ui.theme.Primary,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}
