package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoadHazardDao {
    @Query("SELECT * FROM road_hazards WHERE reportedBy = :reporter ORDER BY timestamp DESC")
    fun getHazardsByReporter(reporter: String): Flow<List<RoadHazardEntity>>

    @Query("SELECT * FROM road_hazards ORDER BY timestamp DESC")
    fun getAllHazards(): Flow<List<RoadHazardEntity>>

    @Query("SELECT * FROM road_hazards WHERE type = :type ORDER BY timestamp DESC")
    fun getHazardsByType(type: String): Flow<List<RoadHazardEntity>>

    @Query("SELECT * FROM road_hazards WHERE id = :id LIMIT 1")
    suspend fun getHazardById(id: Long): RoadHazardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazard(hazard: RoadHazardEntity): Long

    @Update
    suspend fun updateHazard(hazard: RoadHazardEntity)

    @Delete
    suspend fun deleteHazard(hazard: RoadHazardEntity)

    @Query("UPDATE road_hazards SET upvotes = upvotes + 1, userVote = 1 WHERE id = :id")
    suspend fun upvoteHazard(id: Long)

    @Query("UPDATE road_hazards SET downvotes = downvotes + 1, userVote = -1 WHERE id = :id")
    suspend fun downvoteHazard(id: Long)

    @Query("SELECT COUNT(*) FROM road_hazards")
    suspend fun getHazardCount(): Int
}
