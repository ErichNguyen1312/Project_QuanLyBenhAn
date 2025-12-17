package com.example.projectqlbenhan.entity.appointment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = MedicalRecord::class,
            parentColumns = ["recordId"],
            childColumns = ["record_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index(value = ["record_id"])]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val appointmentId: Long = 0,

    @ColumnInfo(name = "record_id")
    val recordId: Long,


    val patientId: Long,
    @ColumnInfo(name = "appointment_date")
    val appointmentDate: Long, // Ngày hẹn

    @ColumnInfo(name = "appointment_time")
    val appointmentTime: String, // Giờ hẹn

    @ColumnInfo(name = "location")
    val location: String?, // Địa điểm khám

    @ColumnInfo(name = "doctor_id")
    val doctorId: Long,

    @ColumnInfo(name = "notes")
    val notes: String?,

    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true,

    @ColumnInfo(name = "reminder_time_before")
    val reminderTimeBeforeHours: Int = 24, // Nhắc trước bao nhiêu giờ

    @ColumnInfo(name = "status")
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, CANCELLED

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)