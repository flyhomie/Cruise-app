package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedTripLogDao {
    @Query("SELECT * FROM trip_scan_logs ORDER BY timestamp DESC")
    fun getAllTripLogs(): Flow<List<ScannedTripLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ScannedTripLogEntity): Long

    @Query("DELETE FROM trip_scan_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM trip_scan_logs")
    suspend fun getLogCount(): Int
}
