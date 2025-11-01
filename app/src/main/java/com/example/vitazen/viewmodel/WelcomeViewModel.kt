package com.example.vitazen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}

/**
 * ViewModel cho màn hình WelcomeScreen.
 * Chịu trách nhiệm xử lý logic và quản lý trạng thái (nếu có).
 */
class WelcomeViewModel : ViewModel() {

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
                // Khi người dùng bấm nút Start, chúng ta sẽ xử lý logic ở đây.
                // Hiện tại, logic chỉ đơn giản là yêu cầu View chuyển màn hình.
                triggerEffect(WelcomeEffect.NavigateToLogin)
            }
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
