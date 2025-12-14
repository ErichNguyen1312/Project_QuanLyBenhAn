package com.example.projectqlbenhan.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord

@Entity(
    tableName = "prescriptions",
    foreignKeys = [
        ForeignKey(
            entity = MedicalRecord::class,
            parentColumns = ["recordId"],
            childColumns = ["record_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["record_id"])]
)
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val prescriptionId: Long = 0,

    @ColumnInfo(name = "record_id")
    val recordId: Long,

    @ColumnInfo(name = "medicine_name")
    val medicineName: String, // Tên thuốc

    @ColumnInfo(name = "dosage")
    val dosage: String, // Liều dùng (vd: "2 viên/lần")

    @ColumnInfo(name = "frequency")
    val frequency: String, // Tần suất (vd: "3 lần/ngày")

    @ColumnInfo(name = "duration_days")
    val durationDays: Int, // Số ngày dùng

    @ColumnInfo(name = "instructions")
    val instructions: String?, // Hướng dẫn sử dụng

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
