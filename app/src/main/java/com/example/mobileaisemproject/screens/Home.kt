package com.example.mobileaisemproject.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileaisemproject.components.CustomFloatingNavBar
import com.example.mobileaisemproject.ml.DiseaseRecord
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme

@Composable
fun HomeScreen(
    records: List<DiseaseRecord>,
    onCameraClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onRecordClick: (DiseaseRecord) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White.copy(alpha = 0.8f))
            ) {
                HorizontalDivider(thickness = 1.dp, color = Color.DarkGray)
                Text(
                    text = "Home",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
        },
        bottomBar = {
            CustomFloatingNavBar(
                selectedIndex = selectedTab,
                onSelect = { index ->
                    selectedTab = index
                    onTabSelected(index)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (records.isEmpty()) {
                EmptyStateContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 4.dp,
                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        DiseaseRecordCard(
                            record = record,
                            onClick = { onRecordClick(record) }
                        )
                    }
                }
            }

            Surface(
                onClick = onCameraClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 24.dp,
                        bottom = innerPadding.calculateBottomPadding() + 20.dp
                    )
                    .size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD6D6D6),
                border = BorderStroke(1.dp, Color.DarkGray),
                shadowElevation = 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Scan Disease",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiseaseRecordCard(
    record: DiseaseRecord,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD6D6D6))
            ) {
                if (record.image != null) {
                    androidx.compose.foundation.Image(
                        bitmap = record.image.asImageBitmap(),
                        contentDescription = record.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = record.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = record.confidence,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Text(
                    text = record.date,
                    fontSize = 13.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, Color.DarkGray)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Record of\nprevious diseases",
                    fontSize = 26.sp,
                    lineHeight = 34.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap the Camera\nButton To get started",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Populated Home Page")
@Composable
fun HomePagePopulatedPreview() {
    MobileAISemProjectTheme {
        val mockList = List(6) {
            DiseaseRecord("$it", "Name of Disease", "Confidence", "Date")
        }
        HomeScreen(
            records = mockList,
            onCameraClick = {},
            onTabSelected = {},
            onRecordClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State Home Page")
@Composable
fun HomePageEmptyPreview() {
    MobileAISemProjectTheme {
        HomeScreen(
            records = emptyList(),
            onCameraClick = {},
            onTabSelected = {}
        )
    }
}