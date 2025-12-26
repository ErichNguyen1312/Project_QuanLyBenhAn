package com.example.projectqlbenhan.dao.prescriptionItemDao

import androidx.room.*
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem

@Dao
interface PrescriptionItemDao {

    /**
     * TRUY VẤN DỮ LIỆU
     */

    // Lấy toàn bộ thuốc (Dùng cho thống kê Dashboard HomeActivity)
    @Query("SELECT * FROM prescription_items ORDER BY created_at DESC")
    suspend fun getAllItems(): List<PrescriptionItem>

    // Lấy toàn bộ thuốc (Dùng cho ViewModel trong màn hình Danh sách)
    @Query("SELECT * FROM prescription_items ORDER BY created_at DESC")
    suspend fun getAllPrescriptionItems(): List<PrescriptionItem>

    // Lấy danh sách thuốc theo mã bệnh án (Sắp xếp thuốc mới kê lên đầu)
    @Query("SELECT * FROM prescription_items WHERE record_id = :recordId ORDER BY itemId DESC")
    suspend fun getItemsByRecordId(recordId: Long): List<PrescriptionItem>

    // Lấy chi tiết một loại thuốc theo ID (Dùng để load dữ liệu vào màn hình Sửa)
    @Query("SELECT * FROM prescription_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getItemById(itemId: Long): PrescriptionItem?

    /**
     * THÊM MỚI VÀ CẬP NHẬT (LƯU DỮ LIỆU)
     */

    // ⭐ Chèn một danh sách thuốc (Dùng khi lưu bệnh án lần đầu hoặc cập nhật hàng loạt)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptionItems(items: List<PrescriptionItem>)

    // Thêm một loại thuốc lẻ
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PrescriptionItem): Long

    // ⭐ QUAN TRỌNG: Cập nhật thông tin thuốc đã có
    // Room sẽ tìm theo 'itemId' để ghi đè dữ liệu mới (Tên, liều dùng, số lượng...)
    @Update
    suspend fun update(item: PrescriptionItem): Int

    /**
     * XÓA DỮ LIỆU
     */

    @Delete
    suspend fun delete(item: PrescriptionItem): Int

    // ⭐ Xóa toàn bộ thuốc theo mã bệnh án
    @Query("DELETE FROM prescription_items WHERE record_id = :recordId")
    suspend fun deleteItemsByRecordId(recordId: Long)
}