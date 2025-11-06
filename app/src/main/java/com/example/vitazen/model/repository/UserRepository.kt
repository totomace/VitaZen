package com.example.vitazen.model.repository

import com.example.vitazen.model.data.User
import com.example.vitazen.model.database.UserDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository pattern cho User data.
 * Làm lớp trung gian giữa ViewModel và Database.
 */
class UserRepository(private val userDao: UserDao) {
    
    /**
     * Lưu hoặc cập nhật user vào database.
     */
    suspend fun insertOrUpdateUser(user: User) {
        userDao.insertUser(user)
    }
    
    /**
     * Lấy user theo uid (one-time).
     */
    suspend fun getUserById(uid: String): User? {
        return userDao.getUserById(uid)
    }
    
    /**
     * Lấy user theo uid (reactive Flow).
     */
    fun getUserByIdFlow(uid: String): Flow<User?> {
        return userDao.getUserByIdFlow(uid)
    }
    
    /**
     * Cập nhật thông tin user.
     */
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    /**
     * Cập nhật timestamp đăng nhập cuối.
     */
    suspend fun updateLastLogin(uid: String) {
        userDao.updateLastLogin(uid, System.currentTimeMillis())
    }
    
    /**
     * Xóa user khỏi database.
     */
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
    
    /**
     * Lấy tất cả users (reactive Flow).
     */
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
}
