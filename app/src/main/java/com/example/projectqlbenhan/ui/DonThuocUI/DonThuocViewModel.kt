package com.example.projectqlbenhan.ui.DonThuocUI

import androidx.lifecycle.*
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DonThuocViewModel(private val dao: PrescriptionItemDao) : ViewModel() {

    private var allItems: List<PrescriptionItem> = listOf()
    private val _filteredPrescriptionItems = MutableLiveData<List<PrescriptionItem>>()
    val filteredPrescriptionItems: LiveData<List<PrescriptionItem>> = _filteredPrescriptionItems

    private var currentRecordId: Long = -1L

    fun loadAll() {
        currentRecordId = -1L
        viewModelScope.launch(Dispatchers.IO) {
            val list = dao.getAllPrescriptionItems()
            withContext(Dispatchers.Main) {
                allItems = list
                _filteredPrescriptionItems.value = list
            }
        }
    }

    fun loadPrescriptionByRecord(recordId: Long) {
        currentRecordId = recordId
        viewModelScope.launch(Dispatchers.IO) {
            val list = dao.getItemsByRecordId(recordId)
            withContext(Dispatchers.Main) {
                allItems = list
                _filteredPrescriptionItems.value = list
            }
        }
    }

    fun themDonThuoc(item: PrescriptionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(item)
            withContext(Dispatchers.Main) { refreshData() }
        }
    }

    fun search(query: String) {
        val result = if (query.isEmpty()) allItems
        else allItems.filter { it.medicineName.contains(query, ignoreCase = true) }
        _filteredPrescriptionItems.value = result
    }

    suspend fun getById(id: Long): PrescriptionItem? {
        return withContext(Dispatchers.IO) { dao.getItemById(id) }
    }

    fun xoaThuoc(item: PrescriptionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(item)
            withContext(Dispatchers.Main) { refreshData() }
        }
    }

    fun capNhatThuoc(item: PrescriptionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(item)
            withContext(Dispatchers.Main) { refreshData() }
        }
    }

    // ⭐ Đảm bảo hàm này nằm TRONG class DonThuocViewModel
    private fun refreshData() {
        if (currentRecordId != -1L) {
            loadPrescriptionByRecord(currentRecordId)
        } else {
            loadAll()
        }
    }
} // Dấu ngoặc kết thúc class phải nằm SAU refreshData