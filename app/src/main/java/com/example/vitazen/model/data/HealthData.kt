package com.example.vitazen.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey val uid: String, // user id
    val weight: Float = 0f, // kg
    val height: Float = 0f, // cm
    val heartRate: Int? = null, // bpm
    val waterIntake: Float = 0f, // lít
    val sleepHours: Float = 0f, // giờ ngủ
    val lastUpdate: Long = System.currentTimeMillis()
)

