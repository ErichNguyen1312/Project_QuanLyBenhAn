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

    @Query("SELECT * FROM patients ORDER BY fullName ASC")
    fun getAll(): List<Patient>

    @Query("SELECT * FROM patients ORDER BY fullName ASC")
    suspend fun getAllOneShot(): List<Patient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("DELETE FROM patients WHERE patientId = :id")
    suspend fun deletePatientById(id: Long)

    @Query("SELECT * FROM patients WHERE patientId = :id")
    suspend fun getPatientById(id: Long): Patient?

    @Query("SELECT * FROM patients WHERE patientId = :id")
    fun getPatientFlowById(id: Long): Flow<Patient?>

    //Lấy Patient theo AccountId
    @Query("SELECT * FROM patients WHERE account_id = :accountId LIMIT 1")
    suspend fun getPatientByAccountId(accountId: Long): Patient?

    @Query("SELECT COUNT(*) FROM patients WHERE medical_record_number = :mrn")
    suspend fun countByMedicalRecordNumber(mrn: String): Int

    @Query("SELECT * FROM patients ORDER BY created_at DESC")
    fun getAllPatientsFlow(): Flow<List<Patient>>

    // Search  theo tên
    @Query("SELECT * FROM patients WHERE fullName LIKE '%' || :searchQuery || '%' OR medical_record_number LIKE '%' || :searchQuery || '%'")
    fun searchPatients(searchQuery: String): Flow<List<Patient>>

    @Query(
        """
        SELECT * FROM medical_records
        WHERE patient_id = :patientId
        ORDER BY examination_date DESC
        LIMIT 5
    """
    )
    suspend fun getRecentMedicalRecords(patientId: Long): List<MedicalRecord>

    @Query(
        """
        SELECT DISTINCT p.*
        FROM patients p
        INNER JOIN medical_records m ON p.patientId = m.patient_id
        WHERE m.doctor_id = :doctorId
        ORDER BY p.fullName
    """
    )
    suspend fun getPatientsByDoctor(doctorId: Long): List<Patient>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun countPatients(): Int

    @Query("SELECT * FROM patients WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getPatientByPhone(phone: String): Patient?
}