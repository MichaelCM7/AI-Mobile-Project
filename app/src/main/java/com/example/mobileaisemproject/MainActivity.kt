package com.example.mobileaisemproject

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.mobileaisemproject.ml.DiseaseRecord
import com.example.mobileaisemproject.screens.HomeScreen
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.WHITE,
                AndroidColor.WHITE
            )
        )
        setContent {
            MobileAISemProjectTheme {
                val records = remember { mutableStateListOf<DiseaseRecord>() }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Master Backdrop
                    Image(
                        painter = painterResource(id = R.drawable.bg_plant_produce),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // App Screens
                    HomeScreen(
                        records = records,
                        onCameraClick = { /* TODO: Navigate to CameraScreen */ },
                        onTabSelected = { /* TODO: Navigate to Profile */ }
                    )

                    // Solid white strip behind the status bar (battery, clock, notifications)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}