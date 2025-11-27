package com.example.vitazen.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class cho User table trong Room Database.
 * Lưu thông tin user để sử dụng offline.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String,
    val email: String,
    val username: String,
    val profilePictureUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
