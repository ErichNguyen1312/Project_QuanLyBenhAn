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


    @Query(
        """
        SELECT * FROM appointments 
        WHERE appointmentId = (SELECT appointment_id FROM medical_records WHERE recordId = :recordId)
    """
    )
    suspend fun getAppointments(recordId: Long): List<Appointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE appointmentId = :id")
    suspend fun deleteById(id: Long)

    // Lấy lịch hẹn sắp tới
    @Query(
        """
        SELECT * FROM appointments
        WHERE appointmentDate >= :today
          AND status = 'SCHEDULED'
        ORDER BY appointmentDate ASC
    """
    )
    fun getUpcomingAppointments(today: Long): List<Appointment>

    //  Lấy List Lịch hẹn kèm Thông tin Bệnh nhân
    @Transaction
    @Query(
        """
        SELECT * FROM appointments
        WHERE appointmentDate >= :today
        ORDER BY appointmentDate ASC
    """
    )
    fun getUpcomingAppointmentsWithPatient(today: Long): List<AppointmentWithPatient>

    // Hàm overload cho việc sort
    @Transaction
    @Query(
        """
        SELECT * FROM appointments 
        WHERE appointmentDate >= :today 
        ORDER BY appointmentDate ASC
    """
    )
    fun getUpcomingAppointmentsWithPatientSorting(today: Long): List<AppointmentWithPatient>

    @Query("SELECT COUNT(*) FROM appointments WHERE appointmentDate BETWEEN :start AND :end")
    suspend fun countTodayAppointments(start: Long, end: Long): Int

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointments(): List<Appointment>

    @Query("SELECT * FROM appointments WHERE patient_id = :patientId ORDER BY appointmentDate ASC")
    fun getAppointmentsByPatient(patientId: Long): List<Appointment>

    // Cập nhật thông tin cuộc hẹn
    @Query(
        """
        UPDATE appointments 
        SET appointmentDate = :date,
            reason = :notes
        WHERE appointmentId = :id
    """
    )
    suspend fun updateAppointment(id: Long, date: Long, notes: String?)

    //  lấy lịch hẹn hôm nay cho bác sĩ
    @Query(
        """
        SELECT * FROM appointments
        WHERE appointmentDate BETWEEN :startToday AND :endToday
          AND status = 'SCHEDULED'
        ORDER BY appointmentDate ASC
    """
    )
    fun getTodayUpcomingAppointments(
        startToday: Long,
        endToday: Long
    ): List<Appointment>

    @Query(
        """
        SELECT COUNT(*) FROM appointments
        WHERE patient_id = :patientId
        AND appointmentDate BETWEEN :startDay AND :endDay
    """
    )
    suspend fun countAppointmentOfPatientInDay(patientId: Long, startDay: Long, endDay: Long): Int

    @Query("UPDATE appointments SET status = :status WHERE appointmentId = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE appointments SET status = 'MISSED' WHERE status = 'SCHEDULED' AND appointmentDate < :now")
    suspend fun markPastAppointmentsAsMissed(now: Long)

    @Transaction
    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY appointmentDate DESC")
    fun getAppointmentsByStatus(status: String): List<AppointmentWithPatient>

    @Query("SELECT * FROM appointments WHERE status = 'SCHEDULED' AND appointmentDate = :today")
    suspend fun getTodayScheduledAppointments(today: Long): List<Appointment>

    @Transaction
    @Query("""
        SELECT * FROM appointments 
        WHERE appointmentDate BETWEEN :start AND :end
        ORDER BY appointmentDate ASC
    """)
    suspend fun getAppointmentsByDateRange(start: Long, end: Long): List<AppointmentWithPatient>

    @Query("""
        SELECT * FROM appointments 
        WHERE doctor_id = :doctorId 
        AND appointmentDate >= :startTime 
        AND appointmentDate <= :endTime 
        AND status != 'CANCELLED'
    """)
    suspend fun getAppointmentsByDoctorAndDate(
        doctorId: Long,
        startTime: Long,
        endTime: Long
    ): List<Appointment>

    // update Tái khám - Trí
    @Query(
        """
    UPDATE appointments 
    SET appointmentDate = :date,
        reason = :notes,
        doctor_id = :doctorId
    WHERE appointmentId = :id
"""
    )
    suspend fun updateAppointmentTri(id: Long, date: Long, notes: String?, doctorId: Long?)

}
