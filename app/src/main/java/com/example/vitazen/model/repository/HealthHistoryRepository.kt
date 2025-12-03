package com.example.vitazen.model.repository

import com.example.vitazen.model.data.HealthHistory
import com.example.vitazen.model.database.HealthHistoryDao
import kotlinx.coroutines.flow.Flow

class HealthHistoryRepository(private val healthHistoryDao: HealthHistoryDao) {
    suspend fun insert(history: HealthHistory) = healthHistoryDao.insert(history)
    
    suspend fun getHistoryInRange(uid: String, startTime: Long, endTime: Long): List<HealthHistory> {
        return healthHistoryDao.getHistoryInRange(uid, startTime, endTime)
    }
    
    fun getHistoryInRangeFlow(uid: String, startTime: Long, endTime: Long): Flow<List<HealthHistory>> {
        return healthHistoryDao.getHistoryInRangeFlow(uid, startTime, endTime)
    }
    
    suspend fun getRecentHistory(uid: String, limit: Int): List<HealthHistory> {
        return healthHistoryDao.getRecentHistory(uid, limit)
    }
    
    fun getAllHistory(uid: String): Flow<List<HealthHistory>> {
        return healthHistoryDao.getAllHistory(uid)
    }
    
    suspend fun deleteOldHistory(uid: String, timestamp: Long) {
        healthHistoryDao.deleteOldHistory(uid, timestamp)
    }
}
