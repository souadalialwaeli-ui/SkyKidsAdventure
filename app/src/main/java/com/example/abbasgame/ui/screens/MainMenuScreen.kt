package com.example.abbasgame.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.abbasgame.audio.SoundManager
import com.example.abbasgame.data.GameRepository
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun MainMenuScreen(
    context: android.content.Context,
    onStartGame: () -> Unit,
    onOpenAirplanes: () -> Unit,
    onOpenLevels: () -> Unit,
    onOpenRewards: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val repository = remember { GameRepository(context) }
    val soundManager = remember { SoundManager(context) }
    var stars by remember { mutableIntStateOf(repository.getStars()) }
    var coins by remember { mutableIntStateOf(repository.getCoins()) }

    // Start menu music
    LaunchedEffect(Unit) {
        soundManager.startMenuMusic(repository.isMusicEnabled())
        stars = repository.getStars()
        coins = repository.getCoins()
    }
    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(soundManager, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> soundManager.pauseBgMusic()
                Lifecycle.Event.ON_RESUME -> soundManager.resumeBgMusic(repository.isMusicEnabled())
                else -> Unit
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            soundManager.stopAllSounds()
        }
    }

    // Animations
    val infinite = rememberInfiniteTransition(label = "menu")

    val cloudOffset1 by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "c1"
    )
    val cloudOffset2 by infinite.animateFloat(
        1f, 0f,
        infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Restart),
        label = "c2"
    )
    val planeY by infinite.animateFloat(
        -6f, 6f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "planeY"
    )
    val planeTilt by infinite.animateFloat(
        -4f, 4f,
        infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
        label = "planeTilt"
    )
    val starTwinkle by infinite.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkle"
    )
    val sunGlow by infinite.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sunGlow"
    )
    val titleScale by infinite.animateFloat(
        1f, 1.04f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "titleScale"
    )

    // Bird positions
    val bird1X by infinite.animateFloat(
        -100f, 1200f,
        infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "bird1"
    )
    val bird2X by infinite.animateFloat(
        1300f, -100f,
        infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Restart),
        label = "bird2"
    )
    val wingFlap by infinite.animateFloat(
        -20f, 20f,
        infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wing"
    )

    Box(Modifier.fillMaxSize()) {
        // ── Sky background canvas ──────────────────────────────────────────────
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height

            // Sky gradient
            drawRect(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5), Color(0xFF87CEEB), Color(0xFFB3E5FC))
                )
            )

            // Sun with glow
            val sunCenter = Offset(w * 0.85f, h * 0.12f)
            drawCircle(Color(0xFFFFE082).copy(alpha = sunGlow * 0.3f), radius = 90f, center = sunCenter)
            drawCircle(Color(0xFFFFD54F).copy(alpha = sunGlow * 0.5f), radius = 65f, center = sunCenter)
            drawCircle(Color(0xFFFFD700), radius = 50f, center = sunCenter)
            drawCircle(Color(0xFFFFF9C4), radius = 35f, center = sunCenter)

            // Stars (top area)
            val starPositions = listOf(
                Offset(w * 0.05f, h * 0.03f), Offset(w * 0.2f, h * 0.07f),
                Offset(w * 0.4f, h * 0.02f), Offset(w * 0.6f, h * 0.08f),
                Offset(w * 0.1f, h * 0.15f), Offset(w * 0.35f, h * 0.12f)
            )
            starPositions.forEachIndexed { i, pos ->
                val a = if (i % 2 == 0) starTwinkle else 1f - starTwinkle + 0.3f
                drawStar(pos, 8f, 4f, Color(0xFFFFEB3B).copy(alpha = a))
            }

            // Far clouds
            val c1x = -200f + cloudOffset1 * (w + 400f)
            drawCloud(Offset(c1x, h * 0.18f), 70f, 0.85f)
            drawCloud(Offset(c1x - 350f, h * 0.22f), 50f, 0.75f)

            val c2x = w + 200f - cloudOffset2 * (w + 400f)
            drawCloud(Offset(c2x, h * 0.15f), 85f, 0.9f)
            drawCloud(Offset(c2x + 300f, h * 0.2f), 60f, 0.7f)

            // Ground hills
            val hillPath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.82f)
                cubicTo(w * 0.15f, h * 0.75f, w * 0.3f, h * 0.85f, w * 0.5f, h * 0.78f)
                cubicTo(w * 0.7f, h * 0.72f, w * 0.85f, h * 0.8f, w, h * 0.75f)
                lineTo(w, h)
                close()
            }
            drawPath(hillPath, Brush.verticalGradient(listOf(Color(0xFF388E3C), Color(0xFF1B5E20)), h * 0.75f, h))

            // Foreground near clouds
            drawCloud(Offset(w * 0.1f, h * 0.42f), 100f, 0.95f)
            drawCloud(Offset(w * 0.72f, h * 0.38f), 90f, 0.9f)

            // Birds
            drawBird(Offset(bird1X, h * 0.28f), wingFlap, 1.2f)
            drawBird(Offset(bird2X, h * 0.35f), -wingFlap, 0.9f)

            // Plane (hero) in center-ish area
            val planeCenterX = w * 0.3f
            val planeCenterY = h * 0.52f + planeY
            rotate(planeTilt, Offset(planeCenterX, planeCenterY)) {
                // Shadow
                drawOval(
                    Color.Black.copy(alpha = 0.12f),
                    topLeft = Offset(planeCenterX - 65f, planeCenterY + 48f),
                    size = Size(130f, 20f)
                )
                // Exhaust trail
                repeat(4) { i ->
                    drawCircle(
                        Color.White.copy(alpha = 0.35f - i * 0.07f),
                        radius = 10f - i * 2f,
                        center = Offset(planeCenterX - 60f - i * 20f, planeCenterY)
                    )
                }
                // Body
                drawRoundRect(
                    Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFFF5252))),
                    topLeft = Offset(planeCenterX - 60f, planeCenterY - 22f),
                    size = Size(120f, 44f),
                    cornerRadius = CornerRadius(22f)
                )
                // Top wing
                drawPath(
                    Path().apply {
                        moveTo(planeCenterX - 15f, planeCenterY - 22f)
                        lineTo(planeCenterX - 60f, planeCenterY - 70f)
                        lineTo(planeCenterX + 25f, planeCenterY - 22f)
                        close()
                    },
                    Brush.verticalGradient(listOf(Color(0xFFFFB300), Color(0xFFE65100)))
                )
                // Bottom wing
                drawPath(
                    Path().apply {
                        moveTo(planeCenterX - 15f, planeCenterY + 22f)
                        lineTo(planeCenterX - 60f, planeCenterY + 60f)
                        lineTo(planeCenterX + 25f, planeCenterY + 22f)
                        close()
                    },
                    Brush.verticalGradient(listOf(Color(0xFFFFB300), Color(0xFFE65100)))
                )
                // Cockpit
                drawCircle(Color(0xFF81D4FA), radius = 20f, center = Offset(planeCenterX - 12f, planeCenterY))
                drawCircle(Color(0xFFE3F2FD), radius = 11f, center = Offset(planeCenterX - 14f, planeCenterY - 3f))
                // Propeller
                drawCircle(Color(0xFF37474F), radius = 12f, center = Offset(planeCenterX + 61f, planeCenterY))
                rotate(wingFlap * 6f, Offset(planeCenterX + 61f, planeCenterY)) {
                    drawLine(Color.White, Offset(planeCenterX + 61f, planeCenterY - 24f), Offset(planeCenterX + 61f, planeCenterY + 24f), 5f)
                    drawLine(Color.White, Offset(planeCenterX + 37f, planeCenterY), Offset(planeCenterX + 85f, planeCenterY), 5f)
                }
            }
        }

        // ── UI Overlay ─────────────────────────────────────────────────────────
        Column(
            Modifier.fillMaxSize().padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: Stars & Coins
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                CurrencyBadge("⭐", stars, Color(0xFFFFD700), Color(0xFF5D4037))
                CurrencyBadge("🪙", coins, Color(0xFFFF8F00), Color.White)
            }

            Spacer(Modifier.height(8.dp))

            // Title Logo
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(titleScale)) {
                Box(
                    Modifier
                        .shadow(16.dp, RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF0D47A1))),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "✈ Sky Kids Adventure",
                            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = Shadow(Color.Black, Offset(2f, 3f), 6f)
                            )
                        )
                        Text(
                            "مغامرات الطائرات للأطفال",
                            fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFFE3F2FD), textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Menu Buttons ─────────────────────────────────────────────────
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // PRIMARY: Start Game
                PulsingGameButton(
                    label = "🚀  ابدأ المغامرة !",
                    bgColors = listOf(Color(0xFF00C853), Color(0xFF43A047), Color(0xFF1B5E20)),
                    onClick = {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onStartGame()
                    },
                    fontSize = 22,
                    modifier = Modifier.fillMaxWidth(0.88f).height(66.dp)
                )

                // 2-column row
                Row(Modifier.fillMaxWidth(0.88f), Arrangement.spacedBy(10.dp)) {
                    GameMenuButton("✈️ اختر طائرتك", Color(0xFFAB47BC), Color(0xFF7B1FA2),
                        Modifier.weight(1f).height(58.dp)) {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onOpenAirplanes()
                    }
                    GameMenuButton("🗺️ خريطة العوالم", Color(0xFF26A69A), Color(0xFF00695C),
                        Modifier.weight(1f).height(58.dp)) {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onOpenLevels()
                    }
                }

                Row(Modifier.fillMaxWidth(0.88f), Arrangement.spacedBy(10.dp)) {
                    GameMenuButton("🎁 المكافآت", Color(0xFFFF7043), Color(0xFFBF360C),
                        Modifier.weight(1f).height(52.dp)) {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onOpenRewards()
                    }
                    GameMenuButton("🏆 الإنجازات", Color(0xFF5C6BC0), Color(0xFF283593),
                        Modifier.weight(1f).height(52.dp)) {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onOpenAchievements()
                    }
                }

                GameMenuButton("⚙️  الإعدادات", Color(0xFF78909C), Color(0xFF37474F),
                    Modifier.fillMaxWidth(0.55f).height(46.dp)) {
                    soundManager.playClickSound(repository.isSoundEnabled())
                    onOpenSettings()
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

// ── Reusable UI Components ────────────────────────────────────────────────────

@Composable
fun CurrencyBadge(icon: String, value: Int, bgColor: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        shadowElevation = 6.dp
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 18.sp)
            Text("$value", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
        }
    }
}

@Composable
fun GameMenuButton(
    label: String,
    colorTop: Color,
    colorBottom: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "btn")

    Box(
        modifier
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(listOf(colorTop, colorBottom)),
                RoundedCornerShape(28.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun PulsingGameButton(
    label: String,
    bgColors: List<Color>,
    onClick: () -> Unit,
    fontSize: Int = 18,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val glow by infinite.animateFloat(
        0.85f, 1f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.94f else glow, spring(Spring.DampingRatioMediumBouncy), label = "ps")

    Box(
        modifier
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(33.dp))
            .background(
                Brush.verticalGradient(bgColors),
                RoundedCornerShape(33.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = fontSize.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

// ── Canvas drawing helpers ────────────────────────────────────────────────────
fun DrawScope.drawStar(center: Offset, outerR: Float, innerR: Float, color: Color) {
    val path = Path()
    for (i in 0 until 10) {
        val angle = Math.toRadians(i * 36.0 - 90.0)
        val r = if (i % 2 == 0) outerR else innerR
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

fun DrawScope.drawBird(pos: Offset, wingAngle: Float, scale: Float) {
    val s = scale * 20f
    // Left wing
    rotate(wingAngle, pos) {
        drawPath(
            Path().apply {
                moveTo(pos.x, pos.y)
                cubicTo(pos.x - s, pos.y - s, pos.x - s * 2f, pos.y - s * 0.5f, pos.x - s * 2.5f, pos.y)
            },
            Color(0xFF37474F),
            style = Stroke(width = s * 0.4f, cap = StrokeCap.Round)
        )
    }
    rotate(-wingAngle, pos) {
        drawPath(
            Path().apply {
                moveTo(pos.x, pos.y)
                cubicTo(pos.x + s, pos.y - s, pos.x + s * 2f, pos.y - s * 0.5f, pos.x + s * 2.5f, pos.y)
            },
            Color(0xFF37474F),
            style = Stroke(width = s * 0.4f, cap = StrokeCap.Round)
        )
    }
}
