package com.example.vitazen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Lớp này đại diện cho các sự kiện (event) mà View (WelcomeScreen)
 * có thể gửi cho ViewModel.
 */
sealed class WelcomeEvent {
    /**
     * Sự kiện xảy ra khi người dùng nhấn nút "Bắt đầu ngay".
     */
    data object StartButtonClicked : WelcomeEvent()
}

/**
 * Lớp này đại diện cho các hiệu ứng một lần (side-effect) mà ViewModel
 * muốn View thực hiện, ví dụ như điều hướng hoặc hiển thị thông báo.
 */
sealed class WelcomeEffect {
    /**
     * Hiệu ứng yêu cầu View điều hướng đến màn hình Login.
     */
    data object NavigateToLogin : WelcomeEffect()
    
    /**
     * Hiệu ứng yêu cầu View điều hướng đến màn hình Home.
     */
    data object NavigateToHome : WelcomeEffect()
    
    /**
     * Hiệu ứng yêu cầu View điều hướng đến màn hình nhập tên.
     */
    data object NavigateToNameInput : WelcomeEffect()
}

/**
 * ViewModel cho màn hình WelcomeScreen.
 * Chịu trách nhiệm xử lý logic và quản lý trạng thái (nếu có).
 */
class WelcomeViewModel(
    private val userRepository: UserRepository? = null,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // _effect là một SharedFlow để gửi các hiệu ứng một lần từ ViewModel đến View.
    // Nó là private để chỉ ViewModel mới có thể gửi hiệu ứng.
    private val _effect = MutableSharedFlow<WelcomeEffect>()

    // effect là phiên bản public, chỉ đọc, để View có thể lắng nghe.
    val effect = _effect.asSharedFlow()

    /**
     * Hàm này được gọi bởi View để xử lý các sự kiện của người dùng.
     * @param event Sự kiện được gửi từ View (ví dụ: một cú nhấp chuột).
     */
    fun handleEvent(event: WelcomeEvent) {
        when (event) {
            is WelcomeEvent.StartButtonClicked -> {
                // Kiểm tra trạng thái đăng nhập trước khi navigate
                checkAuthenticationAndNavigate()
            }
        }
    }
    
    /**
     * Kiểm tra trạng thái đăng nhập và quyết định màn hình tiếp theo.
     */
    private fun checkAuthenticationAndNavigate() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            android.util.Log.d("WelcomeViewModel", "=== START CHECK AUTH ===")
            android.util.Log.d("WelcomeViewModel", "Current user: ${currentUser?.uid}")
            android.util.Log.d("WelcomeViewModel", "UserRepository: ${userRepository != null}")
            
            if (currentUser == null) {
                // Chưa đăng nhập -> đến màn hình Login
                android.util.Log.d("WelcomeViewModel", "❌ No user logged in -> LOGIN")
                triggerEffect(WelcomeEffect.NavigateToLogin)
            } else if (userRepository != null) {
                // Đã đăng nhập -> kiểm tra username
                val user = userRepository.getUserById(currentUser.uid)
                android.util.Log.d("WelcomeViewModel", "User from DB: uid=${user?.uid}, username='${user?.username}'")
                
                if (user == null || user.username.isBlank()) {
                    // Chưa có username -> đến màn hình nhập tên
                    android.util.Log.d("WelcomeViewModel", "❌ No username -> NAME_INPUT")
                    triggerEffect(WelcomeEffect.NavigateToNameInput)
                } else {
                    // Đã có đầy đủ thông tin -> vào thẳng Home
                    android.util.Log.d("WelcomeViewModel", "✅ User has username '${user.username}' -> HOME")
                    triggerEffect(WelcomeEffect.NavigateToHome)
                }
            } else {
                // Fallback nếu không có userRepository
                android.util.Log.d("WelcomeViewModel", "❌ No userRepository -> LOGIN")
                triggerEffect(WelcomeEffect.NavigateToLogin)
            }
            android.util.Log.d("WelcomeViewModel", "=== END CHECK AUTH ===")
        }
    }

    /**
     * Gửi một hiệu ứng đến View.
     */
    private fun triggerEffect(effect: WelcomeEffect) {
        // Sử dụng viewModelScope để đảm bảo coroutine chạy trong vòng đời của ViewModel.
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
