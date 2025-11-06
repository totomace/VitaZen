package com.example.vitazen.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.User
import com.example.vitazen.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * State cho RegisterScreen
 */
data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Events từ UI
 */
sealed class RegisterEvent {
    data class UsernameChanged(val username: String) : RegisterEvent()
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent()
    object RegisterButtonClicked : RegisterEvent()
}

/**
 * Side effects
 */
sealed class RegisterEffect {
    object NavigateToHome : RegisterEffect()
}

/**
 * ViewModel tối ưu với Firebase Authentication + Room Database
 */
class RegisterViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository? = null // Sẽ inject từ MainActivity
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect = _effect.asSharedFlow()

    fun handleEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.UsernameChanged -> {
                _state.update { it.copy(username = event.username, errorMessage = null) }
            }
            is RegisterEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, errorMessage = null) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, errorMessage = null) }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, errorMessage = null) }
            }
            is RegisterEvent.RegisterButtonClicked -> register()
        }
    }

    private fun register() {
        // Tránh double-submit
        if (_state.value.isLoading) return

        val username = _state.value.username.trim()
        val email = _state.value.email.trim()
        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword

        // Validation
        when {
            username.isBlank() -> {
                _state.update { it.copy(errorMessage = "Vui lòng nhập tên người dùng.") }
                return
            }
            username.length < 3 -> {
                _state.update { it.copy(errorMessage = "Tên người dùng phải có ít nhất 3 ký tự.") }
                return
            }
            email.isBlank() -> {
                _state.update { it.copy(errorMessage = "Vui lòng nhập email.") }
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _state.update { it.copy(errorMessage = "Email không hợp lệ.") }
                return
            }
            password.isBlank() -> {
                _state.update { it.copy(errorMessage = "Vui lòng nhập mật khẩu.") }
                return
            }
            password.length < 6 -> {
                _state.update { it.copy(errorMessage = "Mật khẩu phải có ít nhất 6 ký tự.") }
                return
            }
            password != confirmPassword -> {
                _state.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp.") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Đăng ký với Firebase
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                
                // 2. Cập nhật display name (username)
                auth.currentUser?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                )?.await()
                
                // 3. Lưu user vào Room Database để dùng offline
                val firebaseUser = authResult.user
                if (firebaseUser != null && userRepository != null) {
                    val user = User(
                        uid = firebaseUser.uid,
                        email = email,
                        username = username,
                        profilePictureUrl = firebaseUser.photoUrl?.toString(),
                        createdAt = System.currentTimeMillis(),
                        lastLoginAt = System.currentTimeMillis()
                    )
                    userRepository.insertOrUpdateUser(user)
                }
                
                _effect.emit(RegisterEffect.NavigateToHome)
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthUserCollisionException -> "Email này đã được đăng ký."
                    is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn."
                    else -> e.message ?: "Đăng ký thất bại. Vui lòng thử lại."
                }
                _state.update { it.copy(errorMessage = errorMessage) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
