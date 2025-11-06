package com.example.vitazen.ui.register

import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitazen.ui.login.AuthBackground // Tái sử dụng AuthBackground từ LoginScreen
import com.example.vitazen.ui.login.authTextFieldColors // Tái sử dụng authTextFieldColors
import com.example.vitazen.ui.theme.VitaZenYellow
import com.example.vitazen.viewmodel.RegisterEffect
import com.example.vitazen.viewmodel.RegisterEvent
import com.example.vitazen.viewmodel.RegisterState
import com.example.vitazen.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit // Thêm hành động để quay lại
) {
    val state = viewModel.state

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
            isError = state.errorMessage != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = authTextFieldColors(),
            isError = state.errorMessage != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
            label = { Text("Xác nhận mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = authTextFieldColors(),
            isError = state.errorMessage?.contains("khớp") == true // Chỉ báo lỗi khi lỗi liên quan đến mật khẩu
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
