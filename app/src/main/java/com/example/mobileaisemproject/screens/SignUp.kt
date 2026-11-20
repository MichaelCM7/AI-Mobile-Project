package com.example.mobileaisemproject.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileaisemproject.ui.theme.MobileAISemProjectTheme

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AppColors {
    val Green = Color(0xFF006400)
    val Ink = Color.Black
    val Dark = Color.DarkGray
    val Gray = Color(0xFFD6D6D6)
    val Error = Color(0xFFBA1A1A)
}

object Validate {
    fun phone(phone: String): String? {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) return "Phone number cannot be empty"
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length < 9) return "Please enter a valid phone number"
        return null
    }

    fun password(password: String): String? {
        if (password.isEmpty()) return "Password cannot be empty"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    fun confirm(password: String, confirm: String): String? {
        if (confirm.isEmpty()) return "Please confirm your password"
        if (password != confirm) return "Passwords do not match"
        return null
    }

    fun cleanPhone(phone: String): String {
        return phone.trim().filter { it.isDigit() || it == '+' }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Green,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFA5D6A7),
            disabledContentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    error: String? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.Ink
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            trailingIcon = if (isPassword && enabled) {
                {
                    val image = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = description,
                            tint = AppColors.Dark
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Green,
                unfocusedBorderColor = AppColors.Dark,
                errorBorderColor = AppColors.Error,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = AppColors.Gray.copy(alpha = 0.25f),
                disabledBorderColor = AppColors.Dark.copy(alpha = 0.5f),
                disabledTextColor = AppColors.Ink,
                focusedTextColor = AppColors.Ink,
                unfocusedTextColor = AppColors.Ink
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = AppColors.Error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun SwitchAuthRow(
    prompt: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prompt,
            fontSize = 14.sp,
            color = AppColors.Ink
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = actionText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Green,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(
                    role = Role.Button,
                    onClick = onActionClick
                )
                .padding(2.dp)
        )
    }
}

@Composable
fun AuthLayout(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, Color.DarkGray),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Ink,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSignUp: (phone: String, password: String, onResult: (AuthResult) -> Unit) -> Unit = { _, _, onResult -> onResult(AuthResult.Success) },
    onGoToSignIn: () -> Unit = {},
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var serverError by remember { mutableStateOf<String?>(null) }

    AuthLayout(
        title = "Sign Up",
        modifier = modifier
    ) {
        LabeledField(
            label = "Phone",
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Phone",
            keyboardType = KeyboardType.Phone,
            error = if (submitted) Validate.phone(phone) else null,
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            isPassword = true,
            error = if (submitted) Validate.password(password) else null,
        )
        Spacer(Modifier.height(16.dp))
        LabeledField(
            label = "Confirm Password",
            value = confirm,
            onValueChange = { confirm = it },
            placeholder = "Confirm Password",
            isPassword = true,
            imeAction = ImeAction.Done,
            error = if (submitted) Validate.confirm(password, confirm) else null,
        )
        serverError?.let {
            Text(
                text = it,
                color = AppColors.Error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        SwitchAuthRow(
            prompt = "Already have an account?",
            actionText = "Sign In",
            onActionClick = onGoToSignIn
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Sign Up",
            enabled = !busy,
            onClick = {
                submitted = true
                val valid = Validate.phone(phone) == null &&
                        Validate.password(password) == null &&
                        Validate.confirm(password, confirm) == null
                if (valid) {
                    busy = true
                    serverError = null
                    onSignUp(Validate.cleanPhone(phone), password) { result ->
                        busy = false
                        if (result is AuthResult.Error) serverError = result.message
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    MobileAISemProjectTheme {
        SignUpScreen()
    }
}
