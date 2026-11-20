package com.example.mobileaisemproject.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview as CameraPreviewUseCase
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mobileaisemproject.ml.PlantDiseaseClassifier
import com.example.mobileaisemproject.ml.ScanResult
import com.example.mobileaisemproject.ml.loadAndCropSquareBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun CameraScreen(
    onCloseClick: () -> Unit,
    onResultReady: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var trackingOffset by remember { mutableStateOf<Offset?>(null) }
    var cameraControl by remember { mutableStateOf<Camera?>(null) }

    val classifier = remember { PlantDiseaseClassifier(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraControl?.cameraControl?.enableTorch(false)
            classifier.close()
        }
    }

    // Keep physical torch synced with isFlashOn state
    LaunchedEffect(isFlashOn, cameraControl) {
        cameraControl?.cameraControl?.enableTorch(isFlashOn)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Helper to crop & run ONNX inference on any Uri (from camera or gallery)
    fun processImageUri(uri: Uri) {
        isAnalyzing = true
        coroutineScope.launch(Dispatchers.IO) {
            val croppedSquare = loadAndCropSquareBitmap(context, uri)
            if (croppedSquare != null) {
                val result = classifier.classifyAndGetResult(croppedSquare)
                withContext(Dispatchers.Main) {
                    isAnalyzing = false
                    onResultReady(result)
                }
            } else {
                withContext(Dispatchers.Main) {
                    isAnalyzing = false
                    Toast.makeText(context, "Could not process image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            processImageUri(uri)
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                HorizontalDivider(thickness = 1.dp, color = Color.DarkGray)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = {
                        isFlashOn = false
                        onCloseClick()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Return to Home",
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(Color.White)
            ) {
                HorizontalDivider(thickness = 1.dp, color = Color.DarkGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Upload Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(enabled = !isAnalyzing) {
                            // Turn off flash if user chose Upload
                            if (isFlashOn) isFlashOn = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Upload,
                                    contentDescription = "Upload from Gallery",
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Upload",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // 2. Take Photo (Shutter) Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(enabled = !isAnalyzing && hasCameraPermission) {
                            isAnalyzing = true
                            captureSquarePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                onCaptured = { uri ->
                                    // Turn off flash right after shutter capture completes
                                    isFlashOn = false
                                    processImageUri(uri)
                                },
                                onError = { msg ->
                                    isFlashOn = false
                                    isAnalyzing = false
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD6D6D6))
                                .border(1.dp, Color.DarkGray, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Take Photo",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // 3. Flash Toggle Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(enabled = !isAnalyzing) {
                            isFlashOn = !isFlashOn
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFlashOn) Color(0xFFFFF59D) else Color.White,
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isFlashOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                    contentDescription = "Toggle Flash",
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Flash",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Instruction Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Text(
                    text = "Point Towards The Infected Part or\nPest to Identify Issue",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }

            // 1:1 Square Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD6D6D6))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        imageCapture = imageCapture,
                        onCameraReady = { cam -> cameraControl = cam },
                        onTapToFocus = { offset -> trackingOffset = offset }
                    )

                    trackingOffset?.let { offset ->
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (offset.x - 30.dp.toPx()).toInt(),
                                        (offset.y - 30.dp.toPx()).toInt()
                                    )
                                }
                                .size(60.dp)
                                .border(2.dp, Color.Yellow, RoundedCornerShape(8.dp))
                        )
                    }
                } else {
                    Text(
                        text = "Camera permission is required\nto scan leaves",
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }

                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Analyzing...",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    onCameraReady: (Camera) -> Unit,
    onTapToFocus: (Offset) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        val preview = CameraPreviewUseCase.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            val boundCamera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
            camera = boundCamera
            onCameraReady(boundCamera)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                onTapToFocus(offset)
                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    previewView.width.toFloat(),
                    previewView.height.toFloat()
                )
                val point = factory.createPoint(offset.x, offset.y)
                val action = FocusMeteringAction.Builder(point).build()
                camera?.cameraControl?.startFocusAndMetering(action)
            }
        }
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener({
                continuation.resume(future.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun captureSquarePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptured: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PlantDiseaseApp")
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val uri = outputFileResults.savedUri
                if (uri != null) {
                    onCaptured(uri)
                } else {
                    onError("Failed to save captured image")
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError("Capture error: ${exception.message}")
            }
        }
    )
}