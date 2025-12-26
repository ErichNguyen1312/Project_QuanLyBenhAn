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
    val medicineName: String, // Tên thuốc

    @ColumnInfo(name = "quantity")
    val quantity: Int, // Số lượng

    @ColumnInfo(name = "unit")
    val unit: String, // Đơn vị tính (Viên, vỉ...)

    @ColumnInfo(name = "dosage")
    val dosage: String, // Liều dùng

    @ColumnInfo(name = "instruction")
    val instruction: String = "", // Hướng dẫn sử dụng

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis() // Thời gian kê đơn
)