package com.example.abbasgame.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abbasgame.audio.SoundManager
import com.example.abbasgame.data.GameRepository
import com.example.abbasgame.model.Level
import com.example.abbasgame.model.World

@Composable
fun WorldLevelSelectionScreen(
    context: android.content.Context,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { GameRepository(context) }
    val soundManager = remember { SoundManager(context) }
    val worlds = remember { repository.getWorlds() }
    val levels = remember { repository.getLevels() }
    var selectedWorldId by remember { mutableIntStateOf(worlds.firstOrNull { it.isUnlocked }?.id ?: 1) }
    val selectedWorld = worlds.firstOrNull { it.id == selectedWorldId } ?: worlds.first()
    val worldLevels = levels.filter { it.worldId == selectedWorldId }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(selectedWorld.skyTopColor), Color(selectedWorld.skyBottomColor))
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        soundManager.playClickSound(repository.isSoundEnabled())
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.24f))
                ) {
                    Text("رجوع", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("خريطة المغامرات", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(selectedWorld.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                }
                Surface(color = Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(22.dp)) {
                    Text(
                        "⭐ ${repository.getStars()}",
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(worlds) { world ->
                    WorldMapCard(
                        world = world,
                        selected = world.id == selectedWorldId,
                        onClick = {
                            soundManager.playClickSound(repository.isSoundEnabled())
                            selectedWorldId = world.id
                        }
                    )
                }
            }

            Surface(
                color = Color.White.copy(alpha = 0.82f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(selectedWorld.description, color = Color(0xFF31556C), fontWeight = FontWeight.SemiBold)
                    Text(
                        if (selectedWorld.isUnlocked) "اختر محطة على مسار الرحلة" else "أكمل المراحل السابقة لفتح هذا العالم",
                        color = Color(0xFF54768A),
                        fontSize = 12.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(worldLevels, key = { it.id }) { level ->
                    MapLevelRow(
                        level = level,
                        world = selectedWorld,
                        onClick = {
                            soundManager.playClickSound(repository.isSoundEnabled())
                            if (level.isUnlocked && selectedWorld.isUnlocked) onSelectLevel(level.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldMapCard(world: World, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(190.dp)
            .height(86.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color.White else Color.White.copy(alpha = 0.68f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 8.dp else 2.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(world.name, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(world.skyTopColor), maxLines = 1)
            Text(
                if (world.isUnlocked) "عالم مفتوح" else "مغلق • أكمل التقدم",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (world.isUnlocked) Color(0xFF388E3C) else Color(0xFF7B8790)
            )
        }
    }
}

@Composable
private fun MapLevelRow(level: Level, world: World, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(54.dp).height(86.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                drawLine(
                    color = Color.White.copy(alpha = 0.72f),
                    start = androidx.compose.ui.geometry.Offset(centerX, 0f),
                    end = androidx.compose.ui.geometry.Offset(centerX, size.height),
                    strokeWidth = 5.dp.toPx()
                )
                drawCircle(
                    color = if (level.isUnlocked && world.isUnlocked) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.7f),
                    radius = 13.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(centerX, size.height / 2f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(centerX, size.height / 2f)
                )
            }
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .height(78.dp)
                .clickable(enabled = level.isUnlocked && world.isUnlocked, onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (level.isUnlocked && world.isUnlocked) Color.White.copy(alpha = 0.94f)
                else Color.White.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (level.isUnlocked) 5.dp else 1.dp)
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(level.title, color = Color(world.skyTopColor), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(level.gameMode.title, color = Color(0xFF526A78), fontSize = 12.sp)
                }
                Text(
                    if (level.isUnlocked && world.isUnlocked) "⭐ ${level.starsEarned}/3" else "مقفلة",
                    color = if (level.isUnlocked && world.isUnlocked) Color(0xFFB77900) else Color(0xFF7A858C),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
