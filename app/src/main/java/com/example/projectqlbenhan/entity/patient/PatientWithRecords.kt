package com.example.projectqlbenhan.entity.patient

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord

data class PatientWithRecords(
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "patientId",
        entityColumn = "patient_id"
    )
    val medicalRecords: List<MedicalRecord>
)
