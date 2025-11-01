package com.example.vitazen.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitazen.R
import com.example.vitazen.ui.theme.VitaZenYellow
import com.example.vitazen.viewmodel.LoginEffect
import com.example.vitazen.viewmodel.LoginEvent
import com.example.vitazen.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit
) {
    val state = viewModel.state

    // Lắng nghe các hiệu ứng một lần từ ViewModel để điều hướng
    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    // Cấu trúc giao diện chính
    Box(modifier = Modifier.fillMaxSize()) {
        // Lớp nền
        AuthBackground()

        // Lớp nội dung
        LoginForm(
            state = state,
            onEvent = viewModel::handleEvent // Truyền trực tiếp tham chiếu đến hàm handleEvent
        )

        // Lớp hiển thị loading
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

/**
 * Composable riêng cho phần nền và lớp phủ tối.
 * Có thể tái sử dụng cho màn hình Đăng ký sau này.
 */
@Composable
private fun AuthBackground() {
    Image(
        painter = painterResource(id = R.drawable.welcome),
        contentDescription = "Ảnh nền",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
    )
}

/**
 * Composable chứa toàn bộ các thành phần của form đăng nhập.
 */
@Composable
private fun LoginForm(
    state: com.example.vitazen.viewmodel.LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp), // Giảm padding dọc
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tiêu đề
        Text(
            text = "Đăng Nhập",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Chào mừng bạn trở lại!",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Các ô nhập liệu
        EmailTextField(
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            isError = state.errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            isError = state.errorMessage != null
        )

        // Hiển thị lỗi
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Các nút hành động
        Button(
            onClick = { onEvent(LoginEvent.LoginButtonClicked) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitaZenYellow,
                contentColor = Color.Black
            ),
            enabled = !state.isLoading
        ) {
            Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        GoogleSignInButton(onClick = { /* TODO: Xử lý logic đăng nhập Google sau */ })

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* TODO: Điều hướng đến màn hình đăng ký */ }) {
            Text("Chưa có tài khoản? Đăng ký ngay", color = VitaZenYellow)
        }
    }
}

/**
 * Composable riêng cho ô nhập Email.
 */
@Composable
private fun EmailTextField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        isError = isError,
        colors = authTextFieldColors()
    )
}

/**
 * Composable riêng cho ô nhập Mật khẩu.
 */
@Composable
private fun PasswordTextField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Mật khẩu") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = isError,
        colors = authTextFieldColors()
    )
}

/**
 * Composable riêng cho nút đăng nhập bằng Google.
 */
@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Google Icon",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Đăng nhập với Google",
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Hàm tiện ích cung cấp màu sắc tùy chỉnh cho các TextField xác thực.
 */
@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = VitaZenYellow,
    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
    focusedLabelColor = VitaZenYellow,
    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
    cursorColor = VitaZenYellow,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error
)
