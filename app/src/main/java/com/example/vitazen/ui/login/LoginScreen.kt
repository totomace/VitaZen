package com.example.vitazen.ui.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vitazen.R
import com.example.vitazen.ui.theme.VitaZenYellow
import com.example.vitazen.viewmodel.LoginEffect
import com.example.vitazen.viewmodel.LoginEvent
import com.example.vitazen.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Lắng nghe các hiệu ứng một lần (side-effect) từ ViewModel
    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    // --- PHẦN XỬ LÝ GOOGLE SIGN-IN ---

    // 1. Cấu hình các tùy chọn cho Google Sign-In, yêu cầu idToken từ server
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.web_client_id))
        .requestEmail()
        .build()

    // 2. Tạo Google Sign-In Client
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // 3. Tạo launcher để mở cửa sổ đăng nhập của Google và xử lý kết quả trả về
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Lấy tài khoản thành công
                val account = task.getResult(ApiException::class.java)!!
                // Lấy idToken từ tài khoản
                val idToken = account.idToken!!
                // Gửi idToken đến ViewModel để xác thực với Firebase
                viewModel.handleEvent(LoginEvent.GoogleIdTokenReceived(idToken))
            } catch (e: ApiException) {
                // Xử lý các lỗi có thể xảy ra
                Toast.makeText(context, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- GIAO DIỆN ---

    Box(modifier = Modifier.fillMaxSize()) {
        AuthBackground()
        LoginForm(
            state = state,
            onEvent = viewModel::handleEvent,
            onNavigateToRegister = onNavigateToRegister,
            // Thêm một lambda mới để xử lý việc nhấn nút Google
            onGoogleSignInClicked = {
                // Khi nút được nhấn, mở cửa sổ đăng nhập của Google
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        )

        // Hiển thị vòng xoay loading
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// CẬP NHẬT LoginForm để nhận thêm một lambda onGoogleSignInClicked
@Composable
private fun LoginForm(
    state: com.example.vitazen.viewmodel.LoginState,
    onEvent: (LoginEvent) -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignInClicked: () -> Unit
) {
    // State để hiển thị dialog quên mật khẩu
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tiêu đề
        Text(text = "Đăng Nhập", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "Chào mừng bạn trở lại!", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(48.dp))

        // Các ô nhập liệu
        EmailTextField(value = state.email, onValueChange = { onEvent(LoginEvent.EmailChanged(it)) }, isError = state.errorMessage != null)
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(value = state.password, onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) }, isError = state.errorMessage != null)

        // Nút quên mật khẩu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { showForgotPasswordDialog = true }) {
                Text("Quên mật khẩu?", color = VitaZenYellow, fontSize = 14.sp)
            }
        }

        // Hiển thị lỗi hoặc thành công
        if (state.errorMessage != null) {
            Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        }
        if (state.successMessage != null) {
            Text(text = state.successMessage, color = Color.Green, modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút đăng nhập bằng Email/Password
        Button(
            onClick = { onEvent(LoginEvent.LoginButtonClicked) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VitaZenYellow, contentColor = Color.Black),
            enabled = !state.isLoading
        ) {
            Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cập nhật nút Google để gọi đúng lambda
        GoogleSignInButton(onClick = onGoogleSignInClicked)

        Spacer(modifier = Modifier.height(16.dp))

        // Nút chuyển sang màn hình Đăng ký
        TextButton(onClick = onNavigateToRegister) {
            Text("Chưa có tài khoản? Đăng ký ngay", color = VitaZenYellow)
        }
    }
    
    // Dialog quên mật khẩu
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            currentEmail = state.email,
            onDismiss = { showForgotPasswordDialog = false },
            onSendResetEmail = { email ->
                onEvent(LoginEvent.ForgotPasswordClicked(email))
                showForgotPasswordDialog = false
            }
        )
    }
}

// --- CÁC COMPOSABLE PHỤ (KHÔNG THAY ĐỔI) ---
@Composable
fun AuthBackground() {
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

@Composable
private fun EmailTextField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        isError = isError, colors = authTextFieldColors()
    )
}

@Composable
private fun PasswordTextField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text("Mật khẩu") }, modifier = Modifier.fillMaxWidth(),
        singleLine = true, visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = isError, colors = authTextFieldColors()
    )
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google Icon", modifier = Modifier.size(24.dp), tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(12.dp))
        Text("Đăng nhập với Google", color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
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

@Composable
private fun ForgotPasswordDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSendResetEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf(currentEmail) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Quên mật khẩu", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Nhập email của bạn để nhận link khôi phục mật khẩu:")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSendResetEmail(email) },
                colors = ButtonDefaults.buttonColors(containerColor = VitaZenYellow, contentColor = Color.Black)
            ) {
                Text("Gửi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
