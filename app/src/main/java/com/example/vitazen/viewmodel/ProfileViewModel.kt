package com.example.vitazen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.data.HealthData
import com.example.vitazen.model.data.User
import com.example.vitazen.model.repository.HealthDataRepository
import com.example.vitazen.model.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val healthData: HealthData? = null,
    val bmi: Float? = null,
    val bmiCategory: String = "",
    val photoUrl: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val healthDataRepository: HealthDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Lấy user từ database
                val user = userRepository.getUserById(currentUser.uid)
                
                // Lấy health data
                val healthData = healthDataRepository.getHealthDataByUid(currentUser.uid)
                
                // Tính BMI
                val bmi = healthData?.let {
                    if (it.weight > 0 && it.height > 0) {
                        val heightInMeters = it.height / 100f
                        it.weight / (heightInMeters * heightInMeters)
                    } else null
                }
                
                // Phân loại BMI
                val bmiCategory = when {
                    bmi == null -> ""
                    bmi < 18.5f -> "Thiếu cân"
                    bmi < 25f -> "Bình thường"
                    bmi < 30f -> "Thừa cân"
                    else -> "Béo phì"
                }
                
                _uiState.value = ProfileUiState(
                    user = user,
                    healthData = healthData,
                    bmi = bmi,
                    bmiCategory = bmiCategory,
                    photoUrl = currentUser.photoUrl?.toString()
                )
            }
        }
    }
    
    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    class Factory(
        private val userRepository: UserRepository,
        private val healthDataRepository: HealthDataRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(userRepository, healthDataRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
