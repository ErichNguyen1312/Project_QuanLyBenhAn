package com.example.projectqlbenhan.entity.medicalRecord

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projectqlbenhan.entity.Appointment
import com.example.projectqlbenhan.entity.Prescription

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
