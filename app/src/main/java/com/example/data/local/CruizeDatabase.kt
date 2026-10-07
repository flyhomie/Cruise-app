package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        RoadHazardEntity::class,
        HazardReviewEntity::class,
        AppSettingsEntity::class,
        ScannedTripLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CruizeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roadHazardDao(): RoadHazardDao
    abstract fun hazardReviewDao(): HazardReviewDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun scannedTripLogDao(): ScannedTripLogDao

    companion object {
        @Volatile
        private var INSTANCE: CruizeDatabase? = null

        fun getDatabase(context: Context): CruizeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CruizeDatabase::class.java,
                    "cruize_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
