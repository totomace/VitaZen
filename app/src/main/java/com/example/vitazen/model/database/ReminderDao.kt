package com.example.vitazen.model.database

import androidx.room.*
import com.example.vitazen.model.data.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    // Query tất cả reminders của user hiện tại
    @Query("SELECT * FROM reminders WHERE uid = :uid ORDER BY id DESC")
    fun getRemindersByUid(uid: String): Flow<List<Reminder>>

    // Query reminder theo ID
    @Query("SELECT * FROM reminders WHERE id = :id AND uid = :uid")
    suspend fun getReminderById(id: Long, uid: String): Reminder?

    // Insert reminder
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    // Update reminder
    @Update
    suspend fun updateReminder(reminder: Reminder)

    // Delete reminder
    @Delete
    suspend fun deleteReminder(reminder: Reminder)
 
    // Thêm vào ReminderDao.kt
    @Query("SELECT * FROM reminders WHERE uid = :uid AND isEnabled = 1")
    suspend fun getEnabledRemindersSync(uid: String): List<Reminder>
    
    // Delete all reminders của user
    @Query("DELETE FROM reminders WHERE uid = :uid")
    suspend fun deleteAllByUid(uid: String)
}