package com.example.mobileaisemproject.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileaisemproject.components.CustomFloatingNavBar
import com.example.mobileaisemproject.ml.DiseaseRecord
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme

@Composable
fun DiseaseIDScreen(
    record: DiseaseRecord,
    remedyText: String,
    onDeleteClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
            ) {
                HorizontalDivider(thickness = 1.dp, color = Color.DarkGray)
                Text(
                    text = "Disease Name",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.confidence.ifBlank { "Confidence" },
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Text(
                    text = record.date.ifBlank { "Date" },
                    fontSize = 13.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 48.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large rounded grey image / scan preview placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD6D6D6))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Remedy",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Text(
                    text = remedyText.ifBlank { "Remedy to the disease description" },
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDeleteClick,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color(0xFF8A0000)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF1E1E),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DELETE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.6.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DiseaseIDScreenPreview() {
    MobileAISemProjectTheme {
        DiseaseIDScreen(
            record = DiseaseRecord(
                id = "1",
                name = "Disease Name",
                confidence = "Confidence",
                date = "Date"
            ),
            remedyText = "Remedy to the disease description",
            onDeleteClick = {},
            onTabSelected = {}
        )
    }
}