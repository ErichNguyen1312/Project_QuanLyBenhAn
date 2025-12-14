package com.example.projectqlbenhan.ui.DonThuocUI

import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory cần Repository làm tham số để khởi tạo ViewModel
class DonThuocViewModelFactory(private val repository: DonThuocRepository) : ViewModelProvider.Factory {

    // Hàm này được gọi bởi hệ thống Android khi cần tạo ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra xem ViewModel được yêu cầu có phải là DonThuocViewModel không
        if (modelClass.isAssignableFrom(DonThuocViewModel::class.java)) {
            // Nếu đúng, tạo một instance mới của DonThuocViewModel, truyền Repository vào
            @Suppress("UNCHECKED_CAST")
            return DonThuocViewModel(repository) as T
        }
        // Nếu không đúng, ném ra ngoại lệ
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}