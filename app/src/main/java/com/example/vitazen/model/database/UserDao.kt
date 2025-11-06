package com.example.vitazen.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vitazen.model.data.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object cho User entity.
 * Định nghĩa các operations CRUD cho User table.
 */
@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserById(uid: String): User?
    
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByIdFlow(uid: String): Flow<User?>
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE uid = :uid")
    suspend fun updateLastLogin(uid: String, timestamp: Long)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}
