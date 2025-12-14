package com.example.projectqlbenhan.ui.DonThuocUI

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity

import java.util.Locale


class DonThuocViewModel(private val repository: DonThuocRepository) : ViewModel() {

    // LiveData gốc (từ Repository)
    private val danhSachGoc = repository.tatCaDonThuoc // LiveData<List<ChiTietDonThuocEntity>>

    // LiveData chứa Query tìm kiếm hiện tại
    private val searchQuery = MutableLiveData<String>()

    // LiveData công khai chứa kết quả đã lọc (dùng MediatorLiveData)
    val tatCaDonThuoc = MediatorLiveData<List<ChiTietDonThuocEntity>>()

    init {
        // Tích hợp logic tìm kiếm
        tatCaDonThuoc.addSource(danhSachGoc) { result ->
            filterList(result, searchQuery.value)
        }
        tatCaDonThuoc.addSource(searchQuery) { query ->
            filterList(danhSachGoc.value, query)
        }
    }

    private fun filterList(list: List<ChiTietDonThuocEntity>?, query: String?) {
        if (list == null) return

        if (query.isNullOrEmpty()) {
            tatCaDonThuoc.value = list
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            val filteredList = list.filter {
                // ⭐️ FIX: Bổ sung tenBenhNhan và bỏ hoSoKhamId
                it.tenThuoc.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.dangThuoc.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.lieuDung.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        (it.ghiChu?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) ?: false) ||
                        it.tenBenhNhan.lowercase(Locale.getDefault()).contains(lowerCaseQuery) // FIX
            }
            tatCaDonThuoc.value = filteredList
        }
    }

    // Hàm gọi từ UI khi người dùng nhập query
    fun search(query: String) {
        searchQuery.value = query.trim()
    }

    // ------------------------------------------------------
    // CÁC HÀM THAO TÁC DATABASE ASYNC (Đã FIX TÊN HÀM)
    // ------------------------------------------------------

    // FIX: Đã đổi tên hàm themDonThuoc thành them (và làm cho nó suspend để trả về ID)
    suspend fun themDonThuocVaLayId(donThuoc: ChiTietDonThuocEntity): Long {
        return repository.them(donThuoc)
    }

    // FIX: Đã đổi tên hàm capNhatDonThuoc thành capNhat
    fun capNhatDonThuoc(donThuoc: ChiTietDonThuocEntity) = viewModelScope.launch {
        repository.capNhat(donThuoc)
    }

    // FIX: Đã đổi tên hàm xoaDonThuoc thành xoaTheoId
    fun xoaDonThuocTheoId(id: Long) = viewModelScope.launch {
        repository.xoaTheoId(id)
    }

    // FIX: Hàm này trả về LiveData
    fun layDonThuocTheoId(id: Long): LiveData<ChiTietDonThuocEntity?> {
        return repository.layDonThuocTheoId(id)
    }

    // ❌ LƯU Ý: Nếu bạn cần hàm lấy Hồ sơ khám bệnh, bạn cần phải giữ lại HoSoKhamBenhDao/Repo.
    // Vì bạn đã xóa chúng, các hàm đó đã bị loại bỏ khỏi ViewModel này.
}