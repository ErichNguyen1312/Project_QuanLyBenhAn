package com.example.projectqlbenhan.entity.review

import androidx.room.ColumnInfo
import java.io.Serializable

data class ReviewDetail(
    @ColumnInfo(name = "reviewId") val reviewId: Long,
    @ColumnInfo(name = "comment") val comment: String?,
    @ColumnInfo(name = "rating_doctor") val ratingDoctor: Int,
    @ColumnInfo(name = "rating_diagnosis") val ratingDiagnosis: Int,
    @ColumnInfo(name = "rating_medication") val ratingMedication: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,

    // Các trường lấy từ bảng khác qua JOIN
    @ColumnInfo(name = "doctorName") val doctorName: String,
    @ColumnInfo(name = "patientName") val patientName: String
): Serializable