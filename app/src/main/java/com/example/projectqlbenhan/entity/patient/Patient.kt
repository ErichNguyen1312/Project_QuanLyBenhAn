package com.example.projectqlbenhan.entity.patient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "patients")
data class Patient (
    @PrimaryKey(autoGenerate = true)
    val patientId: Long = 0,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: Long, // Timestamp

    @ColumnInfo(name = "gender")
    val gender: String, // "Nam", "Nữ"

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String?,

    @ColumnInfo(name = "address")
    val address: String?,

    @ColumnInfo(name = "medical_record_number")
    val medicalRecordNumber: String, // Mã hồ sơ

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
){
    @Ignore
    fun ageCalculated(): Int = calculateAge(dateOfBirth)
    fun calculateAge(dob: Long): Int {
        val dobCalendar = Calendar.getInstance().apply { timeInMillis = dob }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

}