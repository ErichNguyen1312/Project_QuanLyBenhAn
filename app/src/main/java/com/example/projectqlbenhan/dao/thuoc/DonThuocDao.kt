package com.example.projectqlbenhan.dao.thuoc

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity
@Dao

interface DonThuocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // Hàm thêm: Trả về ID (Long) của hàng được chèn
    suspend fun them(donThuoc: ChiTietDonThuocEntity): Long

    @Update
    suspend fun capNhat(donThuoc: ChiTietDonThuocEntity)

    @Query("DELETE FROM don_thuoc WHERE don_thuoc_id = :id")
    suspend fun xoaTheoId(id: Long)

    @Query("SELECT * FROM don_thuoc ORDER BY ngay_tao DESC")
    // Lấy tất cả đơn thuốc (cho danh sách và dashboard)
    fun layTatCaDonThuoc(): LiveData<List<ChiTietDonThuocEntity>>

    @Query("SELECT * FROM don_thuoc WHERE don_thuoc_id = :id")
    // Lấy chi tiết đơn thuốc theo ID
    fun layDonThuocTheoId(id: Long): LiveData<ChiTietDonThuocEntity?>
}