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

    // Biến lưu lại trạng thái đang lọc theo RecordId hay xem tất cả
    private var currentRecordId: Long = -1L

    /**
     * Tải toàn bộ đơn thuốc (Dashboard/Tất cả)
     */
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

    /**
     * Tải thuốc theo mã bệnh án cụ thể
     */
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

    /**
     * Thêm mới thuốc
     */
    fun themDonThuoc(item: PrescriptionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(item)
            // Chuyển về Main Thread để refresh dữ liệu đồng bộ
            withContext(Dispatchers.Main) {
                refreshData()
            }
        }
    }

    /**
     * Tìm kiếm thuốc trong danh sách đã tải
     */
    fun search(query: String) {
        val result = if (query.isEmpty()) allItems
        else allItems.filter { it.medicineName.contains(query, ignoreCase = true) }
        _filteredPrescriptionItems.value = result
    }

    /**
     * Lấy chi tiết thuốc theo ID (dùng cho màn hình Sửa)
     */
    suspend fun getById(id: Long): PrescriptionItem? {
        return withContext(Dispatchers.IO) { dao.getItemById(id) }
    }

    /**
     * Xóa thuốc
     */
    fun xoaThuoc(item: PrescriptionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(item)
            withContext(Dispatchers.Main) {
                refreshData()
            }
        }
    }

    /**
     * ⭐ FIX DỨT ĐIỂM: Cập nhật thuốc và đồng bộ hóa LiveData
     */
    fun capNhatThuoc(item: PrescriptionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Thực hiện lệnh cập nhật trong Database
            dao.update(item)

            // 2. Quay về Main Thread để cập nhật giao diện
            withContext(Dispatchers.Main) {
                // Tải lại dữ liệu mới nhất từ DB để làm mới biến 'allItems' và LiveData
                refreshData()
            }
        }
    }

    /**
     * Hàm hỗ trợ tự động nhận diện chế độ tải dữ liệu
     */
    private fun refreshData() {
        if (currentRecordId != -1L) {
            loadPrescriptionByRecord(currentRecordId)
        } else {
            loadAll()
        }
    }
}