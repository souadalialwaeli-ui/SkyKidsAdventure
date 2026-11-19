package com.example.abbasgame.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abbasgame.audio.SoundManager
import com.example.abbasgame.data.GameRepository

@Composable
fun LevelCompleteScreen(
    context: android.content.Context,
    score: Int,
    rewardEarned: Boolean,
    onNextLevel: () -> Unit,
    onHome: () -> Unit
) {
    val repository = remember { GameRepository(context) }
    val soundManager = remember { SoundManager(context) }
    var hasEntered by remember { mutableStateOf(false) }
    val entrance by animateFloatAsState(
        targetValue = if (hasEntered) 1f else 0.72f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 280f),
        label = "celebrationEntrance"
    )
    val sparkle by rememberInfiniteTransition(label = "celebration").animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse),
        label = "sparkle"
    )

    LaunchedEffect(Unit) {
        hasEntered = true
        soundManager.playVictorySound(repository.isSoundEnabled())
    }
    DisposableEffect(soundManager) {
        onDispose { soundManager.stopAllSounds() }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF6BC9F5), Color(0xFFFFD66B), Color(0xFFFFA94D))))
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        val compact = maxHeight < 430.dp
        Canvas(Modifier.fillMaxSize()) {
            val points = listOf(
                0.10f to 0.18f, 0.22f to 0.76f, 0.35f to 0.25f, 0.52f to 0.82f,
                0.68f to 0.17f, 0.82f to 0.73f, 0.94f to 0.3f, 0.42f to 0.55f,
                0.75f to 0.48f, 0.16f to 0.51f
            )
            points.forEachIndexed { index, (x, y) ->
                val radius = (7f + (index % 3) * 2f) * sparkle
                drawStar(
                    androidx.compose.ui.geometry.Offset(size.width * x, size.height * y),
                    radius,
                    radius * 0.48f,
                    Color(0xFFFFF4B0).copy(alpha = 0.72f)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .graphicsLayer { scaleX = entrance; scaleY = entrance },
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
            shape = RoundedCornerShape(30.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(if (compact) 18.dp else 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 13.dp)
            ) {
                Text(
                    "أحسنت يا بطل!",
                    fontSize = if (compact) 24.sp else 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE56A19),
                    textAlign = TextAlign.Center
                )
                Text(
                    "أكملت المرحلة بمهارة رائعة",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF587080)
                )
                Text("★★★", fontSize = if (compact) 34.sp else 40.sp, color = Color(0xFFFFB800), letterSpacing = 5.sp)

                Surface(color = Color(0xFFFFF4D6), shape = RoundedCornerShape(18.dp)) {
                    Text(
                        "النقاط المحققة  $score",
                        Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF75512D)
                    )
                }

                if (rewardEarned) {
                    Text(
                        "مكافأة جديدة: 5 نجوم و30 عملة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF32824E),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        "أعدت لعب المرحلة وحافظت على تقدمك",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF587080),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onNextLevel()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39A85A)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("المرحلة التالية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onHome()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text("القائمة الرئيسية", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
