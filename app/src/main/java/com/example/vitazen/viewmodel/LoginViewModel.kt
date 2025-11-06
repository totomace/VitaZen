package com.example.vitazen.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.User
import com.example.vitazen.model.repository.UserRepository
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

/**
 * State cho LoginScreen
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Events từ UI
 */
sealed class LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent()
    data class PasswordChanged(val value: String) : LoginEvent()
    object LoginButtonClicked : LoginEvent()
    data class GoogleIdTokenReceived(val idToken: String) : LoginEvent()
    data class ForgotPasswordClicked(val email: String) : LoginEvent()
}

/**
 * Side effects một lần
 */
sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
}

/**
 * ViewModel tối ưu với dependency injection, validation, và Room Database
 */
class LoginViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository? = null // Sẽ inject từ MainActivity
) : ViewModel() {

    // Quản lý trạng thái của giao diện bằng StateFlow
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    // Gửi hiệu ứng một lần đến View
    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    /**
     * Xử lý events từ UI
     */
    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.value, errorMessage = null, successMessage = null) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.value, errorMessage = null, successMessage = null) }
            }
            is LoginEvent.LoginButtonClicked -> loginWithEmailPassword()
            is LoginEvent.GoogleIdTokenReceived -> signInWithGoogle(event.idToken)
            is LoginEvent.ForgotPasswordClicked -> sendPasswordResetEmail(event.email)
        }
    }

    /**
     * Đăng nhập với Google - tối ưu với validation + Room Database
     */
    private fun signInWithGoogle(idToken: String) {
        // Tránh gọi nhiều lần khi đang loading
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                
                // Lưu/Update user vào Room Database
                val firebaseUser = authResult.user
                if (firebaseUser != null && userRepository != null) {
                    // Kiểm tra xem user đã tồn tại chưa
                    val existingUser = userRepository.getUserById(firebaseUser.uid)
                    
                    if (existingUser != null) {
                        // User đã tồn tại -> update lastLoginAt
                        userRepository.updateLastLogin(firebaseUser.uid)
                    } else {
                        // User mới -> insert vào database
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            username = firebaseUser.displayName ?: "User",
                            profilePictureUrl = firebaseUser.photoUrl?.toString(),
                            createdAt = System.currentTimeMillis(),
                            lastLoginAt = System.currentTimeMillis()
                        )
                        userRepository.insertOrUpdateUser(user)
                    }
                }
                
                _effect.emit(LoginEffect.NavigateToHome)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(errorMessage = e.message ?: "Lỗi đăng nhập Google không xác định.") 
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Đăng nhập Email/Password - có validation + Room Database
     */
    private fun loginWithEmailPassword() {
        // Tránh gọi nhiều lần khi đang loading
        if (_state.value.isLoading) return

        val email = _state.value.email.trim()
        val password = _state.value.password

        // Validation
        when {
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
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                
                // Update lastLoginAt trong Room Database
                val firebaseUser = authResult.user
                if (firebaseUser != null && userRepository != null) {
                    userRepository.updateLastLogin(firebaseUser.uid)
                }
                
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
    
    /**
     * Gửi email reset mật khẩu
     */
    private fun sendPasswordResetEmail(email: String) {
        val trimmedEmail = email.trim()
        
        // Validation
        when {
            trimmedEmail.isBlank() -> {
                _state.update { it.copy(errorMessage = "Vui lòng nhập email.") }
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                _state.update { it.copy(errorMessage = "Email không hợp lệ.") }
                return
            }
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                auth.sendPasswordResetEmail(trimmedEmail).await()
                _state.update { 
                    it.copy(
                        successMessage = "Email khôi phục mật khẩu đã được gửi đến $trimmedEmail. Vui lòng kiểm tra hộp thư.",
                        errorMessage = null
                    ) 
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Email này chưa được đăng ký."
                    else -> "Không thể gửi email. Vui lòng thử lại sau."
                }
                _state.update { it.copy(errorMessage = errorMessage, successMessage = null) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
