package com.example.projectqlbenhan.dao.reviewDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projectqlbenhan.entity.review.Review

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review): Long

    // 1. Tìm review theo Record ID (Thay vì Appointment)
    @Query("SELECT * FROM reviews WHERE record_id = :recordId LIMIT 1")
    suspend fun getReviewByRecord(recordId: Long): Review?

    // 2. Thống kê điểm (Join với bảng medical_records để lấy doctorId nếu cần,
    // hoặc nếu bro đã lưu doctorId vào review thì query thẳng bảng review luôn cho nhanh)
    // Ở đây giả sử mình Query thông qua MedicalRecord để cho chuẩn
    @Query("""
        SELECT AVG(r.rating_doctor) 
        FROM reviews r 
        INNER JOIN medical_records m ON r.record_id = m.recordId 
        WHERE m.doctor_id = :doctorId
    """)
    suspend fun getDoctorAverageRating(doctorId: Long): Float?

    @Query("""
        SELECT r.* FROM reviews r
        INNER JOIN medical_records m ON r.record_id = m.recordId
        WHERE m.doctor_id = :doctorId
        ORDER BY r.created_at DESC
    """)
    suspend fun getDoctorReviews(doctorId: Long): List<Review>
}
