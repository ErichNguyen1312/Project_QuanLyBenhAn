package com.example.projectqlbenhan.dao.medicalRecord

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.projectqlbenhan.entity.medicalRecord.DiseaseStat
import com.example.projectqlbenhan.entity.medicalRecord.FullMedicalRecord
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records WHERE patient_id = :patientId ORDER BY examination_date DESC")
    suspend fun getRecordsOfPatient(patientId: Long): List<MedicalRecord>

    // Lấy Full chi tiết (Bệnh án + Bác sĩ + Thuốc)
    @Transaction
    @Query("SELECT * FROM medical_records WHERE recordId = :recordId")
    suspend fun getFullRecordDetails(recordId: Long): FullMedicalRecord?

    @Query("SELECT * FROM medical_records WHERE recordId = :id LIMIT 1")
    fun getRecordById(id: Long): MedicalRecord?

    @Query("SELECT * FROM medical_records WHERE patient_id = :id LIMIT 1")
    fun getRecordByPatientId(id: Long): MedicalRecord?

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

    @Query("SELECT COUNT(*) FROM medical_records")
    suspend fun countMedicalRecords(): Int

    @Query("SELECT * FROM medical_records ORDER BY examination_date DESC")
    suspend fun getAll(): List<MedicalRecord>

    // Thống kê bệnh
//    @Query("""
//        SELECT diagnosis as diseaseType, COUNT(*) as total
//        FROM medical_records
//        GROUP BY diagnosis
//    """)
//    fun getDiseaseStats(): List<DiseaseStat>

    // Lấy bệnh án cụ thể của bệnh nhân
    @Query("""
        SELECT * FROM medical_records 
        WHERE recordId = :recordId 
        AND patient_id = :patientId
    """)
    suspend fun getRecordOfPatient(recordId: Long, patientId: Long): MedicalRecord?

    @Query("""
        SELECT * FROM medical_records 
        WHERE diagnosis LIKE '%' || :type || '%' 
        ORDER BY examination_date DESC
    """)
    suspend fun getByDiseaseType(type: String): List<MedicalRecord>

    @Query("SELECT DISTINCT diagnosis FROM medical_records")
    suspend fun getAllDiseaseTypes(): List<String>

    // Tìm bệnh án theo lịch hẹn (để check xem lịch này khám chưa)
    @Query("SELECT * FROM medical_records WHERE appointment_id = :apptId LIMIT 1")
    suspend fun getRecordByAppointmentId(apptId: Long): MedicalRecord?

    // Trong MedicalRecordDao.kt

    // Thống kê bệnh
    @Query("""
        SELECT disease_type as diseaseType, COUNT(*) as total 
        FROM medical_records 
        GROUP BY disease_type
    """)
    fun getDiseaseStats(): List<DiseaseStat>
}