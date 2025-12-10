package com.example.projectqlbenhan.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.projectqlbenhan.entity.Patient
//import com.example.projectqlbenhan.entity.PatientWithRecords
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    @Query("SELECT * FROM patients ORDER BY full_Name ASC")
    fun getAll(): List<Patient>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT * FROM patients WHERE patientId = :id")
    suspend fun getPatientById(id: Long): Patient?

    @Query("SELECT * FROM patients ORDER BY created_at DESC")
    fun getAllPatientsFlow(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE full_name LIKE '%' || :searchQuery || '%' OR medical_record_number LIKE '%' || :searchQuery || '%'")
    fun searchPatients(searchQuery: String): Flow<List<Patient>>

//    @Transaction
//    @Query("SELECT * FROM patients WHERE patientId = :patientId")
//    suspend fun getPatientWithRecords(patientId: Long): PatientWithRecords?

}