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

// THÊM FilterType enum
enum class FilterType {
    ALL,
    WEEK,
    MONTH,
    CUSTOM
}

// CẬP NHẬT HistoryUiState với các field mới
data class HistoryUiState(
    val weekLabel: String = "",
    val weekData: List<WeekData> = emptyList(),
    val historyList: List<HealthHistory> = emptyList(),
    val canNavigateToNextWeek: Boolean = false,
    val currentWeekOffset: Int = 0,
    // CÁC FIELD MỚI
    val filteredHistoryList: List<HealthHistory> = emptyList(),
    val isLoading: Boolean = false,
    val filterType: FilterType = FilterType.ALL,
    val searchQuery: String = ""
)

class HistoryViewModel(
    private val healthHistoryRepository: HealthHistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadCurrentWeek()
        loadAllHistory()
    }

    // ========== GIỮ NGUYÊN CODE CŨ ==========
    
    fun navigateToPreviousWeek() {
        val newOffset = _uiState.value.currentWeekOffset - 1
        loadWeekData(newOffset)
    }

    fun navigateToNextWeek() {
        val newOffset = _uiState.value.currentWeekOffset + 1
        if (newOffset <= 0) {
            loadWeekData(newOffset)
        }
    }

    private fun loadCurrentWeek() {
        loadWeekData(0)
    }

    private fun loadWeekData(weekOffset: Int) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)

            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val weekStart = calendar.timeInMillis
            val weekStartDate = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val weekEnd = calendar.timeInMillis
            val weekEndDate = calendar.time

            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val weekLabel = if (weekOffset == 0) {
                "Tuần này (${dateFormat.format(weekStartDate)} - ${dateFormat.format(weekEndDate)})"
            } else {
                "Tuần ${dateFormat.format(weekStartDate)} - ${dateFormat.format(weekEndDate)}"
            }

            val historyList = healthHistoryRepository.getHistoryInRange(uid, weekStart, weekEnd)

            val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            val weekDataList = mutableListOf<WeekData>()

            calendar.timeInMillis = weekStart
            for (i in 0..6) {
                val dayStart = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val dayEnd = calendar.timeInMillis

                val dayHistory = historyList.filter { it.timestamp >= dayStart && it.timestamp < dayEnd }

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

            val canNavigateNext = weekOffset < 0

            _uiState.value = _uiState.value.copy(
                weekLabel = weekLabel,
                weekData = weekDataList,
                historyList = historyList.sortedByDescending { it.timestamp },
                canNavigateToNextWeek = canNavigateNext,
                currentWeekOffset = weekOffset
            )
        }
    }

    // ========== THÊM CÁC FUNCTION MỚI ==========
    
    private fun loadAllHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                
                healthHistoryRepository.getAllHistory(uid).collect { history ->
                    _uiState.value = _uiState.value.copy(
                        filteredHistoryList = filterHistory(
                            history, 
                            _uiState.value.filterType, 
                            _uiState.value.searchQuery
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun setFilterType(filterType: FilterType) {
        _uiState.value = _uiState.value.copy(
            filterType = filterType,
            filteredHistoryList = filterHistory(
                _uiState.value.historyList, 
                filterType, 
                _uiState.value.searchQuery
            )
        )
    }

    fun searchHistory(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredHistoryList = filterHistory(
                _uiState.value.historyList, 
                _uiState.value.filterType, 
                query
            )
        )
    }

    private fun filterHistory(
        history: List<HealthHistory>, 
        filterType: FilterType, 
        query: String
    ): List<HealthHistory> {
        val calendar = Calendar.getInstance()

        val timeFiltered = when (filterType) {
            FilterType.ALL -> history
            FilterType.WEEK -> {
                calendar.add(Calendar.DAY_OF_MONTH, -7)
                history.filter { it.timestamp >= calendar.timeInMillis }
            }
            FilterType.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                history.filter { it.timestamp >= calendar.timeInMillis }
            }
            FilterType.CUSTOM -> history
        }

        return if (query.isBlank()) {
            timeFiltered.sortedByDescending { it.timestamp }
        } else {
            timeFiltered.filter { item ->
                item.notes?.contains(query, ignoreCase = true) == true ||
                item.weight?.toString()?.contains(query) == true ||
                item.heartRate?.toString()?.contains(query) == true
            }.sortedByDescending { it.timestamp }
        }
    }

    fun deleteHistoryItem(item: HealthHistory) {
        viewModelScope.launch {
            try {
                // healthHistoryRepository.delete(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}