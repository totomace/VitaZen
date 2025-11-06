package com.example.vitazen.model.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class cho History table.
 * Lưu lịch sử hoạt động của user (ví dụ: lịch sử ăn uống, tập luyện, v.v.)
 */
@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String // ví dụ: "meal", "exercise", "water", "sleep"
)
