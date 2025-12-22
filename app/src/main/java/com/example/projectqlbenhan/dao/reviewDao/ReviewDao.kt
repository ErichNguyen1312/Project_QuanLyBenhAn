package com.example.projectqlbenhan.dao.reviewDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projectqlbenhan.entity.review.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review): Long

    // Lấy đánh giá của 1 cuộc hẹn
    @Query("SELECT * FROM reviews WHERE appointment_id = :apptId LIMIT 1")
    suspend fun getReviewByAppointment(apptId: Long): Review?

    // Thống kê điểm trung bình của Bác sĩ (Rating 1)
    @Query("""
        SELECT AVG(r.rating_doctor) 
        FROM reviews r 
        INNER JOIN appointments a ON r.appointment_id = a.appointmentId 
        WHERE a.doctor_id = :doctorId
    """)
    fun getDoctorAverageRating(doctorId: Long): Flow<Float?>

    // Lấy danh sách review của bác sĩ
    @Query("""
        SELECT r.* FROM reviews r
        INNER JOIN appointments a ON r.appointment_id = a.appointmentId
        WHERE a.doctor_id = :doctorId
        ORDER BY r.created_at DESC
    """)
    fun getDoctorReviews(doctorId: Long): Flow<List<Review>>
}
