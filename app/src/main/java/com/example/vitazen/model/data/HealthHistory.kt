package com.example.vitazen.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity để lưu lịch sử sức khỏe theo ngày
 */
@Entity(tableName = "health_history")
data class HealthHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String, // user id
    val weight: Float? = null, // kg
    val height: Float? = null, // cm
    val heartRate: Int? = null, // bpm
    val waterIntake: Float? = null, // lít
    val bloodPressureSystolic: Int? = null, // mmHg
    val bloodPressureDiastolic: Int? = null, // mmHg
    val steps: Int? = null, // số bước
    val timestamp: Long = System.currentTimeMillis(), // thời gian ghi nhận
    val notes: String? = null // ghi chú
)
