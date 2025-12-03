package com.example.vitazen.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.vitazen.model.data.HealthHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthHistoryDao {
    @Insert
    suspend fun insert(history: HealthHistory)

    /**
     * Lấy lịch sử sức khỏe trong khoảng thời gian
     */
    @Query("SELECT * FROM health_history WHERE uid = :uid AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getHistoryInRange(uid: String, startTime: Long, endTime: Long): List<HealthHistory>

    /**
     * Lấy lịch sử sức khỏe trong khoảng thời gian (Flow)
     */
    @Query("SELECT * FROM health_history WHERE uid = :uid AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getHistoryInRangeFlow(uid: String, startTime: Long, endTime: Long): Flow<List<HealthHistory>>

    /**
     * Lấy N bản ghi gần nhất
     */
    @Query("SELECT * FROM health_history WHERE uid = :uid ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistory(uid: String, limit: Int): List<HealthHistory>

    /**
     * Lấy tất cả lịch sử của user
     */
    @Query("SELECT * FROM health_history WHERE uid = :uid ORDER BY timestamp DESC")
    fun getAllHistory(uid: String): Flow<List<HealthHistory>>

    /**
     * Xóa lịch sử cũ hơn timestamp
     */
    @Query("DELETE FROM health_history WHERE uid = :uid AND timestamp < :timestamp")
    suspend fun deleteOldHistory(uid: String, timestamp: Long)
}
