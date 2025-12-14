package com.example.projectqlbenhan.dao.thuoc

import androidx.lifecycle.LiveData
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DonThuocRepository(private val donThuocDao: DonThuocDao) {

    val tatCaDonThuoc: LiveData<List<ChiTietDonThuocEntity>> = donThuocDao.layTatCaDonThuoc()

    // Quan trọng: Hàm này trả về Long (ID của hàng được thêm vào)
    suspend fun them(donThuoc: ChiTietDonThuocEntity): Long = withContext(Dispatchers.IO) {
        donThuocDao.them(donThuoc)
    }

    suspend fun capNhat(donThuoc: ChiTietDonThuocEntity) = withContext(Dispatchers.IO) {
        donThuocDao.capNhat(donThuoc)
    }

    suspend fun xoaTheoId(id: Long) = withContext(Dispatchers.IO) {
        donThuocDao.xoaTheoId(id)
    }

    fun layDonThuocTheoId(id: Long): LiveData<ChiTietDonThuocEntity?> {
        return donThuocDao.layDonThuocTheoId(id)
    }
}