package com.example.projectqlbenhan.entity.appointment

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projectqlbenhan.entity.patient.Patient

data class AppointmentWithPatient(
    @Embedded
    val appointment: Appointment,

    @Relation(
        parentColumn = "patient_id", // Tên cột khóa ngoại trong bảng Appointments (trỏ tới Patient)
        entityColumn = "patientId"   // Tên cột khóa chính trong bảng Patients
    )
    val patient: Patient
)