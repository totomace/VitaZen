package com.example.vitazen.model.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.vitazen.ui.reminder.ReminderScreen

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Thêm id để dễ quản lý
    val uid: String = "",
    val title: String = "",
    val type: String = "", // WATER, MEDICINE, EXERCISE, etc.
    val intervalMinutes: Int = 0,
    val waterAmountMl: Int = 0,
    val startTime: String = "", // Format: "HH:mm"
    val endTime: String = "",   // Format: "HH:mm"
    val daysOfWeek: String = "", // JSON string của List<Int>
    val isEnabled: Boolean = true
)

enum class ReminderType {
    WATER,
    MEDICINE,
    EXERCISE,
    CHECKUP,
    CUSTOM
}
@Preview
@Composable
fun ReminderScreenPreview() {
    MaterialTheme {
        ReminderScreen()
    }
}