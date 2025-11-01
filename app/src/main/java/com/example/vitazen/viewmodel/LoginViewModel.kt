package com.example.vitazen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Lớp trạng thái (State) chứa toàn bộ dữ liệu cần thiết để vẽ giao diện LoginScreen.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false, // Trạng thái đang tải (ví dụ: khi xác thực)
    val errorMessage: String? = null // Thông báo lỗi nếu có
)

/**
 * Các sự kiện (Event) mà người dùng có thể thực hiện trên LoginScreen.
 */
sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object LoginButtonClicked : LoginEvent()
}

/**
 * Các hiệu ứng một lần (Side Effect) mà ViewModel muốn View thực hiện.
 */
sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
}

class LoginViewModel : ViewModel() {

    // Quản lý trạng thái của giao diện
    var state by mutableStateOf(LoginState())
        private set

    // Gửi hiệu ứng một lần đến View
    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * Xử lý các sự kiện từ View.
     */
    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                state = state.copy(email = event.email)
            }
            is LoginEvent.PasswordChanged -> {
                state = state.copy(password = event.password)
            }
            is LoginEvent.LoginButtonClicked -> {
                login()
            }
        }
    }

    private fun login() {
        // Chạy coroutine để không chặn luồng chính
        viewModelScope.launch {
            // Bắt đầu trạng thái loading
            state = state.copy(isLoading = true, errorMessage = null)

            // Giả lập việc gọi API hoặc kiểm tra database
            delay(2000) // Giả lập độ trễ mạng 2 giây

            // Logic xác thực đơn giản
            if (state.email.isNotBlank() && state.password == "123456") {
                // Đăng nhập thành công, gửi hiệu ứng điều hướng
                _effect.emit(LoginEffect.NavigateToHome)
            } else {
                // Đăng nhập thất bại, cập nhật trạng thái lỗi
                state = state.copy(errorMessage = "Email hoặc mật khẩu không đúng.")
            }

            // Kết thúc trạng thái loading
            state = state.copy(isLoading = false)
        }
    }
}
