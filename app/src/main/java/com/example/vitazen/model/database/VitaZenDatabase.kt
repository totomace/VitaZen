package com.example.vitazen.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vitazen.model.data.History
import com.example.vitazen.model.data.Reminder
import com.example.vitazen.model.data.User

/**
 * Room Database singleton cho VitaZen app.
 * Chứa 3 tables: User, History, Reminder.
 */
@Database(
    entities = [User::class, History::class, Reminder::class],
    version = 2,
    exportSchema = false
)
abstract class VitaZenDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun historyDao(): HistoryDao
    abstract fun reminderDao(): ReminderDao
    
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
