package com.example.abbasgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.abbasgame.ui.navigation.GameNavigation
import com.example.abbasgame.ui.theme.ABBASGAMETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ABBASGAMETheme {
                GameNavigation()
            }
        }
    }
}
