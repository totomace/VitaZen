package com.example.vitazen.viewmodel

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.Reminder
import com.example.vitazen.model.data.ReminderType
import com.example.vitazen.ui.reminder.ReminderScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    // Tạo danh sách tạm thời để test UI
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
        // Tạo dữ liệu mẫu cho testing
        loadSampleReminders()
    }

    private fun loadSampleReminders() {
        val sampleReminders = listOf(
            Reminder(
                uid = "user1",
                title = "Uống nước buổi sáng",
                type = ReminderType.WATER.name,
                intervalMinutes = 120,
                waterAmountMl = 250,
                startTime = "08:00",
                endTime = "22:00",
                isEnabled = true
            ),
            Reminder(
                uid = "user1",
                title = "Uống thuốc",
                type = ReminderType.MEDICINE.name,
                intervalMinutes = 240,
                waterAmountMl = 100,
                startTime = "09:00",
                endTime = "21:00",
                isEnabled = false
            ),
            Reminder(
                uid = "user1",
                title = "Tập thể dục",
                type = ReminderType.EXERCISE.name,
                intervalMinutes = 1440, // 24 giờ
                waterAmountMl = 0,
                startTime = "06:00",
                endTime = "07:00",
                isEnabled = true
            )
        )
        _reminders.value = sampleReminders
    }

    // Add reminder - Phiên bản đơn giản không dùng gson
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
                // Chuyển đổi List<Int> thành JSON string đơn giản
                val daysJson = daysOfWeek.joinToString(",", "[", "]")

                val reminder = Reminder(
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

                // Thêm vào danh sách hiện tại (tạm thời)
                val currentList = _reminders.value.toMutableList()
                currentList.add(reminder)
                _reminders.value = currentList

                _successMessage.value = "Đã thêm nhắc nhở thành công!"
                _showAddDialog.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi thêm nhắc nhở: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update reminder
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cập nhật trong danh sách (tạm thời)
                val currentList = _reminders.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == reminder.id }
                if (index != -1) {
                    currentList[index] = reminder
                    _reminders.value = currentList
                }

                _successMessage.value = "Đã cập nhật nhắc nhở!"
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
                // Xóa khỏi danh sách (tạm thời)
                val currentList = _reminders.value.toMutableList()
                currentList.removeAll { it.id == reminder.id }
                _reminders.value = currentList

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

                // Cập nhật trong danh sách (tạm thời)
                val currentList = _reminders.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == reminder.id }
                if (index != -1) {
                    currentList[index] = updatedReminder
                    _reminders.value = currentList
                }

                _successMessage.value = if (updatedReminder.isEnabled)
                    "Đã bật nhắc nhở!"
                else
                    "Đã tắt nhắc nhở!"
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
            waterAmountMl <= 0 -> {
                _errorMessage.value = "Lượng nước phải lớn hơn 0"
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

    // Helper function để parse daysOfWeek - Đơn giản hóa không dùng gson
    @Suppress("unused")
    fun parseDaysOfWeek(json: String): List<Int> {
        return try {
            // Remove brackets and split by comma
            val cleanJson = json.removeSurrounding("[", "]")
            if (cleanJson.isBlank()) {
                return listOf(1, 2, 3, 4, 5, 6, 7)
            }
            cleanJson.split(",").map { it.trim().toInt() }
        } catch (e: Exception) {
            listOf(1, 2, 3, 4, 5, 6, 7)
        }
    }

    // Helper function để convert ReminderType sang string
    @Suppress("unused")
    fun reminderTypeToString(type: ReminderType): String {
        return when (type) {
            ReminderType.WATER -> "Uống nước"
            ReminderType.MEDICINE -> "Uống thuốc"
            ReminderType.EXERCISE -> "Tập thể dục"
            ReminderType.CHECKUP -> "Khám sức khỏe"
            ReminderType.CUSTOM -> "Tùy chỉnh"
        }
    }

    // Thêm hàm này để tương thích với ReminderScreen đang gọi
    @Suppress("unused")
    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentList = _reminders.value.toMutableList()
                currentList.add(reminder)
                _reminders.value = currentList

                _successMessage.value = "Đã thêm nhắc nhở thành công!"
                _showAddDialog.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi thêm nhắc nhở: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}