package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HazardReviewDao {
    @Query("SELECT * FROM hazard_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<HazardReviewEntity>>

    @Query("SELECT * FROM hazard_reviews WHERE hazardId = :hazardId ORDER BY timestamp DESC")
    fun getReviewsForHazard(hazardId: Long): Flow<List<HazardReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: HazardReviewEntity): Long
}
