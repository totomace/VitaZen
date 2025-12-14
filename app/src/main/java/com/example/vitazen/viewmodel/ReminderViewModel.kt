package com.example.vitazen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.Reminder
import com.example.vitazen.model.data.ReminderType
import com.example.vitazen.model.database.VitaZenDatabase
import com.example.vitazen.model.repository.ReminderRepository
import com.example.vitazen.util.ReminderNotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val reminderRepository: ReminderRepository
    private val notificationHelper: ReminderNotificationHelper

    // StateFlows
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingReminder = MutableStateFlow<Reminder?>(null)
    val editingReminder: StateFlow<Reminder?> = _editingReminder.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val database = VitaZenDatabase.getInstance(application)
        reminderRepository = ReminderRepository(database.reminderDao())
        notificationHelper = ReminderNotificationHelper(application)
        
        // Load reminders from database
        loadReminders()
    }

    private fun loadReminders() {
        val uid = auth.currentUser?.uid ?: "default_user"
        
        viewModelScope.launch {
            reminderRepository.getReminders(uid).collect { reminderList ->
                _reminders.value = reminderList
            }
        }
    }

    // Add reminder with database persistence
    fun addReminder(
        title: String,
        type: ReminderType,
        intervalMinutes: Int,
        waterAmountMl: Int,
        startTime: String,
        endTime: String,
        daysOfWeek: List<Int>
    ) {
        val uid = auth.currentUser?.uid ?: "default_user"

        if (!validateReminderInput(title, intervalMinutes, waterAmountMl, startTime, endTime)) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val daysJson = daysOfWeek.joinToString(",", "[", "]")
                
                val reminder = Reminder(
                    id = 0, // Room will auto-generate
                    uid = uid,
                    title = title,
                    type = type.name,
                    intervalMinutes = intervalMinutes,
                    waterAmountMl = waterAmountMl,
                    startTime = startTime,
                    endTime = endTime,
                    daysOfWeek = daysJson,
                    isEnabled = true
                )

                // Insert to database and get new ID
                val newId = reminderRepository.insertReminder(reminder)
                
                // Schedule notification
                notificationHelper.scheduleReminder(reminder.copy(id = newId))

                _successMessage.value = "Đã thêm và lưu nhắc nhở thành công!"
                _showAddDialog.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi thêm nhắc nhở: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
// Thêm vào ReminderViewModel
fun testNotificationNow() {
    viewModelScope.launch {
        try {
            // Tạo reminder test với thời gian 1 phút sau
            val testReminder = Reminder(
                id = 999,
                uid = auth.currentUser?.uid ?: "test",
                title = "TEST - Uống nước",
                type = "WATER",
                intervalMinutes = 1,
                waterAmountMl = 250,
                startTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(System.currentTimeMillis() + 60000)), // 1 minute from now
                endTime = "23:59",
                daysOfWeek = "[1,2,3,4,5,6,7]",
                isEnabled = true
            )
            
            notificationHelper.scheduleReminder(testReminder)
            _successMessage.value = "Test notification sẽ hiện sau 1 phút!"
            
        } catch (e: Exception) {
            _errorMessage.value = "Lỗi test: ${e.message}"
        }
    }
}
    // Update reminder
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Update in database
                reminderRepository.updateReminder(reminder)
                
                // Reschedule notification if enabled
                if (reminder.isEnabled) {
                    notificationHelper.scheduleReminder(reminder)
                } else {
                    notificationHelper.cancelReminder(reminder.id)
                }

                _successMessage.value = "Đã cập nhật và lưu nhắc nhở!"
                _editingReminder.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete reminder
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Delete from database
                reminderRepository.deleteReminder(reminder)
                
                // Cancel notification
                notificationHelper.cancelReminder(reminder.id)

                _successMessage.value = "Đã xóa nhắc nhở!"
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi xóa: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Toggle reminder
    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedReminder = reminder.copy(isEnabled = !reminder.isEnabled)
                
                // Update in database
                reminderRepository.updateReminder(updatedReminder)
                
                // Schedule or cancel notification
                if (updatedReminder.isEnabled) {
                    notificationHelper.scheduleReminder(updatedReminder)
                    _successMessage.value = "Đã bật nhắc nhở!"
                } else {
                    notificationHelper.cancelReminder(updatedReminder.id)
                    _successMessage.value = "Đã tắt nhắc nhở!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi chuyển đổi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // UI Actions
    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun startEditing(reminder: Reminder) {
        _editingReminder.value = reminder
    }

    fun cancelEditing() {
        _editingReminder.value = null
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // Validation
    private fun validateReminderInput(
        title: String,
        intervalMinutes: Int,
        waterAmountMl: Int,
        startTime: String,
        endTime: String
    ): Boolean {
        when {
            title.isBlank() -> {
                _errorMessage.value = "Vui lòng nhập tiêu đề"
                return false
            }
            intervalMinutes <= 0 -> {
                _errorMessage.value = "Khoảng cách phải lớn hơn 0"
                return false
            }
            waterAmountMl < 0 -> {
                _errorMessage.value = "Lượng nước không hợp lệ"
                return false
            }
            !isValidTimeFormat(startTime) || !isValidTimeFormat(endTime) -> {
                _errorMessage.value = "Định dạng thời gian không hợp lệ (HH:mm)"
                return false
            }
            startTime >= endTime -> {
                _errorMessage.value = "Thời gian bắt đầu phải trước thời gian kết thúc"
                return false
            }
        }
        return true
    }

    private fun isValidTimeFormat(time: String): Boolean {
        val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        return regex.matches(time)
    }
}