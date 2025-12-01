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
import android.util.Log

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
            Log.w("NameInputViewModel", "Username is blank")
            _uiState.value = NameInputUiState.Error("Vui lòng nhập tên.")
            return
        }

        Log.d("NameInputViewModel", "=== START SUBMIT NAME ===")
        _uiState.value = NameInputUiState.Loading
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                Log.d("NameInputViewModel", "Current Firebase user: ${user?.uid}")

                if (user != null) {
                    val userEntity = User(
                        uid = user.uid,
                        email = user.email ?: "",
                        username = name.trim(),
                        profilePictureUrl = user.photoUrl?.toString(),
                        createdAt = System.currentTimeMillis(),
                        lastLoginAt = System.currentTimeMillis()
                    )
                    Log.d("NameInputViewModel", "Saving user to DB: uid=${userEntity.uid}, username='${userEntity.username}'")
                    userRepository.insertOrUpdateUser(userEntity)

                    // Verify saved data
                    val savedUser = userRepository.getUserById(user.uid)
                    Log.d("NameInputViewModel", "Verify saved user: uid=${savedUser?.uid}, username='${savedUser?.username}'")

                    _uiState.value = NameInputUiState.Success(name.trim())
                    Log.d("NameInputViewModel", "✅ Username saved successfully")
                } else {
                    Log.e("NameInputViewModel", "❌ No current user found")
                    _uiState.value = NameInputUiState.Error("Không tìm thấy thông tin người dùng.")
                }
            } catch (e: Exception) {
                Log.e("NameInputViewModel", "❌ Error saving username: ${e.message}", e)
                _uiState.value = NameInputUiState.Error("Lỗi lưu tên: ${e.message}")
            }
        }
        Log.d("NameInputViewModel", "=== END SUBMIT NAME ===")
    }

    fun resetState() {
        _uiState.value = NameInputUiState.Idle
    }
}