package com.example.projectqlbenhan.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectqlbenhan.entity.Appointment

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE record_id = :recordId ORDER BY appointment_date ASC")
    suspend fun getAppointments(recordId: Long): List<Appointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE appointmentId = :id")
    suspend fun deleteById(id: Long)
}