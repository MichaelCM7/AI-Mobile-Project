package com.example.mobileaisemproject

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mobileaisemproject.ml.DiseaseRecord
import com.example.mobileaisemproject.ml.ScanResult
import com.example.mobileaisemproject.screens.CameraScreen
import com.example.mobileaisemproject.screens.DiseaseIDScreen
import com.example.mobileaisemproject.screens.HomeScreen
import com.example.mobileaisemproject.screens.Profile
import com.example.mobileaisemproject.screens.ProfileScreen
import com.example.mobileaisemproject.screens.ResultsScreen
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private enum class AppScreen {
    HOME, CAMERA, RESULTS, PROFILE, DISEASE_ID
}

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
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                val records = remember { mutableStateListOf<DiseaseRecord>() }
                var latestScanResult by remember { mutableStateOf<ScanResult?>(null) }
                var selectedRecord by remember { mutableStateOf<DiseaseRecord?>(null) }

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = AppScreen.HOME
                }

                // Transparent Box lets your green leafy background show through
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                records = records,
                                onCameraClick = { currentScreen = AppScreen.CAMERA },
                                onTabSelected = { index ->
                                    if (index == 1) currentScreen = AppScreen.PROFILE
                                },
                                onRecordClick = { record ->
                                    selectedRecord = record
                                    currentScreen = AppScreen.DISEASE_ID
                                }
                            )
                        }

                        AppScreen.CAMERA -> {
                            CameraScreen(
                                onCloseClick = { currentScreen = AppScreen.HOME },
                                onResultReady = { result ->
                                    latestScanResult = result

                                    val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        .format(Date())
                                    records.add(
                                        0,
                                        DiseaseRecord(
                                            id = UUID.randomUUID().toString(),
                                            name = result.diseaseName,
                                            confidence = "Confidence: ${"%.1f".format(result.confidence)}%",
                                            date = today,
                                            remedy = result.remedy,
                                            image = result.croppedImage
                                        )
                                    )

                                    currentScreen = AppScreen.RESULTS
                                }
                            )
                        }

                        AppScreen.RESULTS -> {
                            latestScanResult?.let { result ->
                                ResultsScreen(
                                    scanResult = result,
                                    onReturnHomeClick = { currentScreen = AppScreen.HOME }
                                )
                            }
                        }

                        AppScreen.PROFILE -> {
                            ProfileScreen(
                                profile = Profile(
                                    identifiedCount = records.size
                                ),
                                onTabSelected = { index ->
                                    if (index == 0) currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.DISEASE_ID -> {
                            selectedRecord?.let { record ->
                                DiseaseIDScreen(
                                    record = record,
                                    onDeleteClick = {
                                        records.removeIf { it.id == record.id }
                                        selectedRecord = null
                                        currentScreen = AppScreen.HOME
                                    },
                                    onReturnHomeClick = {
                                        selectedRecord = null
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }
                        }
                    }

                    // Paints ONLY the top status bar strip (clock, signal, battery) solid white
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(Color.White)
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}