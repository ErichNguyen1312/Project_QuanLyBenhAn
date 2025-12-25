package com.example.projectqlbenhan.entity.doctor

import androidx.room.Embedded

data class DoctorWithRating(
    @Embedded val doctor: Doctor,
    val averageRating: Double?
)