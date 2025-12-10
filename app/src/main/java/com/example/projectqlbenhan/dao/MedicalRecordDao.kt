package com.example.projectqlbenhan.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.projectqlbenhan.entity.MedicalRecord
import com.example.projectqlbenhan.entity.MedicalRecordWithDetails

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records WHERE patient_id = :patientId ORDER BY examination_date DESC")
    suspend fun getRecordsOfPatient(patientId: Long): List<MedicalRecord>

    @Query("SELECT * FROM medical_records WHERE recordId = :recordId LIMIT 1")
    suspend fun getById(recordId: Long): MedicalRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicalRecord): Long

    @Update
    suspend fun update(record: MedicalRecord)

    @Delete
    suspend fun delete(record: MedicalRecord)

//    // lấy full details: đơn thuốc + lịch hẹn
//    @Transaction
//    @Query("SELECT * FROM medical_records WHERE recordId = :recordId")
//    suspend fun getRecordWithDetails(recordId: Long): MedicalRecordWithDetails
}