package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hazard_reviews")
data class HazardReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hazardId: Long,
    val userName: String,
    val rating: Float, // 1.0 to 5.0
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
