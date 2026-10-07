package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val avatarUri: String = "avatar_1",
    val karmaPoints: Int = 120,
    val reputationScore: Int = 150,
    val bio: String = "Vigilant driver scanning roads for hazards and keeping Cruize community safe.",
    val reportsCount: Int = 5,
    val preferredVehicle: String = "Sports Coupe",
    val isLoggedIn: Boolean = false,
    val lastSignInTime: Long = System.currentTimeMillis() - (1000 * 60 * 25),
    val lastSignOutTime: Long = System.currentTimeMillis() - (1000 * 60 * 60 * 14),
    val createdAt: Long = System.currentTimeMillis()
)
