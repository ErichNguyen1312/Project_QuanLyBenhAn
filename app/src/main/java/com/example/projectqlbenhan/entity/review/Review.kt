package com.example.projectqlbenhan.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projectqlbenhan.entity.appointment.Appointment

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = Appointment::class,
            parentColumns = ["appointmentId"],
            childColumns = ["appointment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["appointment_id"], unique = true)]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Long = 0,

    @ColumnInfo(name = "appointment_id")
    val appointmentId: Long,

    @ColumnInfo(name = "rating_doctor")
    val ratingDoctor: Int, // 1-5

    @ColumnInfo(name = "rating_diagnosis")
    val ratingDiagnosis: Int, // 1-5

    @ColumnInfo(name = "rating_medication")
    val ratingMedication: Int, // 1-5

    @ColumnInfo(name = "comment")
    val comment: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
