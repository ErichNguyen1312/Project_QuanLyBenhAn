package com.example.projectqlbenhan.dao.doctor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projectqlbenhan.entity.doctor.Doctor

@Dao
interface DoctorDao {

    @Query("SELECT * FROM doctors WHERE doctorId = :id LIMIT 1")
    suspend fun getDoctorById(id: Long): Doctor


    @Query("SELECT fullName FROM doctors WHERE doctorId = :id LIMIT 1")
    suspend fun getDoctorNameById(id: Long): String


    @Query("""
        SELECT * FROM doctors
        WHERE username = :username
        AND passwordHash = :passwordHash
        LIMIT 1
    """)
    suspend fun login(
        username: String,
        passwordHash: String
    ): Doctor?

    @Insert
    suspend fun insert(doctor: Doctor)

    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun countDoctors(): Int


}