package com.example.projectqlbenhan.dao.medicalRecord

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records WHERE patient_id = :patientId ORDER BY examination_date DESC")
    suspend fun getRecordsOfPatient(patientId: Long): List<MedicalRecord>


    @Query("SELECT * FROM medical_records WHERE recordId = :id LIMIT 1")
    fun getRecordById(id: Long): MedicalRecord?


    @Query("SELECT * FROM medical_records WHERE patient_id = :patientId ORDER BY patient_id DESC")
    fun getRecordsByPatient(patientId: Long): List<MedicalRecord>
    @Query("SELECT * FROM medical_records WHERE recordId = :recordId LIMIT 1")
    suspend fun getById(recordId: Long): MedicalRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MedicalRecord): Long

    @Update
    suspend fun update(record: MedicalRecord)

    @Delete
    suspend fun delete(record: MedicalRecord)

    @Query("DELETE FROM medical_records WHERE recordId = :id")
    suspend fun deleteRecord(id: Long)

    @Query("""
    UPDATE medical_records SET
        diagnosis = :diagnosis,
        symptoms = :symptoms,
        disease_type = :type,
        examination_date = :examinationDate,
        doctor_name = :doctor,
        notes = :notes
    WHERE recordId = :id
""")
    fun updateRecord(
        id: Long,
        diagnosis: String,
        symptoms: String,
        type: String,
        examinationDate: Long,
        doctor: String?,
        notes: String?
    )



    // lấy full details: đơn thuốc + lịch hẹn
//    @Transaction
//    @Query("SELECT * FROM medical_records WHERE recordId = :recordId")
//    suspend fun getRecordWithDetails(recordId: Long): MedicalRecordWithDetails
}