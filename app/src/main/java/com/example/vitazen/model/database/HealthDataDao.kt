package com.example.vitazen.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vitazen.model.data.HealthData
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHealthData(data: HealthData)

    @Query("SELECT * FROM health_data WHERE uid = :uid")
    suspend fun getHealthDataByUid(uid: String): HealthData?

    @Query("SELECT * FROM health_data WHERE uid = :uid")
    fun getHealthDataByUidFlow(uid: String): Flow<HealthData?>

    @Update
    suspend fun updateHealthData(data: HealthData)

    @Query("SELECT * FROM health_data WHERE uid = :uid AND lastUpdate >= :start AND lastUpdate < :end LIMIT 1")
    suspend fun getHealthDataByUidAndDate(uid: String, start: Long, end: Long): HealthData?
    
    @Query("DELETE FROM health_data WHERE uid = :uid")
    suspend fun deleteHealthData(uid: String)
}
