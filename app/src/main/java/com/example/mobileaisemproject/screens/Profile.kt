package com.example.mobileaisemproject.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileaisemproject.R
import com.example.mobileaisemproject.components.CustomFloatingNavBar
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme

data class Profile(
    val phone: String = "",
    val identifiedCount: Int = 0,
)

@Composable
fun ProfileScreen(
    profile: Profile,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSave: (phone: String, newPassword: String?, onDone: (String?) -> Unit) -> Unit = { _, _, onDone -> onDone(null) },
) {
    var selectedTab by remember { mutableIntStateOf(1) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var phoneDraft by rememberSaveable(inputs = arrayOf(profile.phone)) { mutableStateOf(profile.phone) }
    var passwordDraft by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var serverError by remember { mutableStateOf<String?>(null) }

    val phoneError = if (editing && submitted) Validate.phone(phoneDraft) else null
    val displayPhoneError = phoneError ?: serverError
    val passwordError =
        if (editing && submitted && passwordDraft.isNotEmpty()) Validate.password(passwordDraft) else null

    // Shared pill shape & height matching PrimaryButton
    val actionButtonShape = RoundedCornerShape(20.dp)

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

                        // Avatar
                        Image(
                            painter = painterResource(id = R.drawable.user_img),
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
                            error = displayPhoneError,
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

                        // Side-by-side action buttons:
                        // Default mode: [ Edit ]  [ Log Out ]
                        // Edit mode:    [ Save ]  [ Cancel ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Left Button: Edit / Save
                            Button(
                                onClick = {
                                    if (editing) {
                                        submitted = true
                                        val valid = (Validate.phone(phoneDraft) == null) &&
                                                (passwordDraft.isEmpty() || Validate.password(passwordDraft) == null)
                                        if (valid) {
                                            serverError = null
                                            onSave(Validate.cleanPhone(phoneDraft), passwordDraft.ifEmpty { null }) { errorMsg ->
                                                if (errorMsg != null) {
                                                    serverError = errorMsg
                                                } else {
                                                    editing = false
                                                    passwordDraft = ""
                                                    submitted = false
                                                    serverError = null
                                                }
                                            }
                                        }
                                    } else {
                                        phoneDraft = profile.phone
                                        passwordDraft = ""
                                        submitted = false
                                        serverError = null
                                        editing = true
                                    }
                                },
                                shape = actionButtonShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Green,
                                    contentColor = Color.White,
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (editing) Icons.Filled.Check else Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (editing) "Save" else "Edit",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // Right Button: Log Out (Black & White) / Cancel (Red & White)
                            Button(
                                onClick = {
                                    if (editing) {
                                        editing = false
                                        submitted = false
                                        passwordDraft = ""
                                        phoneDraft = profile.phone
                                        serverError = null
                                    } else {
                                        onLogoutClick()
                                    }
                                },
                                shape = actionButtonShape,
                                border = if (editing) BorderStroke(1.dp, Color(0xFF8A0000)) else null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editing) Color(0xFFFF1E1E) else Color.Black,
                                    contentColor = Color.White,
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                            ) {
                                Icon(
                                    imageVector = if (editing) Icons.Outlined.Close else Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (editing) "Cancel" else "Log Out",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
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
                phone = "0700 xxx xxx",
                identifiedCount = 5,
            ),
        )
    }
}
