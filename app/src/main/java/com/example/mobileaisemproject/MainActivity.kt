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
import com.example.mobileaisemproject.data.local.AppDatabase
import com.example.mobileaisemproject.data.local.HistoryEntity
import com.example.mobileaisemproject.data.local.ImageStorage
import com.example.mobileaisemproject.data.local.PasswordSecurity
import com.example.mobileaisemproject.data.local.SessionManager
import com.example.mobileaisemproject.data.local.UserEntity
import com.example.mobileaisemproject.data.local.toDiseaseRecord
import com.example.mobileaisemproject.ml.DiseaseRecord
import com.example.mobileaisemproject.ml.ScanResult
import com.example.mobileaisemproject.screens.AuthResult
import com.example.mobileaisemproject.screens.CameraScreen
import com.example.mobileaisemproject.screens.DiseaseIDScreen
import com.example.mobileaisemproject.screens.HomeScreen
import com.example.mobileaisemproject.screens.Profile
import com.example.mobileaisemproject.screens.ProfileScreen
import com.example.mobileaisemproject.screens.ResultsScreen
import com.example.mobileaisemproject.screens.SignInScreen
import com.example.mobileaisemproject.screens.SignUpScreen
import com.example.mobileaisemproject.screens.Validate
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme
import kotlinx.coroutines.launch

private enum class AppScreen {
    SIGN_IN, SIGN_UP, HOME, CAMERA, RESULTS, PROFILE, DISEASE_ID
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
                val context = applicationContext
                val db = remember { AppDatabase.getInstance(context) }
                val userDao = remember { db.userDao() }
                val historyDao = remember { db.historyDao() }
                val sessionManager = remember { SessionManager(context) }
                val coroutineScope = rememberCoroutineScope()

                var currentUserId by remember { mutableLongStateOf(sessionManager.currentUserId) }
                var currentScreen by remember { mutableStateOf(AppScreen.SIGN_IN) }
                var isInitializing by remember { mutableStateOf(true) }

                var latestScanResult by remember { mutableStateOf<ScanResult?>(null) }
                var selectedRecord by remember { mutableStateOf<DiseaseRecord?>(null) }

                // Check stored session on app launch
                LaunchedEffect(Unit) {
                    val savedUserId = sessionManager.currentUserId
                    if (savedUserId != SessionManager.NO_USER_ID) {
                        val user = userDao.getUserById(savedUserId)
                        if (user != null) {
                            currentUserId = savedUserId
                            currentScreen = AppScreen.HOME
                        } else {
                            sessionManager.clearSession()
                            currentUserId = SessionManager.NO_USER_ID
                            currentScreen = AppScreen.SIGN_IN
                        }
                    } else {
                        currentScreen = AppScreen.SIGN_IN
                    }
                    isInitializing = false
                }

                // Observe logged in user's scan history and count from Room
                val historyEntities by historyDao.observeHistoryForUser(currentUserId)
                    .collectAsState(initial = emptyList())

                val records = remember(historyEntities) {
                    historyEntities.map { it.toDiseaseRecord() }
                }

                val userEntity by userDao.observeUserById(currentUserId)
                    .collectAsState(initial = null)

                val scanCount by historyDao.observeScanCount(currentUserId)
                    .collectAsState(initial = 0)

                val currentProfile = remember(userEntity, scanCount) {
                    Profile(
                        phone = userEntity?.phone ?: "",
                        identifiedCount = scanCount
                    )
                }

                BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.SIGN_IN && !isInitializing) {
                    when (currentScreen) {
                        AppScreen.SIGN_UP -> currentScreen = AppScreen.SIGN_IN
                        else -> currentScreen = AppScreen.HOME
                    }
                }

                if (!isInitializing) {
                    // Transparent Box lets your green leafy background show through
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            AppScreen.SIGN_IN -> {
                                SignInScreen(
                                    onSignInClick = { phone, password, onResult ->
                                        coroutineScope.launch {
                                            val cleanPhone = Validate.cleanPhone(phone)
                                            val user = userDao.getUserByPhone(cleanPhone)
                                            if (user == null) {
                                                onResult(AuthResult.Error("No account found with this phone number"))
                                            } else {
                                                val verified = PasswordSecurity.verifyPassword(password, user.password)
                                                if (!verified) {
                                                    onResult(AuthResult.Error("Incorrect password"))
                                                } else {
                                                    sessionManager.currentUserId = user.id
                                                    currentUserId = user.id
                                                    onResult(AuthResult.Success)
                                                    currentScreen = AppScreen.HOME
                                                }
                                            }
                                        }
                                    },
                                    onSignUpClick = {
                                        currentScreen = AppScreen.SIGN_UP
                                    }
                                )
                            }

                            AppScreen.SIGN_UP -> {
                                SignUpScreen(
                                    onSignUp = { phone, password, onResult ->
                                        coroutineScope.launch {
                                            val cleanPhone = Validate.cleanPhone(phone)
                                            val existing = userDao.getUserByPhone(cleanPhone)
                                            if (existing != null) {
                                                onResult(AuthResult.Error("An account with this phone number already exists"))
                                            } else {
                                                val hashedPassword = PasswordSecurity.hashPassword(password)
                                                val newUserId = userDao.registerUser(
                                                    UserEntity(
                                                        phone = cleanPhone,
                                                        password = hashedPassword
                                                    )
                                                )
                                                sessionManager.currentUserId = newUserId
                                                currentUserId = newUserId
                                                onResult(AuthResult.Success)
                                                currentScreen = AppScreen.HOME
                                            }
                                        }
                                    },
                                    onGoToSignIn = {
                                        currentScreen = AppScreen.SIGN_IN
                                    }
                                )
                            }

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
                                        coroutineScope.launch {
                                            val savedPath = ImageStorage.saveCroppedBitmap(context, result.croppedImage)
                                            val scanEntity = HistoryEntity(
                                                userId = currentUserId,
                                                image = savedPath,
                                                issueName = result.diseaseName,
                                                confidence = result.confidence.toDouble(),
                                                date = System.currentTimeMillis(),
                                                remedy = result.remedy
                                            )
                                            historyDao.insertScan(scanEntity)
                                            currentScreen = AppScreen.RESULTS
                                        }
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
                                    profile = currentProfile,
                                    onTabSelected = { index ->
                                        if (index == 0) currentScreen = AppScreen.HOME
                                    },
                                    onSave = { newPhone, newPassword, onDone ->
                                        coroutineScope.launch {
                                            val cleanPhone = Validate.cleanPhone(newPhone)
                                            val existing = userDao.getUserByPhone(cleanPhone)
                                            if (existing != null && existing.id != currentUserId) {
                                                onDone("Phone number is already in use by another account")
                                            } else {
                                                if (!newPassword.isNullOrEmpty()) {
                                                    val hashed = PasswordSecurity.hashPassword(newPassword)
                                                    userDao.updatePhoneAndPassword(currentUserId, cleanPhone, hashed)
                                                } else {
                                                    userDao.updatePhone(currentUserId, cleanPhone)
                                                }
                                                onDone(null)
                                            }
                                        }
                                    },
                                    onLogoutClick = {
                                        sessionManager.clearSession()
                                        currentUserId = SessionManager.NO_USER_ID
                                        latestScanResult = null
                                        selectedRecord = null
                                        currentScreen = AppScreen.SIGN_IN
                                    }
                                )
                            }

                            AppScreen.DISEASE_ID -> {
                                selectedRecord?.let { record ->
                                    DiseaseIDScreen(
                                        record = record,
                                        onDeleteClick = {
                                            coroutineScope.launch {
                                                val scanId = record.id.toLongOrNull()
                                                if (scanId != null) {
                                                    historyDao.deleteScanById(scanId)
                                                }
                                                ImageStorage.deleteBitmapFile(record.imagePath)
                                                selectedRecord = null
                                                currentScreen = AppScreen.HOME
                                            }
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
}
