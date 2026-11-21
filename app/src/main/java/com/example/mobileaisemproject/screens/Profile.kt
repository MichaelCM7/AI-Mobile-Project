package com.example.mobileaisemproject.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileaisemproject.components.CustomFloatingNavBar
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme
import com.example.mobileaisemproject.R

data class Profile(
    val phone: String = "",
    val identifiedCount: Int = 0,
)

@Composable
fun ProfileScreen(
    profile: Profile,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit = {},
    onSave: (phone: String, newPassword: String?, onDone: () -> Unit) -> Unit = { _, _, onDone -> onDone() },
) {
    var selectedTab by remember { mutableIntStateOf(1) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var phoneDraft by rememberSaveable(inputs = arrayOf(profile.phone)) { mutableStateOf(profile.phone) }
    var passwordDraft by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val phoneError = if (editing && submitted) Validate.phone(phoneDraft) else null
    val passwordError =
        if (editing && submitted && passwordDraft.isNotEmpty()) Validate.password(passwordDraft) else null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
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
                    .padding(bottom = 16.dp),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, Color.DarkGray),
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        Text(
                            text = "Profile",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Ink,
                        )
                        Spacer(Modifier.height(16.dp))

                        // Avatar placeholder
                        Image(
                            painter = painterResource(id = R.drawable.user_img), // Replace with your exact drawable filename
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(CircleShape)
                                .border(1.dp, AppColors.Dark, CircleShape)
                        )

                        Spacer(Modifier.height(20.dp))

                        IdentifiedCard(count = profile.identifiedCount)
                        Spacer(Modifier.height(20.dp))

                        LabeledField(
                            label = "Phone",
                            value = if (editing) phoneDraft else profile.phone,
                            onValueChange = { phoneDraft = it },
                            placeholder = "Phone",
                            enabled = editing,
                            keyboardType = KeyboardType.Phone,
                            error = phoneError,
                        )
                        Spacer(Modifier.height(16.dp))
                        // The real password is never stored (only a hash), so it can't be shown, only replaced.
                        LabeledField(
                            label = "Password",
                            value = if (editing) passwordDraft else "",
                            onValueChange = { passwordDraft = it },
                            placeholder = if (editing) "New password (leave blank to keep)" else "••••••••",
                            isPassword = true,
                            enabled = editing,
                            imeAction = ImeAction.Done,
                            error = passwordError,
                        )
                        Spacer(Modifier.height(28.dp))

                        if (editing) {
                            PrimaryButton(
                                text = "Save",
                                icon = Icons.Filled.Check,
                                onClick = {
                                    submitted = true
                                    val valid = (Validate.phone(phoneDraft) == null) &&
                                            (passwordDraft.isEmpty() || Validate.password(passwordDraft) == null)
                                    if (valid) {
                                        onSave(Validate.cleanPhone(phoneDraft), passwordDraft.ifEmpty { null }) {
                                            editing = false
                                            passwordDraft = ""
                                            submitted = false
                                        }
                                    }
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    editing = false
                                    submitted = false
                                    passwordDraft = ""
                                    phoneDraft = profile.phone
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) {
                                Text("Cancel", color = AppColors.Ink)
                            }
                        } else {
                            PrimaryButton(
                                text = "Edit",
                                icon = Icons.Outlined.Edit,
                                onClick = {
                                    phoneDraft = profile.phone
                                    passwordDraft = ""
                                    submitted = false
                                    editing = true
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentifiedCard(count: Int) {
    val boxShape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Dark, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Diseases and Pests Identified",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.Ink,
        )
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .size(width = 96.dp, height = 48.dp)
                .clip(boxShape)
                .background(AppColors.Gray)
                .border(1.dp, AppColors.Dark, boxShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Ink,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MobileAISemProjectTheme {
        ProfileScreen(
            profile = Profile(
                phone = "+1 234 567 8900",
                identifiedCount = 5,
            ),
        )
    }
}
