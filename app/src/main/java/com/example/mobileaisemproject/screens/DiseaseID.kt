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
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileaisemproject.ml.DiseaseRecord
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme

@Composable
fun DiseaseIDScreen(
    record: DiseaseRecord,
    onDeleteClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Disease Name, Confidence & Recorded Date Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = record.name.ifBlank { "Disease Name" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.confidence.ifBlank { "Confidence" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        Text(
                            text = record.date.ifBlank { "Date" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1:1 Cropped Image Preview (with fallback placeholder if image is null)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD6D6D6))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (record.image != null) {
                    Image(
                        bitmap = record.image.asImageBitmap(),
                        contentDescription = "Saved Plant Leaf Scan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No Image Available",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

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
                    text = record.remedy.ifBlank { "Remedy description not available." },
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Evenly sized horizontal action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Record Action Button
                Button(
                    onClick = onDeleteClick,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF8A0000)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF1E1E),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Record",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Delete Record",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Back to Home Button
                Button(
                    onClick = onReturnHomeClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Green,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Back to Home",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiseaseIDScreenPreview() {
    val mockBitmap = remember {
        Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(AndroidColor.parseColor("#C0C0C0"))
        }
    }

    MobileAISemProjectTheme {
        DiseaseIDScreen(
            record = DiseaseRecord(
                id = "1",
                name = "Tomato Early Blight",
                confidence = "Confidence: 94.6%",
                date = "Nov 21, 2026",
                remedy = "Remove affected lower leaves immediately to prevent fungal spores from spreading. Improve air circulation around the plant, water at the base rather than overhead, and apply a copper-based fungicide every 7–10 days.",
                image = mockBitmap
            ),
            onDeleteClick = {},
            onReturnHomeClick = {}
        )
    }
}