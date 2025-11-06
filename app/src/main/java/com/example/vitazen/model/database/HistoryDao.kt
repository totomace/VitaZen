package com.example.vitazen.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vitazen.model.data.History
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object cho History entity.
 * Định nghĩa các operations CRUD cho History table.
 */
@Dao
interface HistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)
    
    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getHistoryById(id: Long): History?
    
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistoryByUserId(userId: String): Flow<List<History>>
    
    @Query("SELECT * FROM history WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getHistoryByUserIdAndType(userId: String, type: String): Flow<List<History>>
    
    @Update
    suspend fun updateHistory(history: History)
    
    @Delete
    suspend fun deleteHistory(history: History)
    
    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun deleteAllHistoryByUserId(userId: String)
    
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<History>>
}
