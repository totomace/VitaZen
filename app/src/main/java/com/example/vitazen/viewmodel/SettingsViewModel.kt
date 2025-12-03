package com.example.vitazen.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val notificationsEnabled: Boolean = true,
    val waterReminderEnabled: Boolean = true,
    val exerciseReminderEnabled: Boolean = false,
    val language: String = "Tiếng Việt",
    val showEditNameDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false
)

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val sharedPrefs = context.getSharedPreferences("VitaZenSettings", Context.MODE_PRIVATE)

    init {
        loadUserInfo()
        loadSettings()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val user = userRepository.getUserById(currentUser.uid)
                _uiState.value = _uiState.value.copy(
                    userName = user?.username ?: "",
                    userEmail = currentUser.email ?: ""
                )
            }
        }
    }

    private fun loadSettings() {
        _uiState.value = _uiState.value.copy(
            notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true),
            waterReminderEnabled = sharedPrefs.getBoolean("water_reminder_enabled", true),
            exerciseReminderEnabled = sharedPrefs.getBoolean("exercise_reminder_enabled", false),
            language = sharedPrefs.getString("language", "Tiếng Việt") ?: "Tiếng Việt"
        )
    }

    fun showEditNameDialog() {
        _uiState.value = _uiState.value.copy(showEditNameDialog = true)
    }

    fun showChangePasswordDialog() {
        _uiState.value = _uiState.value.copy(showChangePasswordDialog = true)
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
        Toast.makeText(context, "Đã ${if (enabled) "bật" else "tắt"} thông báo", Toast.LENGTH_SHORT).show()
    }

    fun toggleWaterReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(waterReminderEnabled = enabled)
        sharedPrefs.edit().putBoolean("water_reminder_enabled", enabled).apply()
        Toast.makeText(context, "Đã ${if (enabled) "bật" else "tắt"} nhắc nhở uống nước", Toast.LENGTH_SHORT).show()
    }

    fun toggleExerciseReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(exerciseReminderEnabled = enabled)
        sharedPrefs.edit().putBoolean("exercise_reminder_enabled", enabled).apply()
        Toast.makeText(context, "Đã ${if (enabled) "bật" else "tắt"} nhắc nhở tập thể dục", Toast.LENGTH_SHORT).show()
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val user = userRepository.getUserById(currentUser.uid)
                    if (user != null) {
                        val updatedUser = user.copy(username = newName)
                        userRepository.updateUser(updatedUser)
                        _uiState.value = _uiState.value.copy(userName = newName)
                        Toast.makeText(context, "Đã cập nhật tên thành công", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi cập nhật tên: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.email != null) {
                    // Re-authenticate user with current password
                    val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
                    user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                        if (reAuthTask.isSuccessful) {
                            // Update password
                            user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(context, "Đã đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Lỗi khi đổi mật khẩu: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi đổi mật khẩu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                // TODO: Implement data export to CSV
                Toast.makeText(context, "Chức năng xuất dữ liệu đang được phát triển", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi xuất dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteOldData() {
        viewModelScope.launch {
            try {
                // TODO: Delete data older than 90 days from database
                Toast.makeText(context, "Chức năng xóa dữ liệu cũ đang được phát triển", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi xóa dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
                sharedPrefs.edit().clear().apply()
                Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi khi đăng xuất: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

