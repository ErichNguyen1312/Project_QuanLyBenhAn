package com.example.projectqlbenhan.dao.patient

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.projectqlbenhan.entity.patient.PatientWithRecords

@Dao
interface PatientWithRecordsDao {

    @Transaction
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    suspend fun getPatientWithRecords(patientId: Long): PatientWithRecords?
}