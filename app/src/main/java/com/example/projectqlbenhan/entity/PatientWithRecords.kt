package com.example.projectqlbenhan.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PatientWithRecords(
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "patientId",
        entityColumn = "patient_id"
    )
    val medicalRecords: List<MedicalRecord>
)
