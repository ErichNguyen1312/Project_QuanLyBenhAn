package com.example.projectqlbenhan.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectqlbenhan.entity.Prescription

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE record_id = :recordId")
    suspend fun getByRecord(recordId: Long): List<Prescription>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: Prescription): Long

    @Update
    suspend fun update(prescription: Prescription)

    @Delete
    suspend fun delete(prescription: Prescription)

    @Query("SELECT COUNT(*) FROM prescriptions")
    suspend fun countPrescriptions(): Int

}