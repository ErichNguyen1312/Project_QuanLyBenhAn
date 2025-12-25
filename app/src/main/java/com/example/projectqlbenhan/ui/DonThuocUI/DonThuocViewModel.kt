package com.example.projectqlbenhan.ui.DonThuocUI

import androidx.lifecycle.*
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// ViewModel quản lý nghiệp vụ đơn thuốc sử dụng trực tiếp DAO
class DonThuocViewModel(private val dao: PrescriptionItemDao) : ViewModel() {

    // LiveData chứa Query tìm kiếm từ giao diện
    private val searchQuery = MutableLiveData<String>("")

    // LiveData chứa danh sách thuốc theo Record ID (Bệnh án)
    private val _danhSachThuoc = MutableLiveData<List<PrescriptionItem>>()

    // LiveData công khai chứa kết quả đã lọc để UI quan sát
    val filteredPrescriptionItems = MediatorLiveData<List<PrescriptionItem>>()

    init {
        // Tích hợp logic tìm kiếm khi danh sách thuốc hoặc query thay đổi
        filteredPrescriptionItems.addSource(_danhSachThuoc) { items ->
            filterList(items, searchQuery.value)
        }
        filteredPrescriptionItems.addSource(searchQuery) { query ->
            filterList(_danhSachThuoc.value, query)
        }
    }


    fun loadPrescriptionByRecord(recordId: Long) = viewModelScope.launch(Dispatchers.IO) {
        val items = dao.getItemsByRecordId(recordId)
        _danhSachThuoc.postValue(items)
    }

    private fun filterList(list: List<PrescriptionItem>?, query: String?) {
        if (list == null) return

        if (query.isNullOrEmpty()) {
            filteredPrescriptionItems.value = list
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            filteredPrescriptionItems.value = list.filter {
                it.medicineName.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.unit.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.dosage.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            }
        }
    }

    // Cập nhật query tìm kiếm từ SearchBar
    fun search(query: String) {
        searchQuery.value = query.trim()
    }


    fun themDonThuoc(items: List<PrescriptionItem>) = viewModelScope.launch(Dispatchers.IO) {
        dao.insertPrescriptionItems(items)
    }

    /**
     * Cập nhật thông tin thuốc (Liều dùng, số lượng)
     */
    fun capNhatDonThuoc(item: PrescriptionItem) = viewModelScope.launch(Dispatchers.IO) {
        dao.updateItem(item)
    }

    /**
     * Xóa một loại thuốc khỏi đơn
     */
    fun xoaThuoc(item: PrescriptionItem) = viewModelScope.launch(Dispatchers.IO) {
        dao.deleteSingleItem(item)
    }

    /**
     * Lấy thông tin chi tiết một loại thuốc theo ID
     */
    suspend fun getById(id: Long): PrescriptionItem? = withContext(Dispatchers.IO) {
        return@withContext dao.getItemById(id)
    }
}