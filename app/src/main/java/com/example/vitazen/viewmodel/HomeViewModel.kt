package com.example.vitazen.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitazen.model.repository.UserRepository
import com.example.vitazen.model.repository.HealthDataRepository
import com.example.vitazen.model.repository.HealthHistoryRepository
import com.example.vitazen.model.data.HealthData
import com.example.vitazen.model.data.HealthHistory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class HealthActivity(
    val id: Int,
    val date: String,
    val type: String,
    val value: String,
    val color: Color,
    val icon: String // "weight", "heart", "water", "steps", "blood_pressure"
)

data class WeekData(
    val dayLabel: String, // T2, T3, ...
    val weight: Float?,
    val heartRate: Int?,
    val waterIntake: Float?,
    val sleepHours: Float?,
    val timestamp: Long
)

data class HomeUiState(
    val userName: String = "",
    val healthActivities: List<HealthActivity> = emptyList(),
    val healthData: HealthData? = null, // dữ liệu cá nhân
    val weekData: List<WeekData> = emptyList(), // dữ liệu tuần hiện tại
    val currentWeekOffset: Int = 0, // 0 = tuần hiện tại, -1 = tuần trước
    val canNavigateToNextWeek: Boolean = false, // không thể xem tuần tương lai
    val isCurrentWeek: Boolean = true // đang ở tuần hiện tại
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val healthDataRepository: HealthDataRepository,
    private val healthHistoryRepository: HealthHistoryRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserName()
        loadHealthData()
        checkAndResetForNewDay()
        generateSampleDataIfNeeded()
        loadWeekData()
        loadRecentActivities()
    }
    
    /**
     * Tự động tạo dữ liệu mẫu cho 2 tuần qua nếu chưa có dữ liệu
     */
    private fun generateSampleDataIfNeeded() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            if (healthHistoryRepository == null) return@launch
            
            // Kiểm tra xem đã có dữ liệu chưa
            val existingData = healthHistoryRepository.getRecentHistory(uid, 1)
            if (existingData.isNotEmpty()) return@launch // Đã có dữ liệu rồi
            
            // Tạo dữ liệu mẫu cho 14 ngày qua
            val calendar = Calendar.getInstance()
            val baseWeight = 68f // Cân nặng ban đầu
            
            for (i in 13 downTo 0) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_MONTH, -i)
                calendar.set(Calendar.HOUR_OF_DAY, 8 + (i % 12)) // Thay đổi giờ
                calendar.set(Calendar.MINUTE, i * 4)
                calendar.set(Calendar.SECOND, 0)
                
                // Tạo biến động cân nặng tự nhiên: giảm dần với dao động nhỏ
                val dayProgress = (13 - i) / 13f
                val trend = -1.5f * dayProgress // Giảm 1.5kg trong 2 tuần
                val randomVariation = (kotlin.random.Random.nextFloat() - 0.5f) * 0.3f // Dao động ±0.15kg
                val weight = baseWeight + trend + randomVariation
                
                val history = HealthHistory(
                    uid = uid,
                    weight = weight,
                    height = 170f + (kotlin.random.Random.nextFloat() * 0.5f), // Chiều cao cố định với biến động nhỏ
                    heartRate = 65 + kotlin.random.Random.nextInt(15), // 65-80 bpm
                    waterIntake = 1.5f + kotlin.random.Random.nextFloat() * 1.5f, // 1.5-3L
                    sleepHours = 6f + kotlin.random.Random.nextFloat() * 3f, // 6-9 giờ
                    bloodPressureSystolic = 115 + kotlin.random.Random.nextInt(15), // 115-130
                    bloodPressureDiastolic = 75 + kotlin.random.Random.nextInt(10), // 75-85
                    steps = 6000 + kotlin.random.Random.nextInt(6000), // 6000-12000 bước
                    timestamp = calendar.timeInMillis,
                    notes = "Dữ liệu mẫu"
                )
                
                healthHistoryRepository.insert(history)
            }
        }
    }

    /**
     * Lấy hoạt động gần đây từ database
     */
    private fun loadRecentActivities() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            if (healthHistoryRepository == null) return@launch
            
            // Lấy 10 bản ghi gần nhất
            val recentHistory = healthHistoryRepository.getRecentHistory(uid, 10)
            
            // Chuyển đổi thành HealthActivity
            val activities = mutableListOf<HealthActivity>()
            var activityId = 1
            
            recentHistory.forEach { history ->
                val date = formatDate(history.timestamp)
                
                // Cân nặng
                history.weight?.let {
                    activities.add(
                        HealthActivity(
                            id = activityId++,
                            date = date,
                            type = "Cân nặng",
                            value = "${String.format("%.1f", it)} kg",
                            color = Color(0xFF6200EE),
                            icon = "weight"
                        )
                    )
                }
                
                // Nhịp tim
                history.heartRate?.let {
                    activities.add(
                        HealthActivity(
                            id = activityId++,
                            date = date,
                            type = "Nhịp tim",
                            value = "$it bpm",
                            color = Color(0xFFF44336),
                            icon = "heart"
                        )
                    )
                }
                
                // Nước uống
                history.waterIntake?.let {
                    activities.add(
                        HealthActivity(
                            id = activityId++,
                            date = date,
                            type = "Nước uống",
                            value = "${String.format("%.1f", it)} lít",
                            color = Color(0xFF2196F3),
                            icon = "water"
                        )
                    )
                }
                
                // Thời gian ngủ
                history.sleepHours?.let {
                    activities.add(
                        HealthActivity(
                            id = activityId++,
                            date = date,
                            type = "Thời gian ngủ",
                            value = "${String.format("%.1f", it)} giờ",
                            color = Color(0xFF9C27B0),
                            icon = "sleep"
                        )
                    )
                }

                // Huyết áp
                if (history.bloodPressureSystolic != null && history.bloodPressureDiastolic != null) {
                    activities.add(
                        HealthActivity(
                            id = activityId++,
                            date = date,
                            type = "Huyết áp",
                            value = "${history.bloodPressureSystolic}/${history.bloodPressureDiastolic} mmHg",
                            color = Color(0xFF4CAF50),
                            icon = "blood_pressure"
                        )
                    )
                }
                
                // Số bước
                history.steps?.let {
                    activities.add(
                        HealthActivity(
                            id = activityId++,
                            date = date,
                            type = "Số bước",
                            value = "${String.format("%,d", it)} bước",
                            color = Color(0xFFFF9800),
                            icon = "steps"
                        )
                    )
                }
            }
            
            // Giới hạn chỉ hiển thị 5 hoạt động gần đây nhất để tránh lag
            val recentActivities = activities.take(5)
            _uiState.value = _uiState.value.copy(healthActivities = recentActivities)
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return String.format("%02d/%02d/%04d", day, month, year)
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

    fun saveHealthData(weight: Float, height: Float, heartRate: Int?, waterIntake: Float, sleepHours: Float) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val healthData = HealthData(
                uid = uid,
                weight = weight,
                height = height,
                heartRate = heartRate,
                waterIntake = waterIntake,
                sleepHours = sleepHours,
                lastUpdate = System.currentTimeMillis()
            )
            healthDataRepository.insertOrUpdateHealthData(healthData)
            _uiState.value = _uiState.value.copy(healthData = healthData)
        }
    }

    /**
     * Lấy dữ liệu sức khỏe của ngày hôm nay
     * Ngày mới bắt đầu từ 00:00
     */
    fun getTodayHealthData(): HealthData? {
        return _uiState.value.healthData
    }

    /**
     * Kiểm tra xem có phải ngày mới không (sau 00:00)
     * Nếu là ngày mới, reset dữ liệu hiện tại
     */
    private fun checkAndResetForNewDay() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val currentData = healthDataRepository.getHealthDataByUid(uid)

            if (currentData != null) {
                val calendar = Calendar.getInstance()
                val todayStart = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // Nếu dữ liệu thuộc ngày hôm qua, reset về giá trị mặc định
                if (currentData.lastUpdate < todayStart) {
                    val newData = HealthData(
                        uid = uid,
                        weight = 0f,
                        height = currentData.height, // Giữ chiều cao
                        heartRate = null,
                        waterIntake = 0f,
                        sleepHours = 0f,
                        lastUpdate = System.currentTimeMillis()
                    )
                    healthDataRepository.insertOrUpdateHealthData(newData)
                    _uiState.value = _uiState.value.copy(healthData = newData)
                }
            }
        }
    }

    /**
     * Navigate to previous week
     */
    fun navigateToPreviousWeek() {
        val newOffset = _uiState.value.currentWeekOffset - 1
        loadWeekData(newOffset)
    }

    /**
     * Navigate to next week
     */
    fun navigateToNextWeek() {
        val newOffset = _uiState.value.currentWeekOffset + 1
        if (newOffset <= 0) { // Can't view future weeks
            loadWeekData(newOffset)
        }
    }

    fun navigateToCurrentWeek() {
        loadWeekData(0)
    }

    /**
     * Lấy dữ liệu tuần (từ Thứ 2 đến Chủ nhật)
     */
    private fun loadWeekData(weekOffset: Int = 0) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            if (healthHistoryRepository == null) return@launch

            // Tính thời gian bắt đầu và kết thúc tuần
            val calendar = Calendar.getInstance()
            
            // Di chuyển đến tuần mong muốn
            calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)

            // Lùi về Thứ 2 tuần đó
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val weekStart = calendar.timeInMillis

            // Tính thời gian kết thúc tuần (Chủ nhật 23:59:59)
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val weekEnd = calendar.timeInMillis

            // Lấy dữ liệu từ database
            val historyList = healthHistoryRepository.getHistoryInRange(uid, weekStart, weekEnd)

            // Tạo danh sách 7 ngày
            val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            val weekDataList = mutableListOf<WeekData>()

            calendar.timeInMillis = weekStart
            for (i in 0..6) {
                val dayStart = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val dayEnd = calendar.timeInMillis

                // Tìm dữ liệu trong ngày này
                val dayHistory = historyList.filter { it.timestamp >= dayStart && it.timestamp < dayEnd }

                // Tính trung bình các giá trị
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

            // Kiểm tra có thể chuyển tuần sau không
            val canNavigateNext = weekOffset < 0
            val isCurrentWeek = weekOffset == 0

            _uiState.value = _uiState.value.copy(
                weekData = weekDataList,
                currentWeekOffset = weekOffset,
                canNavigateToNextWeek = canNavigateNext,
                isCurrentWeek = isCurrentWeek
            )
        }
    }

    /**
     * Lưu dữ liệu sức khỏe và thêm vào lịch sử
     */
    fun saveHealthDataWithHistory(weight: Float, height: Float, heartRate: Int?, waterIntake: Float, sleepHours: Float) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            
            // Lưu vào bảng health_data (current)
            val healthData = HealthData(
                uid = uid,
                weight = weight,
                height = height,
                heartRate = heartRate,
                waterIntake = waterIntake,
                sleepHours = sleepHours,
                lastUpdate = System.currentTimeMillis()
            )
            healthDataRepository.insertOrUpdateHealthData(healthData)
            _uiState.value = _uiState.value.copy(healthData = healthData)

            // Lưu vào bảng health_history (lịch sử)
            if (healthHistoryRepository != null) {
                val history = HealthHistory(
                    uid = uid,
                    weight = weight,
                    height = height,
                    heartRate = heartRate,
                    waterIntake = waterIntake,
                    sleepHours = sleepHours,
                    timestamp = System.currentTimeMillis()
                )
                healthHistoryRepository.insert(history)
                
                // Reload dữ liệu tuần và hoạt động
                loadWeekData()
                loadRecentActivities()
            }
        }
    }
}