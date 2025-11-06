package com.example.vitazen.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vitazen.model.data.Reminder
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object cho Reminder entity.
 * Định nghĩa các operations CRUD cho Reminder table.
 */
@Dao
interface ReminderDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?
    
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY time ASC")
    fun getRemindersByUserId(userId: String): Flow<List<Reminder>>
    
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isActive = 1 ORDER BY time ASC")
    fun getActiveRemindersByUserId(userId: String): Flow<List<Reminder>>
    
    @Update
    suspend fun updateReminder(reminder: Reminder)
    
    @Query("UPDATE reminders SET isActive = :isActive WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, isActive: Boolean)
    
    @Delete
    suspend fun deleteReminder(reminder: Reminder)
    
    @Query("DELETE FROM reminders WHERE userId = :userId")
    suspend fun deleteAllRemindersByUserId(userId: String)
    
    @Query("SELECT * FROM reminders ORDER BY time ASC")
    fun getAllReminders(): Flow<List<Reminder>>
}
