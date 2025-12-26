package com.example.projectqlbenhan.entity.prescriptionItem

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity đại diện cho một mục thuốc trong đơn thuốc của bệnh án.
 * Việc sử dụng data class giúp DiffUtil trong Adapter so sánh dữ liệu chính xác để cập nhật UI.
 */
@Entity(tableName = "prescription_items")
data class PrescriptionItem(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,

    val recordId: Long, // Liên kết với bảng MedicalRecord

    val medicineName: String,

    val dosage: String, // Ví dụ: "Sáng 1 viên, chiều 1 viên sau ăn"

    val unit: String = "Viên", // Đơn vị tính mặc định

    val quantity: Int = 0, // Số lượng (mặc định là 0 để tránh lỗi nhập liệu trống)

    val instruction: String = "", // Hướng dẫn thêm hoặc ghi chú từ bác sĩ

    val createdAt: Long = System.currentTimeMillis() // Thời điểm kê đơn để sắp xếp
)