package com.example.abbasgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun SettingsScreen(
    context: android.content.Context,
    onBack: () -> Unit
) {
    val repository = remember { GameRepository(context) }
    val soundManager = remember { SoundManager(context) }

    var musicEnabled by remember { mutableStateOf(repository.isMusicEnabled()) }
    var musicVolume by remember { mutableFloatStateOf(repository.getMusicVolume()) }
    var effectsVolume by remember { mutableFloatStateOf(repository.getEffectsVolume()) }
    var soundEnabled by remember { mutableStateOf(repository.isSoundEnabled()) }
    var voiceEnabled by remember { mutableStateOf(repository.isVoiceEnabled()) }
    var currentDifficulty by remember { mutableStateOf(repository.getDifficulty()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF78909C), Color(0xFF546E7A), Color(0xFF37474F))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
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
                        soundManager.playClickSound(soundEnabled)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Text(text = "⬅️ رجوع", color = Color.White)
                }

                Text(text = "⚙️ الإعدادات", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.width(60.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎵 الموسيقى الخلفية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = {
                                musicEnabled = it
                                repository.setMusicEnabled(it)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔔 المؤثرات الصوتية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                repository.setSoundEnabled(it)
                                soundManager.playClickSound(it)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🗣️ التعليق الصوتي", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                        Switch(
                            checked = voiceEnabled,
                            onCheckedChange = {
                                voiceEnabled = it
                                repository.setVoiceEnabled(it)
                            }
                        )
                    }

                    HorizontalDivider()

                    Text("مستوى الموسيقى  ${(musicVolume * 100).toInt()}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                    Slider(
                        value = musicVolume,
                        onValueChange = {
                            musicVolume = it
                            repository.setMusicVolume(it)
                            soundManager.setMusicVolume(it)
                        },
                        valueRange = 0f..1f
                    )

                    Text("مستوى المؤثرات  ${(effectsVolume * 100).toInt()}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                    Slider(
                        value = effectsVolume,
                        onValueChange = {
                            effectsVolume = it
                            repository.setEffectsVolume(it)
                            soundManager.setEffectsVolume(it)
                        },
                        valueRange = 0f..1f
                    )

                    Text(text = "🎮 مستوى صعوبة اللعبة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("سهل", "متوسط", "صعب").forEach { diff ->
                            val isSelected = currentDifficulty == diff
                            Button(
                                onClick = {
                                    currentDifficulty = diff
                                    repository.setDifficulty(diff)
                                    soundManager.playClickSound(soundEnabled)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF2E7D32) else Color(0xFFB0BEC5)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(text = diff, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
