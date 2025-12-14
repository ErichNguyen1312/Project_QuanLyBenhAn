package com.example.projectqlbenhan.dao.thuoc

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity


@Database(
    // FIX 1: CHỈ CÒN Entity Đơn Thuốc
    entities = [ChiTietDonThuocEntity::class],
    // ⭐️ Đảm bảo tăng version nếu bạn đã chạy ứng dụng trước khi xóa HoSoKhamBenh
    version = 2,
    exportSchema = false
)
abstract class PhongKhamDatabase : RoomDatabase() {


    abstract fun donThuocDao(): DonThuocDao

    companion object {
        @Volatile
        private var INSTANCE: PhongKhamDatabase? = null

        fun layDatabase(context: Context): PhongKhamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhongKhamDatabase::class.java,
                    "phong_kham_db"
                )
                    // ⭐️ BẮT BUỘC: Sử dụng migration phá hủy để xóa bảng HoSoKhamBenh cũ
                    // Nếu bạn đã chạy ứng dụng trước đó.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}