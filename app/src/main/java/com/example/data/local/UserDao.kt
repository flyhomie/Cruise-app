package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0, lastSignOutTime = :signOutTime WHERE isLoggedIn = 1")
    suspend fun logoutAllUsers(signOutTime: Long = System.currentTimeMillis())

    @Query("UPDATE users SET isLoggedIn = 1, lastSignInTime = :signInTime WHERE id = :userId")
    suspend fun setLoggedInUser(userId: Long, signInTime: Long = System.currentTimeMillis())

    @Query("UPDATE users SET karmaPoints = karmaPoints + :points, reputationScore = reputationScore + :repDelta, reportsCount = reportsCount + 1 WHERE id = :userId")
    suspend fun addKarma(userId: Long, points: Int = 10, repDelta: Int = 15)

    @Query("UPDATE users SET reputationScore = MAX(0, reputationScore + :delta) WHERE id = :userId")
    suspend fun adjustReputation(userId: Long, delta: Int)

    @Query("UPDATE users SET username = :username, bio = :bio, preferredVehicle = :vehicle, avatarUri = :avatar WHERE id = :userId")
    suspend fun updateProfile(userId: Long, username: String, bio: String, vehicle: String, avatar: String)
}
