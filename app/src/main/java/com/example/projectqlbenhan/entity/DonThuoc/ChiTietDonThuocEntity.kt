package com.example.projectqlbenhan.entity.DonThuoc

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "don_thuoc")
data class ChiTietDonThuocEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "don_thuoc_id")
    val donThuocId: Long = 0,

    // ⭐️ FIX: Sử dụng ten_benh_nhan thay vì ho_so_kham_id
    @ColumnInfo(name = "ten_benh_nhan")
    val tenBenhNhan: String, // Trường bắt buộc

    @ColumnInfo(name = "ten_thuoc")
    val tenThuoc: String,

    @ColumnInfo(name = "dang_thuoc")
    val dangThuoc: String,

    @ColumnInfo(name = "lieu_dung")
    val lieuDung: String,

    @ColumnInfo(name = "so_lan_dung")
    val soLanDung: Int,

    @ColumnInfo(name = "ghi_chu")
    val ghiChu: String?,

    @ColumnInfo(name = "ngay_tao")
    val ngayTao: Long = System.currentTimeMillis()
)