package com.example.projectqlbenhan.entity.doctor

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "doctors",
    indices = [Index(value = ["username"], unique = true)]
)
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val doctorId: Long = 0,

    val username: String,
    val passwordHash: String,
    val fullName: String,
    val specialization: String?
)