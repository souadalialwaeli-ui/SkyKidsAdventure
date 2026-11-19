package com.example.abbasgame.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abbasgame.audio.SoundManager
import com.example.abbasgame.data.GameRepository
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun SplashScreen(onNavigateToMain: () -> Unit) {

    // Infinite pulsing scale for logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logoScale"
    )
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -30f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "cloud"
    )
    val planeX by infiniteTransition.animateFloat(
        initialValue = -50f, targetValue = 50f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "planeX"
    )
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "star"
    )

    // Enter animation
    var visible by remember { mutableStateOf(false) }
    val entryScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "entry"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2800)
        onNavigateToMain()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5), Color(0xFF87CEEB))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated sky canvas
        Canvas(Modifier.fillMaxSize()) {
            // Stars
            val stars = listOf(
                Offset(80f, 120f), Offset(240f, 60f), Offset(400f, 180f),
                Offset(600f, 90f), Offset(750f, 200f), Offset(900f, 70f),
                Offset(150f, 400f), Offset(850f, 350f), Offset(500f, 300f)
            )
            stars.forEachIndexed { i, s ->
                val a = if (i % 2 == 0) starAlpha else 1f - starAlpha
                drawCircle(Color.White.copy(alpha = a), radius = if (i % 3 == 0) 5f else 3f, center = s)
            }
            // Far clouds
            drawCloud(Offset(size.width * 0.15f + cloudOffset, size.height * 0.25f), 80f, 0.5f)
            drawCloud(Offset(size.width * 0.75f - cloudOffset, size.height * 0.3f), 60f, 0.4f)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(entryScale)
        ) {
            // Animated plane on splash
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(logoScale)
                    .offset(x = planeX.dp, y = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2 + planeX * 0.5f
                    val cy = size.height / 2
                    // Shadow
                    drawOval(
                        Color.Black.copy(alpha = 0.15f),
                        topLeft = Offset(cx - 55f, cy + 35f),
                        size = Size(110f, 20f)
                    )
                    // Body
                    drawRoundRect(
                        Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFFF5252))),
                        topLeft = Offset(cx - 55f, cy - 20f),
                        size = Size(110f, 40f),
                        cornerRadius = CornerRadius(20f)
                    )
                    // Wing top
                    drawPath(
                        Path().apply {
                            moveTo(cx - 10f, cy - 20f)
                            lineTo(cx - 50f, cy - 65f)
                            lineTo(cx + 20f, cy - 20f)
                            close()
                        },
                        Brush.verticalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6F00)))
                    )
                    // Wing bottom
                    drawPath(
                        Path().apply {
                            moveTo(cx - 10f, cy + 20f)
                            lineTo(cx - 50f, cy + 55f)
                            lineTo(cx + 20f, cy + 20f)
                            close()
                        },
                        Brush.verticalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6F00)))
                    )
                    // Cockpit
                    drawCircle(Color(0xFF81D4FA), radius = 18f, center = Offset(cx - 10f, cy))
                    drawCircle(Color(0xFFE3F2FD), radius = 10f, center = Offset(cx - 12f, cy - 2f))
                    // Propeller
                    drawCircle(Color(0xFF37474F), radius = 10f, center = Offset(cx + 56f, cy))
                    drawLine(Color.White, Offset(cx + 56f, cy - 20f), Offset(cx + 56f, cy + 20f), 4f)
                    drawLine(Color.White, Offset(cx + 36f, cy), Offset(cx + 76f, cy), 4f)
                    // Exhaust circles
                    repeat(3) { i ->
                        drawCircle(
                            Color.White.copy(alpha = 0.4f - i * 0.1f),
                            radius = (8f - i * 2f),
                            center = Offset(cx - 55f - i * 15f, cy)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Title with golden shadow effect
            Text(
                "✈ Sky Kids Adventure",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(Color(0xFF01579B), Offset(2f, 4f), blurRadius = 8f)
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "🌟 مغامرات الطائرات للأطفال 🌟",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFEB3B),
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(Color(0xFFE65100), Offset(1f, 2f), blurRadius = 4f)
                )
            )

            Spacer(Modifier.height(40.dp))

            // Loading dots
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { i ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.6f, targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            tween(500, delayMillis = i * 160, easing = FastOutSlowInEasing),
                            RepeatMode.Reverse
                        ),
                        label = "dot$i"
                    )
                    Box(
                        Modifier
                            .size(14.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

// Helper extension to draw a cloud shape
fun DrawScope.drawCloud(center: Offset, radius: Float, alpha: Float) {
    drawCircle(Color.White.copy(alpha = alpha), radius = radius, center = center)
    drawCircle(Color.White.copy(alpha = alpha), radius = radius * 0.75f, center = Offset(center.x + radius * 0.9f, center.y + radius * 0.2f))
    drawCircle(Color.White.copy(alpha = alpha), radius = radius * 0.65f, center = Offset(center.x - radius * 0.8f, center.y + radius * 0.3f))
    drawOval(
        Color.White.copy(alpha = alpha),
        topLeft = Offset(center.x - radius * 1.5f, center.y),
        size = Size(radius * 3.2f, radius * 0.9f)
    )
}
