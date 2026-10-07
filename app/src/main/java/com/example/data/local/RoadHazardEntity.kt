package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HazardType {
    SPEED_BUMP,
    COP_TRAP,
    SPEED_CAMERA,
    STATIONARY_SPEED_CAM,
    MOBILE_SPEED_CAM,
    CCTV_CAMERA,
    POTHOLE,
    ACCIDENT,
    ROADWORK
}

enum class HazardSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Entity(tableName = "road_hazards")
data class RoadHazardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // HazardType name
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val roadName: String,
    val speedLimit: Int = 45, // in MPH / KMH
    val severity: String = HazardSeverity.MEDIUM.name,
    val reportedBy: String,
    val upvotes: Int = 1,
    val downvotes: Int = 0,
    val userVote: Int = 0, // 1 = upvoted, -1 = downvoted, 0 = none
    val isVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val cameraType: String = "TRAFFIC_SURVEILLANCE", // TRAFFIC_SURVEILLANCE, SPEED_GANTRY, RED_LIGHT, ANPR_PLATE_SCANNER, MUNICIPAL_SECURITY
    val cameraDirection: String = "BOTH_WAYS", // NORTHBOUND, SOUTHBOUND, ALL_LANES, INTERSECTION_360
    val cameraStatus: String = "ACTIVE_RECORDING" // ACTIVE_RECORDING, AI_ENFORCING, SPEED_MONITORED, RED_LIGHT_ACTIVE
)
