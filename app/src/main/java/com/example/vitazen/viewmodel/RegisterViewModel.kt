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

// Trạng thái cho màn hình đăng ký
data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// Các sự kiện người dùng có thể thực hiện
sealed class RegisterEvent {
    data class UsernameChanged(val username: String) : RegisterEvent()
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent()
    object RegisterButtonClicked : RegisterEvent()
}

// Hiệu ứng một lần (điều hướng)
sealed class RegisterEffect {
    object NavigateToHome : RegisterEffect()
}

class RegisterViewModel : ViewModel() {
    var state by mutableStateOf(RegisterState())
        private set

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect = _effect.asSharedFlow()

    fun handleEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.UsernameChanged -> state = state.copy(username = event.username)
            is RegisterEvent.EmailChanged -> state = state.copy(email = event.email)
            is RegisterEvent.PasswordChanged -> state = state.copy(password = event.password)
            is RegisterEvent.ConfirmPasswordChanged -> state = state.copy(confirmPassword = event.confirmPassword)
            is RegisterEvent.RegisterButtonClicked -> register()
        }
    }

    private fun register() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            delay(2000) // Giả lập gọi API

            if (state.password != state.confirmPassword) {
                state = state.copy(errorMessage = "Mật khẩu xác nhận không khớp.")
            } else if (state.username.isBlank() || state.email.isBlank() || state.password.isBlank()) {
                state = state.copy(errorMessage = "Vui lòng điền đầy đủ thông tin.")
            } else {
                // Đăng ký thành công
                _effect.emit(RegisterEffect.NavigateToHome)
            }

            state = state.copy(isLoading = false)
        }
    }
}
