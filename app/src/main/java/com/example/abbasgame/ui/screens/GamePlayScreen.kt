package com.example.abbasgame.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abbasgame.audio.SoundManager
import com.example.abbasgame.data.GameRepository
import com.example.abbasgame.model.GameMode
import com.example.abbasgame.model.GameState
import com.example.abbasgame.model.ObjectType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private data class FlightObject(
    val id: Long,
    val type: ObjectType,
    var x: Float,
    var y: Float,
    var phase: Float = Random.nextFloat() * 6.28f,
    var active: Boolean = true
)

@Composable
fun GamePlayScreen(
    context: android.content.Context,
    levelId: Int,
    onBack: () -> Unit,
    onWin: (Int, Boolean) -> Unit
) {
    val repository = remember { GameRepository(context) }
    val soundManager = remember { SoundManager(context) }
    val level = remember(levelId) { repository.getLevels().firstOrNull { it.id == levelId } }
    val mode = level?.gameMode ?: GameMode.FLYING_ADVENTURE
    val targetScore = level?.targetScore ?: 8
    val selectedPlane = remember {
        repository.getAirplanes().firstOrNull { it.id == repository.getSelectedAirplaneId() }
            ?: repository.getAirplanes().first()
    }
    val background = remember { loadBitmap(context, "bg_sky") }
    val star = remember { loadBitmap(context, "star") }
    val coin = remember { loadBitmap(context, "coin") }
    val ring = remember { loadBitmap(context, "ring") }
    val animal = remember { loadBitmap(context, "animal") }
    val obstacle = remember { loadBitmap(context, "obstacle") }

    var gameState by remember(levelId) { mutableStateOf(GameState.PLAYING) }
    var score by remember(levelId) { mutableIntStateOf(0) }
    var lives by remember(levelId) { mutableIntStateOf(3) }
    var planeX by remember(levelId) { mutableFloatStateOf(0.27f) }
    var planeY by remember(levelId) { mutableFloatStateOf(0.52f) }
    var tilt by remember(levelId) { mutableFloatStateOf(0f) }
    var worldWidth by remember { mutableFloatStateOf(1f) }
    var worldHeight by remember { mutableFloatStateOf(1f) }
    var elapsed by remember(levelId) { mutableFloatStateOf(0f) }
    val objects = remember(levelId) { mutableStateListOf<FlightObject>() }
    var nextObjectId by remember(levelId) { mutableLongStateOf(0L) }
    var spawnTimer by remember(levelId) { mutableFloatStateOf(0f) }
    var rewardCommitted by remember(levelId) { mutableStateOf(false) }

    fun startMusic() = soundManager.startGameMusic(repository.isMusicEnabled())
    val lifecycleOwner = context as? LifecycleOwner
    val currentGameState by rememberUpdatedState(gameState)

    DisposableEffect(soundManager, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> soundManager.pauseBgMusic()
                Lifecycle.Event.ON_RESUME -> {
                    if (currentGameState == GameState.PLAYING) {
                        soundManager.resumeBgMusic(repository.isMusicEnabled())
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            soundManager.stopAllSounds()
        }
    }

    LaunchedEffect(levelId) {
        startMusic()
    }

    LaunchedEffect(gameState, levelId) {
        if (gameState != GameState.PLAYING) return@LaunchedEffect
        var previousFrame = 0L
        while (gameState == GameState.PLAYING) {
            withFrameNanos { frameTime ->
                val dt = if (previousFrame == 0L) 1f / 60f
                else ((frameTime - previousFrame) / 1_000_000_000f).coerceIn(0f, 0.05f)
                previousFrame = frameTime
                elapsed += dt
                spawnTimer += dt
                tilt *= 0.88f

                val spawnEvery = if (repository.getDifficulty() == "صعب") 0.95f else 1.25f
                if (spawnTimer >= spawnEvery && worldWidth > 1f && worldHeight > 1f) {
                    spawnTimer = 0f
                    val pick = Random.nextFloat()
                    val type = when (mode) {
                        GameMode.STAR_COLLECTION -> if (pick < 0.78f) ObjectType.STAR else ObjectType.COIN
                        GameMode.RING_CHALLENGE -> if (pick < 0.72f) ObjectType.RING else ObjectType.OBSTACLE_BIRD
                        GameMode.ANIMAL_RESCUE -> if (pick < 0.42f) ObjectType.ANIMAL else ObjectType.OBSTACLE_BIRD
                        GameMode.FLYING_ADVENTURE -> when {
                            pick < 0.43f -> ObjectType.STAR
                            pick < 0.65f -> ObjectType.COIN
                            pick < 0.78f -> ObjectType.RING
                            pick < 0.88f -> ObjectType.ANIMAL
                            else -> ObjectType.OBSTACLE_BIRD
                        }
                    }
                    objects.add(FlightObject(++nextObjectId, type, Random.nextFloat() * 0.76f + 0.12f, -0.08f))
                }

                val speed = (when (repository.getDifficulty()) {
                    "صعب" -> 0.38f
                    "متوسط" -> 0.29f
                    else -> 0.23f
                }) * selectedPlane.speedMultiplier
                objects.forEach { obj ->
                    if (!obj.active) return@forEach
                    obj.y += speed * dt
                    obj.phase += dt * 2.3f
                    if (obj.type == ObjectType.OBSTACLE_BIRD) {
                        obj.x += kotlin.math.sin(obj.phase) * dt * 0.12f
                        obj.x = obj.x.coerceIn(0.08f, 0.92f)
                    }
                    val hitRadius = when (obj.type) {
                        ObjectType.RING -> 0.075f
                        ObjectType.OBSTACLE_BIRD -> 0.055f
                        else -> 0.06f
                    }
                    if (abs(obj.x - planeX) < hitRadius && abs(obj.y - planeY) < hitRadius) {
                        obj.active = false
                        when (obj.type) {
                            ObjectType.STAR -> { score += 1; soundManager.playStarSound(repository.isSoundEnabled()) }
                            ObjectType.COIN -> { score += 2; soundManager.playCoinSound(repository.isSoundEnabled()) }
                            ObjectType.RING -> { score += 3; soundManager.playRingSound(repository.isSoundEnabled()) }
                            ObjectType.ANIMAL -> { score += 3; soundManager.playRescueSound(repository.isSoundEnabled()) }
                            ObjectType.OBSTACLE_BIRD, ObjectType.OBSTACLE_CLOUD -> {
                                lives = max(0, lives - 1)
                                soundManager.playHitSound(repository.isSoundEnabled())
                                if (lives == 0) {
                                    gameState = GameState.LOST
                                    soundManager.playFailureSound(repository.isSoundEnabled())
                                }
                            }
                        }
                    } else if (obj.y > 1.12f) {
                        obj.active = false
                    }
                }
                objects.removeAll { !it.active }
                if (score >= targetScore && !rewardCommitted) {
                    rewardCommitted = true
                    gameState = GameState.WON
                    val firstCompletion = !repository.hasCollectedLevelReward(levelId)
                    if (firstCompletion) {
                        repository.addStars(5)
                        repository.addCoins(30)
                        repository.markLevelRewardCollected(levelId)
                    }
                    repository.unlockLevel(levelId + 1)
                    repository.setLevelStars(levelId, 3)
                    repository.checkAchievements()
                    onWin(score, firstCompletion)
                }
            }
        }
    }

    val objective = when (mode) {
        GameMode.FLYING_ADVENTURE -> "اجمع النجوم والعملات وتجنب الطيور"
        GameMode.STAR_COLLECTION -> "اجمع النجوم اللامعة"
        GameMode.RING_CHALLENGE -> "وجّه الطائرة عبر الحلقات"
        GameMode.ANIMAL_RESCUE -> "أنقذ الحيوانات اللطيفة"
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF72C8F3))) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        SideEffect {
            worldWidth = wPx.coerceAtLeast(1f)
            worldHeight = hPx.coerceAtLeast(1f)
        }

        Canvas(
            Modifier.fillMaxSize().pointerInput(levelId, gameState) {
                detectDragGestures { change, amount ->
                    change.consume()
                    if (gameState == GameState.PLAYING) {
                        planeX = (planeX + amount.x / size.width).coerceIn(0.08f, 0.92f)
                        planeY = (planeY + amount.y / size.height).coerceIn(0.13f, 0.88f)
                        tilt = (amount.x / size.width * 160f).coerceIn(-18f, 18f)
                    }
                }
            }
        ) {
            val w = size.width
            val h = size.height
            if (background != null) {
                drawImage(background, dstSize = IntSize(w.toInt(), h.toInt()), alpha = 0.88f)
            } else {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFFB3E5FC))))
            }
            val cloudTravel = (elapsed * 0.035f) % 1.2f
            for (i in 0..4) {
                val x = ((i * 0.31f + cloudTravel) % 1.3f - 0.12f) * w
                val y = h * (0.18f + (i % 3) * 0.2f)
                drawFlightCloud(Offset(x, y), w * (0.07f + (i % 2) * 0.025f), 0.24f)
            }
            objects.forEach { obj ->
                val center = Offset(obj.x * w, obj.y * h)
                val bob = kotlin.math.sin(obj.phase) * 5f
                val bmp = when (obj.type) {
                    ObjectType.STAR -> star
                    ObjectType.COIN -> coin
                    ObjectType.RING -> ring
                    ObjectType.ANIMAL -> animal
                    ObjectType.OBSTACLE_BIRD, ObjectType.OBSTACLE_CLOUD -> obstacle
                }
                val side = when (obj.type) {
                    ObjectType.RING -> min(w, h) * 0.12f
                    ObjectType.ANIMAL -> min(w, h) * 0.09f
                    else -> min(w, h) * 0.075f
                }
                if (obj.type == ObjectType.RING) {
                    drawCircle(Color(0x66FFF176), side * (0.7f + 0.08f * kotlin.math.sin(elapsed * 4f)), center, style = Stroke(7f))
                }
                if (bmp != null) {
                    drawImage(bmp, dstOffset = IntOffset((center.x - side / 2).toInt(), (center.y + bob - side / 2).toInt()), dstSize = IntSize(side.toInt(), side.toInt()))
                } else {
                    drawCircle(if (obj.type == ObjectType.STAR) Color(0xFFFFD740) else Color.White, side / 2, Offset(center.x, center.y + bob))
                }
            }
            val planeCenter = Offset(planeX * w, planeY * h + kotlin.math.sin(elapsed * 3f) * h * 0.008f)
            repeat(5) { i ->
                drawCircle(Color.White.copy(alpha = 0.28f - i * 0.04f), min(w, h) * (0.018f - i * 0.002f), Offset(planeCenter.x - w * (0.045f + i * 0.018f), planeCenter.y + h * 0.008f))
            }
            rotate(tilt, planeCenter) {
                drawCartoonPlane(planeCenter, min(w, h) * 0.16f, elapsed, Color(selectedPlane.colorPrimary), Color(selectedPlane.colorSecondary))
            }
        }

        Column(Modifier.fillMaxWidth().align(Alignment.TopCenter).safeDrawingPadding().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { soundManager.stopAllSounds(); onBack() }) { Text("خروج") }
                Surface(color = Color(0xDDFFFFFF), shape = RoundedCornerShape(20.dp)) {
                    Text("❤️".repeat(lives) + "   ⭐ $score", Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                }
                OutlinedButton(onClick = { gameState = GameState.PAUSED; soundManager.pauseBgMusic() }) { Text("إيقاف") }
            }
            Spacer(Modifier.height(8.dp))
            Surface(color = Color(0xCCFFFFFF), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth(0.9f).padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(objective, fontSize = 13.sp, color = Color(0xFF23445E), fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { (score.toFloat() / targetScore).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            }
        }

        if (gameState == GameState.PAUSED || gameState == GameState.LOST) {
            Box(Modifier.fillMaxSize().background(Color(0x990D2850)), contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (gameState == GameState.PAUSED) "استراحة الطيار" else "لنحاول مرة أخرى!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF174A75))
                        Text("النقاط $score من $targetScore", color = Color.DarkGray)
                        Button(onClick = {
                            if (gameState == GameState.LOST) {
                                score = 0
                                lives = 3
                                planeX = 0.27f
                                planeY = 0.52f
                                objects.clear()
                                spawnTimer = 0f
                                rewardCommitted = false
                            }
                            gameState = GameState.PLAYING
                            startMusic()
                        }) { Text(if (gameState == GameState.PAUSED) "متابعة الرحلة" else "إعادة اللعب") }
                        TextButton(onClick = { soundManager.stopAllSounds(); onBack() }) { Text("العودة للقائمة") }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawFlightCloud(center: Offset, width: Float, alpha: Float) {
    val cloudHeight = width * 0.42f
    val white = Color.White.copy(alpha = alpha)
    drawOval(
        color = Color(0xFF76B8E9).copy(alpha = alpha * 0.2f),
        topLeft = Offset(center.x - width * 0.46f, center.y + cloudHeight * 0.12f),
        size = Size(width * 0.92f, cloudHeight * 0.68f)
    )
    drawOval(
        color = white,
        topLeft = Offset(center.x - width * 0.47f, center.y - cloudHeight * 0.02f),
        size = Size(width * 0.94f, cloudHeight * 0.62f)
    )
    drawOval(
        color = white,
        topLeft = Offset(center.x - width * 0.25f, center.y - cloudHeight * 0.34f),
        size = Size(width * 0.43f, cloudHeight * 0.78f)
    )
    drawOval(
        color = white.copy(alpha = alpha * 0.88f),
        topLeft = Offset(center.x + width * 0.02f, center.y - cloudHeight * 0.24f),
        size = Size(width * 0.4f, cloudHeight * 0.66f)
    )
}

fun DrawScope.drawCartoonPlane(
    center: Offset,
    size: Float,
    timeSeconds: Float,
    primary: Color = Color(0xFFE53935),
    secondary: Color = Color(0xFFFF8A65)
) {
    val x = center.x
    val y = center.y
    val s = size

    drawOval(
        Color(0x330C3C68),
        Offset(x - s * 0.52f, y + s * 0.27f),
        Size(s * 0.96f, s * 0.14f)
    )
    drawPath(
        Path().apply {
            moveTo(x - s * 0.23f, y - s * 0.04f)
            lineTo(x - s * 0.02f, y - s * 0.43f)
            quadraticTo(x + s * 0.08f, y - s * 0.49f, x + s * 0.12f, y - s * 0.08f)
            close()
        },
        Brush.verticalGradient(listOf(secondary, primary), y - s * 0.5f, y)
    )
    drawPath(
        Path().apply {
            moveTo(x - s * 0.24f, y + s * 0.03f)
            lineTo(x - s * 0.02f, y + s * 0.38f)
            quadraticTo(x + s * 0.1f, y + s * 0.43f, x + s * 0.13f, y + s * 0.08f)
            close()
        },
        Brush.verticalGradient(listOf(primary, secondary), y, y + s * 0.45f)
    )
    drawPath(
        Path().apply {
            moveTo(x - s * 0.43f, y - s * 0.07f)
            lineTo(x - s * 0.55f, y - s * 0.23f)
            lineTo(x - s * 0.31f, y - s * 0.12f)
            lineTo(x - s * 0.3f, y + s * 0.1f)
            lineTo(x - s * 0.56f, y + s * 0.21f)
            lineTo(x - s * 0.4f, y + s * 0.04f)
            close()
        },
        Brush.verticalGradient(listOf(secondary, primary), y - s * 0.24f, y + s * 0.24f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(primary, secondary, primary), x - s * 0.52f, x + s * 0.56f),
        topLeft = Offset(x - s * 0.52f, y - s * 0.13f),
        size = Size(s * 1.04f, s * 0.26f),
        cornerRadius = CornerRadius(s * 0.13f)
    )
    drawOval(
        Color(0xFF80DEEA),
        Offset(x - s * 0.05f, y - s * 0.095f),
        Size(s * 0.25f, s * 0.17f)
    )
    drawOval(
        Color(0xFFE1F5FE),
        Offset(x - s * 0.025f, y - s * 0.09f),
        Size(s * 0.14f, s * 0.085f)
    )
    drawCircle(Color(0xFF455A64), s * 0.045f, Offset(x + s * 0.54f, y))
    rotate((timeSeconds * 900f) % 360f, Offset(x + s * 0.54f, y)) {
        drawLine(Color.White, Offset(x + s * 0.54f, y - s * 0.15f), Offset(x + s * 0.54f, y + s * 0.15f), s * 0.035f)
        drawLine(Color.White, Offset(x + s * 0.39f, y), Offset(x + s * 0.69f, y), s * 0.035f)
    }
}

private fun loadBitmap(context: android.content.Context, name: String): ImageBitmap? =
    runCatching {
        val resourceId = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (resourceId == 0) null
        else android.graphics.BitmapFactory.decodeResource(context.resources, resourceId)?.asImageBitmap()
    }.getOrNull()
