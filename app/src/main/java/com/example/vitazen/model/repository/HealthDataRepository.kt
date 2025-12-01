package com.example.vitazen.model.repository

import com.example.vitazen.model.data.HealthData
import com.example.vitazen.model.database.HealthDataDao
import kotlinx.coroutines.flow.Flow

class HealthDataRepository(private val healthDataDao: HealthDataDao) {
    suspend fun insertOrUpdateHealthData(data: HealthData) = healthDataDao.insertOrUpdateHealthData(data)
    suspend fun getHealthDataByUid(uid: String): HealthData? = healthDataDao.getHealthDataByUid(uid)
    fun getHealthDataByUidFlow(uid: String): Flow<HealthData?> = healthDataDao.getHealthDataByUidFlow(uid)
    suspend fun updateHealthData(data: HealthData) = healthDataDao.updateHealthData(data)
    suspend fun getHealthDataByUidAndDate(uid: String, start: Long, end: Long): HealthData? = healthDataDao.getHealthDataByUidAndDate(uid, start, end)
}
