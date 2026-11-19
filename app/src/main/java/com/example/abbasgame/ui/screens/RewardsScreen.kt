package com.example.abbasgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun RewardsScreen(
    context: android.content.Context,
    onBack: () -> Unit
) {
    val repository = GameRepository(context)
    val soundManager = SoundManager(context)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF7043), Color(0xFFF4511E), Color(0xFFD84315))
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

                Text(text = "🎁 المكافآت اليومية", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.width(60.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "⭐ مكافأة تسجيل الدخول ⭐", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "+50 نجمة و +20 عملة ذهبية!", fontSize = 16.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            soundManager.playVictorySound(repository.isSoundEnabled())
                            repository.addStars(50)
                            repository.addCoins(20)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(text = "استلم المكافأة الآن!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
