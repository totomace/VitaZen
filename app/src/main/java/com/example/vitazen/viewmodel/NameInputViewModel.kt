package com.example.vitazen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.User
import com.example.vitazen.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NameInputUiState {
    object Idle : NameInputUiState()
    object Loading : NameInputUiState()
    data class Success(val userName: String) : NameInputUiState()
    data class Error(val message: String) : NameInputUiState()
}

class NameInputViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<NameInputUiState>(NameInputUiState.Idle)
    val uiState: StateFlow<NameInputUiState> = _uiState.asStateFlow()

    fun submitName(name: String) {
        if (name.isBlank()) {
            _uiState.value = NameInputUiState.Error("Vui lòng nhập tên.")
            return
        }
        _uiState.value = NameInputUiState.Loading
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userEntity = User(
                        uid = user.uid,
                        email = user.email ?: "",
                        username = name.trim(),
                        profilePictureUrl = user.photoUrl?.toString(),
                        createdAt = System.currentTimeMillis(),
                        lastLoginAt = System.currentTimeMillis()
                    )
                    userRepository.insertOrUpdateUser(userEntity)
                    _uiState.value = NameInputUiState.Success(name.trim())
                } else {
                    _uiState.value = NameInputUiState.Error("Không tìm thấy thông tin người dùng.")
                }
            } catch (e: Exception) {
                _uiState.value = NameInputUiState.Error("Lỗi lưu tên: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = NameInputUiState.Idle
    }
}