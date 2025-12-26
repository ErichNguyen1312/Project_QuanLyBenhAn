package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.MedicalRecordDatabase

class DanhSachDonThuoc : AppCompatActivity() {

    private lateinit var donThuocViewModel: DonThuocViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: DonThuocAdapter
    private var currentRecordId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_danh_sach_don_thuoc)

        // 1. Lấy recordId từ Intent sớm (mặc định -1L để load tất cả đơn thuốc)
        currentRecordId = intent.getLongExtra("RECORD_ID", -1L)

        initViews()
        setupViewModel()
        setupRecyclerView()
        setupSearch()
    }

    /**
     * ⭐ GIẢI PHÁP CẬP NHẬT DỮ LIỆU:
     * Hàm onResume sẽ chạy mỗi khi bạn thoát màn hình 'SuaDonThuoc' để quay lại đây.
     * Nó kích hoạt việc tải lại dữ liệu mới nhất từ Database vào ViewModel.
     */
    override fun onResume() {
        super.onResume()
        taiDuLieuMoiNhat()
    }

    private fun taiDuLieuMoiNhat() {
        // ViewModel sẽ thực hiện truy vấn Database dựa trên trạng thái hiện tại
        if (currentRecordId != -1L) {
            donThuocViewModel.loadPrescriptionByRecord(currentRecordId)
        } else {
            donThuocViewModel.loadAll()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewDonThuoc)
        // Ánh xạ SearchView (Đảm bảo XML dùng androidx.appcompat.widget.SearchView)
        searchView = findViewById(R.id.searchViewDonThuoc)
        findViewById<ImageView>(R.id.iconBack).setOnClickListener { finish() }
    }

    private fun setupViewModel() {
        val database = MedicalRecordDatabase.getDatabase(this)
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory)[DonThuocViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // Khởi tạo Adapter với các callback xử lý sự kiện
        adapter = DonThuocAdapter(
            onClick = { item ->
                // Mở màn hình Sửa Đơn Thuốc
                val intent = Intent(this, SuaDonThuoc::class.java).apply {
                    putExtra("ITEM_ID", item.itemId)
                }
                startActivity(intent)
            },
            onDelete = { item ->
                // Xóa thuốc và hiển thị thông báo nhanh
                donThuocViewModel.xoaThuoc(item)
                Toast.makeText(this, "Đã xóa: ${item.medicineName}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DanhSachDonThuoc)
            adapter = this@DanhSachDonThuoc.adapter
        }

        // Lắng nghe LiveData: Khi DB thay đổi, danh sách tự động cập nhật tên mới
        donThuocViewModel.filteredPrescriptionItems.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                donThuocViewModel.search(query ?: "")
                searchView.clearFocus() // Thu bàn phím lại cho gọn màn hình
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                donThuocViewModel.search(newText ?: "")
                return true
            }
        })
    }
}