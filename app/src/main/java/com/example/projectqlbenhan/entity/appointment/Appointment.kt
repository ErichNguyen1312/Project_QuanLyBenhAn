package com.example.projectqlbenhan.entity.appointment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Doctor::class,
            parentColumns = ["doctorId"],
            childColumns = ["doctor_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("patient_id"), Index("doctor_id")]
)
data class Appointment(

    @PrimaryKey(autoGenerate = true)
    val appointmentId: Long = 0,
    @ColumnInfo(name = "patient_id")
    val patientId: Long,
    @ColumnInfo(name = "doctor_id")
    val doctorId: Long?, // Có thể null lúc mới đặt chờ xếp lịch

    val appointmentDate: Long, // Timestamp gồm ngày + giờ
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, CANCELLED
    val reason: String?, // Lý do khám
    val createdAt: Long = System.currentTimeMillis()
)