package com.example.projectqlbenhan.entity.account

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["username"], unique = true)]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val accountId: Long = 0,
    val username: String,
    val passwordHash: String,
    val role: String, // "ADMIN", "DOCTOR", "PATIENT"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
