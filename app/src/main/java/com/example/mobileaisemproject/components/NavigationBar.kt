package com.example.mobileaisemproject.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CustomFloatingNavBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.Black, CircleShape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPillItem(
                icon = Icons.Outlined.Home,
                contentDescription = "Home",
                isSelected = selectedIndex == 0,
                onClick = { onSelect(0) }
            )
            NavPillItem(
                icon = Icons.Outlined.Person,
                contentDescription = "Profile",
                isSelected = selectedIndex == 1,
                onClick = { onSelect(1) }
            )
        }
    }
}

@Composable
private fun NavPillItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFF006400)
    } else {
        Color.Transparent
    }

    val borderModifier = if (!isSelected) {
        Modifier.border(1.dp, Color.DarkGray, CircleShape)
    } else {
        Modifier
    }

    val tintModifier = if (isSelected) {
        Color.White
    } else {
        Color.Black
    }

    Box(
        modifier = Modifier
            .width(96.dp)
            .height(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintModifier,
            modifier = Modifier.size(26.dp)
        )
    }
}