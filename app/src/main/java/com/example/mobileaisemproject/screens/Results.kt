package com.example.mobileaisemproject.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileaisemproject.ml.ScanResult
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme

@Composable
fun ResultsScreen(
    scanResult: ScanResult,
    onReturnHomeClick: () -> Unit
) {
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

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Disease Name & Confidence
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp, 5.dp)
                ) {
                    Text(
                        text = scanResult.diseaseName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Confidence: ${"%.1f".format(scanResult.confidence)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1:1 Cropped Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD6D6D6))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = scanResult.croppedImage.asImageBitmap(),
                    contentDescription = "Cropped Plant Leaf",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Remedy Section
            Text(
                text = "Remedy",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Text(
                    text = scanResult.remedy,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onReturnHomeClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Green,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Back to Home",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    val mockBitmap = remember {
        Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(AndroidColor.parseColor("#C0C0C0"))
        }
    }

    val sampleResult = ScanResult(
        diseaseName = "Tomato Early Blight",
        confidence = 94.6f,
        remedy = "Remove affected lower leaves immediately to prevent fungal spores from spreading. Improve air circulation around the plant, water at the base rather than overhead, and apply a copper-based fungicide every 7–10 days.",
        croppedImage = mockBitmap
    )

    MobileAISemProjectTheme {
        ResultsScreen(
            scanResult = sampleResult,
            onReturnHomeClick = {}
        )
    }
}