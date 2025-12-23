package com.example.projectqlbenhan.ui.DonThuocUI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao

// Factory sử dụng PrescriptionItemDao để khởi tạo ViewModel trực tiếp
class DonThuocViewModelFactory(private val dao: PrescriptionItemDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra xem ViewModel yêu cầu có phải là DonThuocViewModel không
        if (modelClass.isAssignableFrom(DonThuocViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Truyền dao vào ViewModel thay vì Repository
            return DonThuocViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}