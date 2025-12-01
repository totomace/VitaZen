package com.example.vitazen.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.repository.UserRepository
import com.example.vitazen.model.repository.HealthDataRepository
import com.example.vitazen.model.data.HealthData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HealthActivity(
    val id: Int,
    val date: String,
    val type: String,
    val value: String,
    val color: Color
)

data class HomeUiState(
    val userName: String = "",
    val healthActivities: List<HealthActivity> = emptyList(),
    val healthData: HealthData? = null // dữ liệu cá nhân
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val healthDataRepository: HealthDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName = "",
            healthActivities = listOf(
                HealthActivity(1, "15/06/2024", "Cân nặng", "68 kg", Color(0xFF6200EE)),
                HealthActivity(2, "14/06/2024", "Huyết áp", "120/80 mmHg", Color(0xFF4CAF50)),
                HealthActivity(3, "13/06/2024", "Nhịp tim", "72 bpm", Color(0xFFF44336)),
                HealthActivity(4, "12/06/2024", "Số bước", "8,542 bước", Color(0xFF2196F3))
            ),
            healthData = null
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserName()
        loadHealthData()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val user = userRepository.getUserById(uid)
                _uiState.value = _uiState.value.copy(userName = user?.username ?: "")
            }
        }
    }

    private fun loadHealthData() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val healthData = healthDataRepository.getHealthDataByUid(uid)
                _uiState.value = _uiState.value.copy(healthData = healthData)
            }
        }
    }

    fun saveHealthData(weight: Float, height: Float, heartRate: Int?, waterIntake: Float) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val healthData = HealthData(
                uid = uid,
                weight = weight,
                height = height,
                heartRate = heartRate,
                waterIntake = waterIntake,
                lastUpdate = System.currentTimeMillis()
            )
            healthDataRepository.insertOrUpdateHealthData(healthData)
            _uiState.value = _uiState.value.copy(healthData = healthData)
        }
    }

    fun loadYesterdayHealthData(onLoaded: (HealthData?) -> Unit) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val now = System.currentTimeMillis()
            val millisInDay = 24 * 60 * 60 * 1000
            val todayStart = now - (now % millisInDay)
            val yesterdayStart = todayStart - millisInDay
            val yesterdayEnd = todayStart
            val data = healthDataRepository.getHealthDataByUidAndDate(uid, yesterdayStart, yesterdayEnd)
            onLoaded(data)
        }
    }
}