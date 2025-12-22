package com.example.projectqlbenhan.entity.prescriptionItem

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord

@Entity(
    tableName = "prescription_items",
    foreignKeys = [
        ForeignKey(
            entity = MedicalRecord::class,
            parentColumns = ["recordId"],
            childColumns = ["record_id"],
            onDelete = ForeignKey.CASCADE // Xóa bệnh án thì xóa luôn thuốc
        )
    ],
    indices = [Index(value = ["record_id"])]
)
data class PrescriptionItem(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,

    @ColumnInfo(name = "record_id")
    val recordId: Long, // Link tới lần khám nào

    @ColumnInfo(name = "medicine_name")
    val medicineName: String, // VD: "Paracetamol 500mg" (Nhập tay)

    @ColumnInfo(name = "quantity")
    val quantity: Int,     // VD: 10

    @ColumnInfo(name = "unit")
    val unit: String,      // VD: "Viên", "Vỉ", "Chai"

    @ColumnInfo(name = "dosage")
    val dosage: String     // VD: "Sáng 1, Chiều 1 sau ăn")
)