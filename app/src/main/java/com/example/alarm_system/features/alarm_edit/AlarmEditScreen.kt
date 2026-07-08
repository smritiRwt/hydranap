package com.example.alarm_system.features.alarm_edit

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm_system.ui.components.GlassBackground
import com.example.alarm_system.ui.components.GlassCard
import com.example.alarm_system.ui.theme.Primary
import com.example.alarm_system.ui.theme.Secondary
import com.example.alarm_system.ui.theme.AccentBlue
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    viewModel: AlarmEditViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.updateSound(uri?.toString())
        }
    }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (state.id == 0) "NEW ALARM" else "EDIT ALARM",
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ultra-clean Time Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .clickable { showTimePicker = true }
                ) {
                    Text(
                        text = state.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontSize = 90.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-4).sp
                    )
                    Text(
                        text = "SET TIME",
                        color = Primary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                if (showTimePicker) {
                    val timePickerState = rememberTimePickerState(
                        initialHour = state.time.hour,
                        initialMinute = state.time.minute
                    )
                    
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.updateTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                                showTimePicker = false
                            }) { Text("OK", color = Primary) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) { Text("CANCEL", color = Color.White.copy(alpha = 0.6f)) }
                        },
                        containerColor = Color(0xFF1A1A2E),
                        text = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                TimePicker(
                                    state = timePickerState,
                                    colors = TimePickerDefaults.colors(
                                        clockDialColor = Color.White.copy(alpha = 0.05f),
                                        selectorColor = Primary,
                                        containerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    )
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = state.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = { Text("Alarm Name", color = Color.White.copy(alpha = 0.3f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Primary,
                            focusedIndicatorColor = Primary.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f)
                        ),
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                }

                // Luxury Repeat Selector
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "REPEAT", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val isSelected = state.repeatDays.contains(day)
                            val dayName = day.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                            
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Primary else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.toggleDay(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayName,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Sound and Vibration
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, state.soundUri?.let { Uri.parse(it) })
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                }
                                ringtoneLauncher.launch(intent)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Alarm Sound", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            val soundName = if (state.soundUri != null) {
                                try {
                                    RingtoneManager.getRingtone(context, Uri.parse(state.soundUri)).getTitle(context)
                                } catch (e: Exception) { "Default" }
                            } else {
                                "Default"
                            }
                            Text(soundName, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.White.copy(alpha = 0.05f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vibration", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("Haptic feedback", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.isVibrationEnabled,
                            onCheckedChange = { viewModel.updateVibration(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Primary
                            )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(
                        onClick = { viewModel.saveAlarm(onBack) },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Primary, Secondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
