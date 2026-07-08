package com.example.alarm_system.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.alarm_system.ui.theme.*

@Composable
fun GlassBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundStart)
    ) {
        // Animated Aurora Gradient Blobs
        val infiniteTransition = rememberInfiniteTransition(label = "aurora")
        
        val xOffset1 by infiniteTransition.animateFloat(
            initialValue = 0.1f, targetValue = 0.9f,
            animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
            label = "x1"
        )
        val yOffset1 by infiniteTransition.animateFloat(
            initialValue = 0.1f, targetValue = 0.8f,
            animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse),
            label = "y1"
        )
        
        val xOffset2 by infiniteTransition.animateFloat(
            initialValue = 0.8f, targetValue = 0.2f,
            animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse),
            label = "x2"
        )
        val yOffset2 by infiniteTransition.animateFloat(
            initialValue = 0.7f, targetValue = 0.1f,
            animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Reverse),
            label = "y2"
        )

        Canvas(modifier = Modifier.fillMaxSize().blur(120.dp)) {
            drawCircle(
                color = Primary.copy(alpha = 0.25f),
                radius = size.width * 0.7f,
                center = Offset(size.width * xOffset1, size.height * yOffset1)
            )
            drawCircle(
                color = AccentPink.copy(alpha = 0.2f),
                radius = size.width * 0.6f,
                center = Offset(size.width * xOffset2, size.height * yOffset2)
            )
            drawCircle(
                color = AccentBlue.copy(alpha = 0.15f),
                radius = size.width * 0.8f,
                center = Offset(size.width * (1f - xOffset1), size.height * (1f - yOffset2))
            )
        }
        
        content()
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -200f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.0f),
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim - 300f, translateAnim - 300f),
            end = Offset(translateAnim, translateAnim),
            tileMode = TileMode.Clamp
        )
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    useShimmer: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .then(if (useShimmer) Modifier.shimmerEffect() else Modifier)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

@Composable
fun GlassFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            ),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.9f),
                            Secondary.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun GlassPillNav(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .height(72.dp)
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(36.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(36.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home, selectedIndex == 0) { onItemSelected(0) }
            NavItem(Icons.Default.Notifications, selectedIndex == 1) { onItemSelected(1) }
            NavItem(Icons.Default.Timer, selectedIndex == 2) { onItemSelected(2) }
            NavItem(Icons.Default.Add, selectedIndex == 3, isMain = true) { onItemSelected(3) }
            NavItem(Icons.Default.Schedule, selectedIndex == 4) { onItemSelected(4) }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    isSelected: Boolean,
    isMain: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isMain) 56.dp else 48.dp)
            .clip(CircleShape)
            .background(if (isMain) Primary else if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected || isMain) Color.White else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(if (isMain) 28.dp else 24.dp)
        )
    }
}
