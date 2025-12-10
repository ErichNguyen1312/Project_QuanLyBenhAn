package com.example.projectqlbenhan.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.projectqlbenhan.entity.MedicalRecordWithDetails

@Dao
interface MedicalRecordWithDetailsDao {
    @Transaction
    @Query("SELECT * FROM medical_records WHERE recordId = :recordId")
    suspend fun getRecordFullDetails(recordId: Long): MedicalRecordWithDetails?
}