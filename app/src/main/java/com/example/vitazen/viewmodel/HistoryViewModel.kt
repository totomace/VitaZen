package com.example.vitazen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.HealthHistory
import com.example.vitazen.model.repository.HealthHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HistoryUiState(
    val weekLabel: String = "",
    val weekData: List<WeekData> = emptyList(),
    val historyList: List<HealthHistory> = emptyList(),
    val canNavigateToNextWeek: Boolean = false,
    val currentWeekOffset: Int = 0 // 0 = tuần hiện tại, -1 = tuần trước, etc.
)

class HistoryViewModel(
    private val healthHistoryRepository: HealthHistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadCurrentWeek()
    }

    fun navigateToPreviousWeek() {
        val newOffset = _uiState.value.currentWeekOffset - 1
        loadWeekData(newOffset)
    }

    fun navigateToNextWeek() {
        val newOffset = _uiState.value.currentWeekOffset + 1
        if (newOffset <= 0) { // Không cho phép xem tuần tương lai
            loadWeekData(newOffset)
        }
    }

    private fun loadCurrentWeek() {
        loadWeekData(0)
    }

    private fun loadWeekData(weekOffset: Int) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            // Tính toán thời gian bắt đầu và kết thúc tuần
            val calendar = Calendar.getInstance()

            // Di chuyển đến tuần mong muốn
            calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)

            // Lùi về Thứ 2 của tuần đó
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val weekStart = calendar.timeInMillis
            val weekStartDate = calendar.time

            // Tính thời gian kết thúc tuần (Chủ nhật 23:59:59)
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val weekEnd = calendar.timeInMillis
            val weekEndDate = calendar.time

            // Format label cho tuần
            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val weekLabel = if (weekOffset == 0) {
                "Tuần này (${dateFormat.format(weekStartDate)} - ${dateFormat.format(weekEndDate)})"
            } else {
                "Tuần ${dateFormat.format(weekStartDate)} - ${dateFormat.format(weekEndDate)}"
            }

            // Lấy dữ liệu từ database
            val historyList = healthHistoryRepository.getHistoryInRange(uid, weekStart, weekEnd)

            // Tạo dữ liệu cho biểu đồ tuần
            val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            val weekDataList = mutableListOf<WeekData>()

            calendar.timeInMillis = weekStart
            for (i in 0..6) {
                val dayStart = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val dayEnd = calendar.timeInMillis

                // Lấy dữ liệu của ngày này
                val dayHistory = historyList.filter { it.timestamp >= dayStart && it.timestamp < dayEnd }

                // Tính trung bình các giá trị trong ngày
                val avgWeight = if (dayHistory.isNotEmpty()) {
                    dayHistory.mapNotNull { it.weight }.average().toFloat()
                } else null

                val avgHeartRate = if (dayHistory.isNotEmpty()) {
                    dayHistory.mapNotNull { it.heartRate }.average().toInt()
                } else null

                val avgWaterIntake = if (dayHistory.isNotEmpty()) {
                    dayHistory.mapNotNull { it.waterIntake }.average().toFloat()
                } else null

                val avgSleepHours = if (dayHistory.isNotEmpty()) {
                    dayHistory.mapNotNull { it.sleepHours }.average().toFloat()
                } else null

                weekDataList.add(
                    WeekData(
                        dayLabel = dayLabels[i],
                        weight = avgWeight,
                        heartRate = avgHeartRate,
                        waterIntake = avgWaterIntake,
                        sleepHours = avgSleepHours,
                        timestamp = dayStart
                    )
                )
            }

            // Kiểm tra xem có thể chuyển sang tuần sau không
            val canNavigateNext = weekOffset < 0

            _uiState.value = HistoryUiState(
                weekLabel = weekLabel,
                weekData = weekDataList,
                historyList = historyList.sortedByDescending { it.timestamp },
                canNavigateToNextWeek = canNavigateNext,
                currentWeekOffset = weekOffset
            )
        }
    }
}

