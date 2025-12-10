package com.example.projectqlbenhan.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MedicalRecordWithDetails(
    @Embedded val medicalRecord: MedicalRecord,
    @Relation(
        parentColumn = "recordId",
        entityColumn = "record_id"
    )
    val prescriptions: List<Prescription>,
    @Relation(
        parentColumn = "recordId",
        entityColumn = "record_id"
    )
    val appointments: List<Appointment>
)
