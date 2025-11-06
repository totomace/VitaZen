package com.example.vitazen.ui.register

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitazen.ui.login.AuthBackground
import com.example.vitazen.ui.login.authTextFieldColors
import com.example.vitazen.ui.theme.VitaZenYellow
import com.example.vitazen.viewmodel.EmailVerificationState
import com.example.vitazen.viewmodel.RegisterEffect
import com.example.vitazen.viewmodel.RegisterEvent
import com.example.vitazen.viewmodel.RegisterState
import com.example.vitazen.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthBackground()

        RegisterForm(
            state = state,
            onEvent = viewModel::handleEvent
        )

        // Nút quay lại
        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("Quay lại", color = Color.White)
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun RegisterForm(state: RegisterState, onEvent: (RegisterEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            // Thêm verticalScroll để tránh tràn layout trên màn hình nhỏ
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tạo tài khoản",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Bắt đầu hành trình của bạn với VitaZen",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Username
        OutlinedTextField(
            value = state.username,
            onValueChange = { onEvent(RegisterEvent.UsernameChanged(it)) },
            label = { Text("Tên người dùng") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = authTextFieldColors(),
            isError = state.errorMessage != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = authTextFieldColors(),
            isError = state.emailVerificationState == EmailVerificationState.INVALID,
            trailingIcon = {
                when (state.emailVerificationState) {
                    EmailVerificationState.VERIFYING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    EmailVerificationState.VALID -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Email hợp lệ",
                            tint = Color.Green
                        )
                    }
                    else -> { /* IDLE hoặc INVALID không hiện gì */ }
                }
            },
            supportingText = {
                state.emailValidationResult?.let { result ->
                    when {
                        result.errorMessage != null -> {
                            Text(
                                text = result.errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        result.warningMessage != null -> {
                            Text(
                                text = result.warningMessage,
                                color = Color.Yellow.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password
        var passwordVisible by remember { mutableStateOf(false) }
        val isPasswordValid = state.password.length >= 6
        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = authTextFieldColors(),
            isError = state.errorMessage != null,
            trailingIcon = {
                Row {
                    // Dấu tích xanh
                    if (isPasswordValid && state.errorMessage == null && state.password.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mật khẩu hợp lệ",
                            tint = Color.Green,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    // Icon con mắt
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        val isConfirmPasswordValid = state.confirmPassword.isNotBlank() && state.password == state.confirmPassword
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
            label = { Text("Xác nhận mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = authTextFieldColors(),
            isError = state.errorMessage?.contains("khớp") == true,
            trailingIcon = {
                Row {
                    // Dấu tích xanh
                    if (isConfirmPasswordValid && state.errorMessage?.contains("khớp") != true) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mật khẩu khớp",
                            tint = Color.Green,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    // Icon con mắt
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onEvent(RegisterEvent.RegisterButtonClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitaZenYellow,
                contentColor = Color.Black
            ),
            enabled = !state.isLoading
        ) {
            Text("Đăng ký", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
