package com.example.vitazen.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.repository.UserRepository
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
    val healthActivities: List<HealthActivity> = emptyList()
    ,
    val heightCm: Float? = null,
    val waterLiters: Float = 0f
)

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName = "",
            healthActivities = listOf(
                HealthActivity(1, "15/06/2024", "Cân nặng", "68 kg", Color(0xFF6200EE)),
                HealthActivity(2, "14/06/2024", "Huyết áp", "120/80 mmHg", Color(0xFF4CAF50)),
                HealthActivity(3, "13/06/2024", "Nhịp tim", "72 bpm", Color(0xFFF44336)),
                HealthActivity(4, "12/06/2024", "Số bước", "8,542 bước", Color(0xFF2196F3))
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserName()
    }

    fun addHealthActivity(type: String, value: String) {
        val current = _uiState.value.healthActivities
        val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newActivity = HealthActivity(
            id = nextId,
            date = "Hôm nay",
            type = type,
            value = value,
            color = Color(0xFF4CAF50)
        )
        _uiState.value = _uiState.value.copy(healthActivities = current + newActivity)
    }

    private fun loadUserName() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val user = userRepository.getUserById(uid)
                _uiState.value = _uiState.value.copy(userName = user?.username ?: "", heightCm = user?.heightCm)
            }
        }
    }

    fun setHeight(heightCm: Float) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val existing = userRepository.getUserById(uid)
                if (existing != null) {
                    val updated = existing.copy(heightCm = heightCm)
                    userRepository.insertOrUpdateUser(updated)
                    _uiState.value = _uiState.value.copy(heightCm = heightCm)
                }
            }
        }
    }

    fun setWaterLiters(liters: Float) {
        _uiState.value = _uiState.value.copy(waterLiters = liters)
    }

    fun addWaterLiters(delta: Float) {
        val newVal = (_uiState.value.waterLiters + delta).coerceAtLeast(0f)
        _uiState.value = _uiState.value.copy(waterLiters = newVal)
    }

    fun addWeightWithOptionalBMI(weightStr: String, autoCalcBMI: Boolean) {
        val weightValue = weightStr.trim().replace("kg", "").replace(" ", "")
        val weight = weightValue.toFloatOrNull()
        if (weight == null) return

        // Add weight activity
        addHealthActivity("Cân nặng", "$weight kg")

        // If auto-calc BMI and we have height, compute and add BMI
        val height = _uiState.value.heightCm
        if (autoCalcBMI && height != null && height > 0f) {
            val heightM = height / 100f
            val bmi = weight / (heightM * heightM)
            val bmiStr = String.format("%.1f", bmi)
            addHealthActivity("BMI", bmiStr)
        }
    }
}