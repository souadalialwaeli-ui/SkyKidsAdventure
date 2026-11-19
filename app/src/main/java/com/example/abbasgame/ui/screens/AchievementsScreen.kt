package com.example.abbasgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

@Composable
fun AchievementsScreen(
    context: android.content.Context,
    onBack: () -> Unit
) {
    val repository = remember { GameRepository(context) }
    val soundManager = remember { SoundManager(context) }
    val achievements = remember { repository.getAchievements() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5), Color(0xFF303F9F))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    Text(text = "⬅️ رجوع", color = Color.White)
                }

                Text(text = "🏆 الإنجازات والأوسمة", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.width(60.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { ach ->
                    Card(
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = ach.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = ach.description, fontSize = 14.sp, color = Color.Gray)
                            }
                            Text(
                                text = if (ach.isUnlocked) "✅ مكتمل" else "🔒 قفل",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) Color(0xFF43A047) else Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}
