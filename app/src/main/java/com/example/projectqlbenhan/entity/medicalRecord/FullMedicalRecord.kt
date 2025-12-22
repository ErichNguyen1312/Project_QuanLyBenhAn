package com.example.projectqlbenhan.entity.medicalRecord

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem

data class FullMedicalRecord(
    @Embedded val medicalRecord: MedicalRecord,

    @Relation(
        parentColumn = "doctor_id",
        entityColumn = "doctorId"
    )
    val doctor: Doctor?,

    @Relation(
        parentColumn = "recordId", // ID trong bảng MedicalRecord
        entityColumn = "record_id" // ID trong bảng PrescriptionItem
    )
    val prescriptionItems: List<PrescriptionItem>
)
