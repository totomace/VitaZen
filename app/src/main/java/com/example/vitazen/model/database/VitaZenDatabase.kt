package com.example.vitazen.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vitazen.model.data.History
import com.example.vitazen.model.data.Reminder
import com.example.vitazen.model.data.User
import com.example.vitazen.model.data.HealthData
import com.example.vitazen.model.data.HealthHistory
import com.example.vitazen.model.data.Note
import com.example.vitazen.model.dao.NoteDao

/**
 * Room Database singleton cho VitaZen app.
 * Chứa 6 tables: User, History, Reminder, HealthData, HealthHistory, Note.
 */
@Database(
    entities = [User::class, History::class, Reminder::class, HealthData::class, HealthHistory::class, Note::class],
    version = 5, // tăng version để Room cập nhật table mới
    exportSchema = false
)
abstract class VitaZenDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun historyDao(): HistoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun healthDataDao(): HealthDataDao
    abstract fun healthHistoryDao(): HealthHistoryDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: VitaZenDatabase? = null
        
        fun getInstance(context: Context): VitaZenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VitaZenDatabase::class.java,
                    "vitazen_database"
                )
                    .fallbackToDestructiveMigration() // Chỉ dùng trong development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
