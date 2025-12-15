package com.example.projectqlbenhan.entity.medicalRecord

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.patient.Patient

@Entity(
    tableName = "medical_records",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"])]
)
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,

    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    @ColumnInfo(name = "diagnosis")
    val diagnosis: String, // Chuẩn đoán

    @ColumnInfo(name = "symptoms")
    val symptoms: String, // Mô tả triệu chứng

    @ColumnInfo(name = "disease_type")
    val diseaseType: String, // Loại bệnh (để lọc)

    @ColumnInfo(name = "examination_date")
    val examinationDate: Long, // Ngày khám

//    @ColumnInfo(name = "doctor_name")
//    val doctorName: String?,


    @ColumnInfo(name = "doctor_id")
    val doctorId: Long,



    @ColumnInfo(name = "notes")
    val notes: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
