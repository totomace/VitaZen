package com.example.vitazen.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vitazen.MainActivity
import com.example.vitazen.R
import com.example.vitazen.model.data.Reminder
import java.util.Calendar

class ReminderNotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "vitazen_reminder_channel"
        private const val CHANNEL_NAME = "Nhắc nhở VitaZen"
        private const val CHANNEL_DESCRIPTION = "Thông báo nhắc nhở uống nước và sức khỏe"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
                )
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(reminder: Reminder) {
        if (!reminder.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Parse start time
        val timeParts = reminder.startTime.split(":")
        val startHour = timeParts[0].toInt()
        val startMinute = timeParts[1].toInt()

        // Calculate next alarm time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time has passed today, schedule for next occurrence
            while (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.MINUTE, reminder.intervalMinutes)
            }
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_water_ml", reminder.waterAmountMl)
            putExtra("reminder_type", reminder.type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel any existing alarm with same ID
        alarmManager.cancel(pendingIntent)

        // Schedule exact repeating alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // For Android 12+, use setExactAndAllowWhileIdle for each occurrence
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Request exact alarm permission
                    android.util.Log.e("ReminderHelper", "Cannot schedule exact alarms")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6-11
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // For older versions
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    (reminder.intervalMinutes * 60 * 1000).toLong(),
                    pendingIntent
                )
            }
            
            android.util.Log.d("ReminderHelper", 
                "Alarm scheduled for: ${calendar.time} (ID: ${reminder.id}, Interval: ${reminder.intervalMinutes} min)")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("ReminderHelper", "Error scheduling alarm: ${e.message}")
        }
    }

    fun cancelReminder(reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        android.util.Log.d("ReminderHelper", "Alarm cancelled for ID: $reminderId")
    }

    fun showNotification(reminderId: Long, title: String, waterMl: Int, reminderType: String) {
        // Intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_reminder", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build notification with alarm-like features
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $title")
            .setContentText("Đã đến giờ! Hãy uống ${waterMl}ml nước nhé 💧")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("⏰ Đã đến giờ!\n\n💧 Hãy uống ${waterMl}ml nước để giữ gìn sức khỏe.\n\nNhấn để mở ứng dụng."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(false) // User can dismiss
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500)) // Vibration pattern
            .setLights(0xFF00FF00.toInt(), 3000, 1000) // Green light
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Show as fullscreen (like alarm)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Đánh dấu đã uống",
                createMarkDoneIntent(context, reminderId)
            )
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(reminderId.toInt(), notification)
            android.util.Log.d("ReminderHelper", "Notification shown for: $title (ID: $reminderId)")
        } else {
            android.util.Log.e("ReminderHelper", "Notification permission not granted")
        }
    }

    private fun createMarkDoneIntent(context: Context, reminderId: Long): PendingIntent {
        val intent = Intent(context, MarkDoneBroadcastReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId + 10000).toInt(), // Different request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("ReminderReceiver", "Alarm triggered!")
        
        val reminderId = intent.getLongExtra("reminder_id", 0)
        val title = intent.getStringExtra("reminder_title") ?: "Nhắc nhở"
        val waterMl = intent.getIntExtra("reminder_water_ml", 250)
        val reminderType = intent.getStringExtra("reminder_type") ?: "WATER"

        val helper = ReminderNotificationHelper(context)
        helper.showNotification(reminderId, title, waterMl, reminderType)
        
        // Reschedule for next occurrence (for Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Get reminder from database and reschedule
            // This ensures continuous reminders
            rescheduleReminder(context, reminderId)
        }
    }
    
    private fun rescheduleReminder(context: Context, reminderId: Long) {
        // Load reminder from database and schedule next occurrence
        // This is needed for Android 12+ as setExactAndAllowWhileIdle only schedules once
        try {
            // You'll need to implement this based on your database access
            android.util.Log.d("ReminderReceiver", "Rescheduling reminder ID: $reminderId")
        } catch (e: Exception) {
            android.util.Log.e("ReminderReceiver", "Error rescheduling: ${e.message}")
        }
    }
}

class MarkDoneBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", 0)
        
        // Dismiss notification
        NotificationManagerCompat.from(context).cancel(reminderId.toInt())
        
        // Optionally: Save to database that user completed this reminder
        android.util.Log.d("MarkDoneReceiver", "Marked done: $reminderId")
    }
}