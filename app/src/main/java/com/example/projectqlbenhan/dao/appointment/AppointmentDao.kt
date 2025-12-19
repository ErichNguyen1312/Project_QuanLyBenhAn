package com.example.projectqlbenhan.dao.appointment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.appointment.AppointmentWithPatient

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE record_id = :recordId ORDER BY appointment_date ASC")
    suspend fun getAppointments(recordId: Long): List<Appointment>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE appointmentId = :id")
    suspend fun deleteById(id: Long)

    //list lich hen
    @Query(
        """
        SELECT * FROM appointments
        WHERE appointment_date >= :today
          AND status = 'SCHEDULED'
        ORDER BY appointment_date ASC, appointment_time ASC
    """
    )
    fun getUpcomingAppointments(
        today: Long
    ): List<Appointment>


    @Transaction
    @Query(
        """
        SELECT * FROM appointments
        WHERE appointment_date >= :today
        ORDER BY appointment_date ASC, appointment_time ASC
       
    """
    )
    fun getUpcomingAppointmentsWithPatient(
        today: Long

    ): List<AppointmentWithPatient>


//    @Query("""
//    SELECT COUNT(*) FROM appointments
//    WHERE appointment_date BETWEEN :start AND :end
//""")
//    suspend fun countTodayAppointments(
//        start: Long,
//        end: Long
//    ): Int

    @Query("SELECT COUNT(*) FROM appointments WHERE appointment_date BETWEEN :start AND :end")
    suspend fun countTodayAppointments(start: Long, end: Long): Int

    @Transaction
    @Query("""
    SELECT * FROM appointments 
    WHERE appointment_date >= :today 
    ORDER BY appointment_date ASC, appointment_time ASC
""")
    fun getUpcomingAppointmentsWithPatientSorting(today: Long): List<AppointmentWithPatient>

    // Mới thêm - Trí
    @Query(
        """
    SELECT * FROM appointments
"""
    )
    suspend fun getAllAppointments(): List<Appointment>

    @Insert
    fun insertAppointments(
        appointments: Appointment
    )

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY appointment_date ASC")
    fun getAppointmentsByPatient(patientId: Long): List<Appointment>

    @Query(
        """
    UPDATE appointments 
    SET appointment_date = :date,
        appointment_time = :time,
        notes = :notes
    WHERE appointmentId = :id
"""
    )
    suspend fun updateAppointment(
        id: Long,
        date: Long,
        time: String,
        notes: String?
    )

    // Mới thêm cho thông báo tái khám - Trí
    @Query("""
    SELECT * FROM appointments
    WHERE appointment_date BETWEEN :startToday AND :endToday
      AND appointment_time >= :nowTime
      AND status = 'SCHEDULED'
    ORDER BY appointment_time ASC
""")
    fun getTodayUpcomingAppointments(
        startToday: Long,
        endToday: Long,
        nowTime: String
    ): List<Appointment>

    @Query("""
SELECT COUNT(*) FROM appointments
WHERE patientId = :patientId
AND appointment_date BETWEEN :startDay AND :endDay
""")
    suspend fun countAppointmentOfPatientInDay(
        patientId: Long,
        startDay: Long,
        endDay: Long
    ): Int


    @Query("""
SELECT COUNT(*) FROM appointments
WHERE appointment_date = :appointmentDate
AND appointment_time = :appointmentTime
""")
    suspend fun countAppointmentAtTime(
        appointmentDate: Long,
        appointmentTime: String
    ): Int


    // xu ly appointmnet status
    @Query("UPDATE appointments SET status = :status WHERE appointmentId = :id")
    suspend fun updateStatus(id: Long, status: String)


    @Query("UPDATE appointments SET status = 'MISSED' WHERE status = 'SCHEDULED' AND appointment_date < :now")
    suspend fun markPastAppointmentsAsMissed(now: Long)


    @Transaction
    @Query(
        """
        SELECT * FROM appointments 
        WHERE status = :status
        ORDER BY appointment_date DESC, appointment_time DESC
    """
    )
    fun getAppointmentsByStatus(status: String): List<AppointmentWithPatient>

    @Query("SELECT * FROM appointments WHERE status = 'SCHEDULED' AND appointment_date = :today")
    suspend fun getTodayScheduledAppointments(today: Long): List<Appointment>
}
