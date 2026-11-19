package com.example.abbasgame.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abbasgame.audio.SoundManager
import com.example.abbasgame.data.GameRepository
import com.example.abbasgame.model.Airplane

@Composable
fun AirplaneSelectionScreen(
    context: android.content.Context,
    onBack: () -> Unit
) {
    val repository = remember { GameRepository(context) }
    val soundManager = remember { SoundManager(context) }
    var airplanes by remember { mutableStateOf(repository.getAirplanes()) }
    var selectedId by remember { mutableStateOf(repository.getSelectedAirplaneId()) }
    var totalStars = remember { mutableStateOf(repository.getStars()) }
    val propellerPhase by rememberInfiniteTransition(label = "planePreview").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "propellerPhase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2), Color(0xFF4A148C))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Text(text = "⬅️ رجوع", color = Color.White, fontSize = 16.sp)
                }

                Text(
                    text = "اختيار الطائرة",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B))) {
                    Text(
                        text = "⭐ ${totalStars.value}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Airplane Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(airplanes) { plane ->
                    val isSelected = plane.id == selectedId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clickable {
                                soundManager.playClickSound(repository.isSoundEnabled())
                                if (plane.isUnlocked) {
                                    repository.setSelectedAirplaneId(plane.id)
                                    selectedId = plane.id
                                } else if (totalStars.value >= plane.price) {
                                    repository.addStars(-plane.price)
                                    repository.unlockAirplane(plane.id)
                                    repository.setSelectedAirplaneId(plane.id)
                                    selectedId = plane.id
                                    airplanes = repository.getAirplanes()
                                    totalStars.value = repository.getStars()
                                    soundManager.playVictorySound(repository.isSoundEnabled())
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFFFEE58) else Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Canvas(Modifier.fillMaxWidth(0.72f).height(56.dp)) {
                                drawCartoonPlane(
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    size = size.minDimension * 1.1f,
                                    timeSeconds = propellerPhase,
                                    primary = Color(plane.colorPrimary),
                                    secondary = Color(plane.colorSecondary)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = plane.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF5D4037) else Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "السرعة ${"%.1f".format(plane.speedMultiplier)}×",
                                fontSize = 11.sp,
                                color = Color(0xFF546E7A)
                            )
                            if (plane.isUnlocked) {
                                Text(
                                    text = if (isSelected) "✨ الطائرة الحالية" else "مفتوحة",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFFE53935) else Color(0xFF43A047)
                                )
                            } else {
                                Text(
                                    text = "🔒 فتح بـ ${plane.price} ⭐",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE53935)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
