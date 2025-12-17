package com.example.projectqlbenhan.entity.appointment

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projectqlbenhan.entity.patient.Patient

data class AppointmentWithPatient(

    @Embedded
    val appointment: Appointment,

    @Relation(
        parentColumn = "patientId",
        entityColumn = "patientId"
    )
    val patient: Patient
)