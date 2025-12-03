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
    val darkModeEnabled: Boolean = false,
    val language: String = "Tiếng Việt"
)

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
        // TODO: Load from SharedPreferences or Database
        // For now, using default values
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        // TODO: Save to SharedPreferences
    }

    fun toggleWaterReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(waterReminderEnabled = enabled)
        // TODO: Save to SharedPreferences and schedule/cancel notifications
    }

    fun toggleExerciseReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(exerciseReminderEnabled = enabled)
        // TODO: Save to SharedPreferences and schedule/cancel notifications
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(darkModeEnabled = enabled)
        // TODO: Save to SharedPreferences and apply theme
    }

    fun exportData() {
        viewModelScope.launch {
            // TODO: Implement data export to CSV
            // Show success message
        }
    }

    fun deleteOldData() {
        viewModelScope.launch {
            // TODO: Delete data older than 90 days
            // Show confirmation dialog first
        }
    }
}

