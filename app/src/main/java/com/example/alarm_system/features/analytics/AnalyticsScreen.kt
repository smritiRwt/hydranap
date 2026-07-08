package com.example.alarm_system.features.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm_system.ui.components.GlassBackground
import com.example.alarm_system.ui.components.GlassCard

@Composable
fun AnalyticsScreen() {
    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Sleep Analytics",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sleep Quality",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "85%",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Average Sleep",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "7h 45m",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
