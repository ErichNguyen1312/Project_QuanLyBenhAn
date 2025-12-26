package com.example.projectqlbenhan.entity.medicalRecord

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem

data class FullMedicalRecord(
    @Embedded
    val medicalRecord: MedicalRecord,

    @Relation(
        parentColumn = "doctor_id", // Cột doctor_id trong class MedicalRecord
        entityColumn = "doctorId"   // Cột doctorId trong class Doctor
    )
    val doctor: Doctor? = null,

    @Relation(
        parentColumn = "recordId",
        entityColumn = "record_id"
    )
    val prescriptionItems: List<PrescriptionItem>
)