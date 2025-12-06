package com.example.vitazen.model.repository

import com.example.vitazen.model.database.ReminderDao
import com.example.vitazen.model.data.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {

    fun getReminders(uid: String): Flow<List<Reminder>> {
        return reminderDao.getRemindersByUid(uid)
    }

    suspend fun getReminderById(id: Long, uid: String): Reminder? {
        return reminderDao.getReminderById(id, uid)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteAllReminders(uid: String) {
        reminderDao.deleteAllByUid(uid)
    }
}