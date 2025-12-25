package com.example.projectqlbenhan.dao.reviewDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projectqlbenhan.entity.review.Review
import com.example.projectqlbenhan.entity.review.ReviewDetail

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

    //Lầy thông tin cho chức năng thống kê đánh giá - Trí
    @Query("""
                SELECT 
            r.reviewId, r.comment, r.rating_doctor, r.rating_diagnosis, r.rating_medication, r.created_at,
            d.fullName as doctorName,
            p.fullName as patientName
        FROM reviews r
        INNER JOIN medical_records m ON r.record_id = m.recordId
        INNER JOIN doctors d ON m.doctor_id = d.doctorId
        INNER JOIN patients p ON m.patient_id = p.patientId
        WHERE (:docId = -1 OR d.doctorId = :docId)
        AND (:rating = 0 OR r.rating_doctor = :rating)
        ORDER BY r.created_at DESC
    """)
    suspend fun getFilteredReviews(docId: Long, rating: Int): List<ReviewDetail>

    @Query("""
        SELECT 
            r.reviewId, r.comment, r.rating_doctor, r.rating_diagnosis, r.rating_medication, r.created_at,
            d.fullName as doctorName,
            p.fullName as patientName
        FROM reviews r
        INNER JOIN medical_records m ON r.record_id = m.recordId
        INNER JOIN doctors d ON m.doctor_id = d.doctorId
        INNER JOIN patients p ON m.patient_id = p.patientId
        WHERE (r.reviewId = :reviewId)
    """)
    suspend fun getReviewById(reviewId: Long): ReviewDetail
}
