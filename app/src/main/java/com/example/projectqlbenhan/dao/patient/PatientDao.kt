package com.example.projectqlbenhan.dao.patient

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    // ⭐ Hàm blocking (dùng cẩn thận, chỉ cho mục đích one-shot hoặc testing)
    @Query("SELECT * FROM patients ORDER BY full_Name ASC")
    fun getAll(): List<Patient>

    // ⭐ Hàm lấy tất cả bệnh nhân bất đồng bộ (one-shot read)
    @Query("SELECT * FROM patients ORDER BY full_Name ASC")
    suspend fun getAllOneShot(): List<Patient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("DELETE FROM patients WHERE patientId = :id")
    suspend fun deletePatientById(id: Long)

    // Lấy thông tin bệnh nhân theo ID (one-shot read)
    @Query("SELECT * FROM patients WHERE patientId = :id")
    suspend fun getPatientById(id: Long): Patient?

    // ⭐ Hàm lấy thông tin bệnh nhân theo ID (real-time stream)
    @Query("SELECT * FROM patients WHERE patientId = :id")
    fun getPatientFlowById(id: Long): Flow<Patient?>

    // Kiểm tra tính duy nhất của Mã số Hồ sơ Y tế (MRN)
    @Query("SELECT COUNT(*) FROM patients WHERE medical_record_number = :mrn")
    suspend fun countByMedicalRecordNumber(mrn: String): Int

    // -----------------------------------------------------
    // TRUY VẤN FLOW VÀ TÌM KIẾM
    // -----------------------------------------------------

    // Lấy tất cả bệnh nhân dưới dạng Flow (real-time list)
    @Query("SELECT * FROM patients ORDER BY created_at DESC")
    fun getAllPatientsFlow(): Flow<List<Patient>>

    // Tìm kiếm bệnh nhân theo tên hoặc MRN (real-time search)
    @Query("SELECT * FROM patients WHERE full_name LIKE '%' || :searchQuery || '%' OR medical_record_number LIKE '%' || :searchQuery || '%'")
    fun searchPatients(searchQuery: String): Flow<List<Patient>>

    // -----------------------------------------------------
    // TRUY VẤN MỐI QUAN HỆ
    // -----------------------------------------------------

    // Lấy 5 hồ sơ bệnh án gần đây nhất của một bệnh nhân
    @Query(
        """
    SELECT * FROM medical_records
    WHERE patient_id = :patientId
    ORDER BY examination_date DESC
    LIMIT 5
"""
    )
    suspend fun getRecentMedicalRecords(patientId: Long): List<MedicalRecord>

    // Lấy danh sách bệnh nhân đã được một bác sĩ cụ thể khám
    @Query("""
    SELECT DISTINCT p.*
    FROM patients p
    INNER JOIN medical_records m
        ON p.patientId = m.patient_id
    WHERE m.doctor_id = :doctorId
    ORDER BY p.full_name
""")
    suspend fun getPatientsByDoctor(doctorId: Long): List<Patient>

//    @Transaction
//    @Query("SELECT * FROM patients WHERE patientId = :patientId")
//    suspend fun getPatientWithRecords(patientId: Long): PatientWithRecords? // Mối quan hệ 1-N
}