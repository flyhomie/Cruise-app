package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val speedUnitMph: Boolean = false, // false = KM/H (Default East Africa standard), true = MPH
    val isAiVoiceOn: Boolean = true,
    val proximityAlertRangeMeters: Int = 500,
    val is3dPerspective: Boolean = true,
    val nightDriveTheme: Boolean = true,
    val audioAlertVolume: Float = 1.0f,
    val autoRadarScanning: Boolean = true,
    val selectedRegion: String = "Kenya (Nairobi / Mombasa Corridors)",
    val brandAccentTheme: String = "NEON_CYAN", // NEON_CYAN, COBALT_BLUE, AMBER_RADAR
    val offlineCacheEnabled: Boolean = true,
    val customVoiceStyle: String = "Driver Pro (English / Swahili)",
    val lastUpdated: Long = System.currentTimeMillis()
)
