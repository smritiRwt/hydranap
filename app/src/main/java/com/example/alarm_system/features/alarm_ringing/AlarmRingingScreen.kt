package com.example.alarm_system.features.alarm_ringing

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.ui.components.GlassBackground
import com.example.alarm_system.ui.theme.Error
import com.example.alarm_system.ui.theme.Primary
import com.example.alarm_system.ui.theme.Warning
import java.time.format.DateTimeFormatter

@Composable
fun AlarmRingingScreen(
    alarm: Alarm?,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val blurVal by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blur"
    )

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Immersive Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    // Animated Pulse Wave
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(scale)
                            .blur(blurVal.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape)
                    )
                    
                    if (alarm != null) {
                        Text(
                            text = alarm.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                            color = Color.White,
                            letterSpacing = (-2).sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (alarm != null) {
                    Text(
                        text = "💧 ${alarm.title.ifBlank { "Drink Water" }}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Time to stay hydrated and energized.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
                }
            }

            // High-end Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Stop Button (Glassy Red)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Error.copy(alpha = 0.8f),
                                        Color(0xFFF87171).copy(alpha = 0.6f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stop",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                // Snooze Button (Premium Glass)
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Snooze 10 min",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
