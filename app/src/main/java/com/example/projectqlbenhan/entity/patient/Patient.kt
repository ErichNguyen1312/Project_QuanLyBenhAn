package com.example.projectqlbenhan.entity.patient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.account.Account
import java.util.Calendar

@Entity(
    tableName = "patients",
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = ["accountId"],
        childColumns = ["account_id"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(
        value = ["account_id"],
        unique = true
    ), Index(value = ["medical_record_number"], unique = true)]
)
data class Patient(
    @PrimaryKey(autoGenerate = true) val patientId: Long = 0,
    @ColumnInfo(name = "account_id") val accountId: Long?, // Nullable nếu tạo hồ sơ offline

    val fullName: String,
    @ColumnInfo(name = "medical_record_number") val medicalRecordNumber: String, // Mã hồ sơ (VD: BN-2024001)
    val dateOfBirth: Long,
    val gender: String,
    val phoneNumber: String?,
    val address: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)