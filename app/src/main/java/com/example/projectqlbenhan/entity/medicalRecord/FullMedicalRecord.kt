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
        parentColumn = "recordId", // Biến recordId trong class MedicalRecord
        entityColumn = "recordId"  // ⭐ FIX TẠI ĐÂY: Sửa record_id thành recordId cho khớp với PrescriptionItem
    )
    val prescriptionItems: List<PrescriptionItem> = emptyList() // ⭐ Thêm giá trị mặc định để fix lỗi Constructor
)