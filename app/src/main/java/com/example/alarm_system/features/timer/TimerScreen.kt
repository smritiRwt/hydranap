package com.example.alarm_system.features.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alarm_system.ui.components.GlassBackground
import com.example.alarm_system.ui.theme.Primary
import java.util.Locale

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val remainingTime by viewModel.remainingTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()

    var showPicker by remember { mutableStateOf(true) }
    
    LaunchedEffect(totalTime) {
        showPicker = totalTime == 0L
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = "Timer",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(60.dp))

            if (showPicker) {
                TimerPicker(onTimerSet = { h, m, s ->
                    viewModel.setTimer(h, m, s)
                    showPicker = false
                })
            } else {
                Text(
                    text = formatRemainingTime(remainingTime),
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { 
                            viewModel.clear()
                            showPicker = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { viewModel.toggle() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color.Red.copy(alpha = 0.6f) else Primary)
                    ) {
                        Text(if (isRunning) "Pause" else "Start")
                    }
                }
            }
        }
    }
}

@Composable
fun TimerPicker(onTimerSet: (Int, Int, Int) -> Unit) {
    // Simplified picker for now
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(5) }
    var seconds by remember { mutableStateOf(0) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberColumn(value = hours, label = "h", onValueChange = { hours = it })
            Text(":", color = Color.White, style = MaterialTheme.typography.titleLarge)
            NumberColumn(value = minutes, label = "m", onValueChange = { minutes = it })
            Text(":", color = Color.White, style = MaterialTheme.typography.titleLarge)
            NumberColumn(value = seconds, label = "s", onValueChange = { seconds = it })
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = { onTimerSet(hours, minutes, seconds) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Set Timer")
        }
    }
}

@Composable
fun NumberColumn(value: Int, label: String, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Button(onClick = { if (value < 59) onValueChange(value + 1) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
            Text("+", color = Color.White)
        }
        Text(
            text = String.format(Locale.getDefault(), "%02d%s", value, label),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
        Button(onClick = { if (value > 0) onValueChange(value - 1) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
            Text("-", color = Color.White)
        }
    }
}

private fun formatRemainingTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
