package com.example.projectqlbenhan.ui.DonThuocUI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao

class DonThuocViewModelFactory(private val dao: PrescriptionItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonThuocViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DonThuocViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}