package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_scan_logs")
data class ScannedTripLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hazardType: String,
    val label: String,
    val roadName: String,
    val driverSpeedKmh: Int,
    val confidence: Int,
    val distanceMeters: Int,
    val actionTaken: String = "Audio Alert Played",
    val timestamp: Long = System.currentTimeMillis()
)
