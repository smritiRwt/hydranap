package com.example.alarm_system.features.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm_system.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToHome: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundStart,
                        BackgroundEnd
                    )
                )
            )
    ) {
        // Decorative background highlights for a "high-end" feel
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-80).dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(GoldPrimary.copy(alpha = 0.12f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .size(350.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(GoldDark.copy(alpha = 0.1f), Color.Transparent)))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> IntroPage()
                    1 -> WaterOnboarding(
                        interval = viewModel.waterIntervalMinutes,
                        onIntervalChange = { viewModel.waterIntervalMinutes = it }
                    )
                    2 -> SleepOnboarding(
                        startTime = viewModel.sleepStartTime,
                        endTime = viewModel.sleepEndTime,
                        onStartTimeChange = { viewModel.sleepStartTime = it },
                        onEndTimeChange = { viewModel.sleepEndTime = it }
                    )
                }
            }

            // Bottom Navigation Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp)
                    .navigationBarsPadding()
            ) {
                // Centered Indicator
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val active = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (active) 28.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) GoldPrimary
                                    else Color.White.copy(alpha = 0.15f)
                                )
                                .animateContentSize()
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.completeOnboarding(onNavigateToHome)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = GoldPrimary),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = MidnightMain
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 18.dp)
                ) {
                    Text(
                        if (pagerState.currentPage == 2) "GET STARTED" else "CONTINUE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
        
        if (pagerState.currentPage > 0) {
            IconButton(
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack, 
                    contentDescription = "Back", 
                    tint = GoldPrimary
                )
            }
        }
    }
}

@Composable
fun IntroPage() {
    OnboardingPageLayout(
        title = "Elegance in Routine",
        subtitle = "A sophisticated way to manage your wellness. Let's tailor your experience.",
        icon = Icons.Rounded.AutoAwesome,
        iconColor = GoldPrimary
    )
}

@Composable
fun WaterOnboarding(
    interval: Int,
    onIntervalChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(180.dp),
                shape = CircleShape,
                color = AccentBlue.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(AccentBlue, Color.Transparent)))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(90.dp),
                        tint = AccentBlue
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Vital Hydration",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 34.sp,
                letterSpacing = (-0.5).sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Precision reminders to keep you at your peak. Define your frequency.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$interval",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = AccentBlue,
                        fontSize = 72.sp
                    )
                )
                Text(
                    "MINUTES INTERVAL",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
                    color = Color.White.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(40.dp))
                Slider(
                    value = interval.toFloat(),
                    onValueChange = { onIntervalChange(it.toInt()) },
                    valueRange = 15f..120f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
fun SleepOnboarding(
    startTime: LocalTime,
    endTime: LocalTime,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(180.dp),
                shape = CircleShape,
                color = GoldPrimary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(GoldPrimary, Color.Transparent)))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = null,
                        modifier = Modifier.size(90.dp),
                        tint = GoldPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Circadian Rhythm",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 34.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Align your life with your body's natural clock for optimal performance.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))

        TimeActionCard(
            label = "Bedtime",
            time = startTime.format(formatter),
            icon = Icons.Rounded.NightsStay,
            accentColor = GoldPrimary,
            onClick = { showStartTimePicker = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TimeActionCard(
            label = "Rise",
            time = endTime.format(formatter),
            icon = Icons.Rounded.WbSunny,
            accentColor = AccentPink,
            onClick = { showEndTimePicker = true }
        )
    }

    if (showStartTimePicker) {
        TimePickerModal(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = { 
                onStartTimeChange(it)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerModal(
            initialTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = { 
                onEndTimeChange(it)
                showEndTimePicker = false
            }
        )
    }
}

@Composable
fun OnboardingPageLayout(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(220.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Brush.sweepGradient(listOf(iconColor.copy(alpha = 0.5f), Color.Transparent, iconColor.copy(alpha = 0.5f))))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(110.dp),
                        tint = iconColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1.5).sp,
                fontSize = 44.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun TimeActionCard(
    label: String,
    time: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Text(
                time,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    val state = rememberTimePickerState(initialTime.hour, initialTime.minute)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onTimeSelected(LocalTime.of(state.hour, state.minute)) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = MidnightMain)
            ) {
                Text("CONFIRM", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.5f))
            ) { Text("CANCEL") }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        selectorColor = GoldPrimary,
                        periodSelectorSelectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                        periodSelectorSelectedContentColor = GoldPrimary,
                        timeSelectorSelectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = GoldPrimary,
                        clockDialColor = MidnightSlate,
                        clockDialSelectedContentColor = MidnightMain,
                        clockDialUnselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                )
            }
        },
        containerColor = MidnightMain,
        shape = RoundedCornerShape(28.dp)
    )
}
