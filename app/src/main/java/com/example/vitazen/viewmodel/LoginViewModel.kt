package com.example.vitazen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// XÓA DÒNG "private val ktx: Any"

/**
 * Lớp trạng thái (State) chứa toàn bộ dữ liệu cần thiết để vẽ giao diện LoginScreen.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Các sự kiện (Event) mà người dùng có thể thực hiện trên LoginScreen.
 */
sealed class LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent()
    data class PasswordChanged(val value: String) : LoginEvent()
    object LoginButtonClicked : LoginEvent()
    // Sự kiện mới: Nhận idToken từ UI sau khi đăng nhập Google thành công
    data class GoogleIdTokenReceived(val idToken: String) : LoginEvent()
}

/**
 * Các hiệu ứng một lần (Side Effect) mà ViewModel muốn View thực hiện.
 */
sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
}


class LoginViewModel : ViewModel() {

    // Quản lý trạng thái của giao diện bằng StateFlow
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    // Gửi hiệu ứng một lần đến View
    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    // Khởi tạo Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Xử lý các sự kiện từ View.
     */
    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _state.update { it.copy(email = event.value) }
            is LoginEvent.PasswordChanged -> _state.update { it.copy(password = event.value) }
            is LoginEvent.LoginButtonClicked -> loginWithEmailPassword()
            // Khi nhận được idToken, gọi hàm đăng nhập bằng Google
            is LoginEvent.GoogleIdTokenReceived -> signInWithGoogle(event.idToken)
        }
    }

    /**
     * Hàm mới để xác thực idToken của Google với Firebase.
     */
    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Tạo credential từ idToken
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                // Dùng credential để đăng nhập vào Firebase
                auth.signInWithCredential(credential).await()
                // Gửi hiệu ứng điều hướng khi thành công
                _effect.emit(LoginEffect.NavigateToHome)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message ?: "Lỗi đăng nhập Google không xác định.") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Hàm đăng nhập bằng Email/Password (đã cập nhật để dùng Firebase).
     */
    private fun loginWithEmailPassword() {
        if (state.value.email.isBlank() || state.value.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Vui lòng nhập email và mật khẩu.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                auth.signInWithEmailAndPassword(state.value.email, state.value.password).await()
                _effect.emit(LoginEffect.NavigateToHome)
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Email này chưa được đăng ký."
                    is FirebaseAuthInvalidCredentialsException -> "Sai mật khẩu. Vui lòng thử lại."
                    else -> "Email hoặc mật khẩu không đúng."
                }
                _state.update { it.copy(errorMessage = errorMessage) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // XÓA HÀM private fun FirebaseAuth.signInWithEmailAndPassword... Ở ĐÂY
}
