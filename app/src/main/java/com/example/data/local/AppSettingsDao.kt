package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getAppSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET speedUnitMph = :isMph WHERE id = 1")
    suspend fun updateSpeedUnit(isMph: Boolean)

    @Query("UPDATE app_settings SET isAiVoiceOn = :enabled WHERE id = 1")
    suspend fun updateAiVoice(enabled: Boolean)

    @Query("UPDATE app_settings SET proximityAlertRangeMeters = :rangeMeters WHERE id = 1")
    suspend fun updateProximityRange(rangeMeters: Int)

    @Query("UPDATE app_settings SET is3dPerspective = :is3d WHERE id = 1")
    suspend fun update3dPerspective(is3d: Boolean)

    @Query("UPDATE app_settings SET nightDriveTheme = :nightMode WHERE id = 1")
    suspend fun updateThemeMode(nightMode: Boolean)

    @Query("UPDATE app_settings SET selectedRegion = :region WHERE id = 1")
    suspend fun updateRegion(region: String)

    @Query("UPDATE app_settings SET brandAccentTheme = :accent WHERE id = 1")
    suspend fun updateBrandAccent(accent: String)
}
