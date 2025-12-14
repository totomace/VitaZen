package com.example.vitazen.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vitazen.model.database.VitaZenDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d("BootReceiver", "Device rebooted, rescheduling alarms...")
            
            // Reschedule all enabled reminders
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = VitaZenDatabase.getInstance(context)
                    val reminderDao = database.reminderDao()
                    val auth = FirebaseAuth.getInstance()
                    val uid = auth.currentUser?.uid
                    
                    if (uid != null) {
                        // Get all enabled reminders for current user
                        val reminders = reminderDao.getEnabledRemindersSync(uid)
                        
                        val notificationHelper = ReminderNotificationHelper(context)
                        
                        reminders.forEach { reminder ->
                            notificationHelper.scheduleReminder(reminder)
                            Log.d("BootReceiver", "Rescheduled reminder: ${reminder.title}")
                        }
                        
                        Log.d("BootReceiver", "Rescheduled ${reminders.size} reminders")
                    } else {
                        Log.w("BootReceiver", "No user logged in, skipping reschedule")
                    }
                    
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling reminders: ${e.message}", e)
                }
            }
        }
    }
}