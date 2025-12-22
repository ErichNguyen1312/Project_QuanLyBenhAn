package com.example.projectqlbenhan.entity.medicalRecord

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.patient.Patient

@Entity(
    tableName = "medical_records",
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
        ),
        ForeignKey(
            entity = Appointment::class,
            parentColumns = ["appointmentId"],
            childColumns = ["appointment_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("patient_id"), Index("doctor_id"), Index("appointment_id")]
)
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,

    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    @ColumnInfo(name = "doctor_id")
    val doctorId: Long?,

    @ColumnInfo(name = "appointment_id")
    val appointmentId: Long? = null, // ⭐️ QUAN TRỌNG: Nullable để tạo bệnh án không cần lịch hẹn

    @ColumnInfo(name = "diagnosis")
    val diagnosis: String,

    @ColumnInfo(name = "symptoms")
    val symptoms: String,

    @ColumnInfo(name = "doctor_notes")
    val doctorNotes: String?,

    @ColumnInfo(name = "doctor_advice")
    val doctorAdvice: String?, // ⭐️ MỚI: Lời dặn dò bệnh nhân (Ăn kiêng, uống nhiều nước...)

    @ColumnInfo(name = "examination_date")
    val examinationDate: Long = System.currentTimeMillis()
)
