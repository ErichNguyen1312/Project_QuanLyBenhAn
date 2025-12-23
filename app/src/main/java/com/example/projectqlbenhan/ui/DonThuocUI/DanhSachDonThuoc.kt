package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView // Sử dụng SearchView của AndroidX để ổn định hơn
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
// Sửa import đúng Database trung tâm
import com.example.projectqlbenhan.database.MedicalRecordDatabase

class DanhSachDonThuoc : AppCompatActivity() {

    private lateinit var donThuocViewModel: DonThuocViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var adapter: DonThuocAdapter
    private lateinit var iconBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_danh_sach_don_thuoc)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        khoiTaoMVVM()
        setControl()
        setupRecyclerView()
        setupSearch()
        setupEventListeners()
    }

    private fun khoiTaoMVVM() {
        // Sử dụng MedicalRecordDatabase thay cho PhongKhamDatabase cũ
        val database = MedicalRecordDatabase.getDatabase(this)

        // Khởi tạo Factory bằng PrescriptionItemDao trực tiếp
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory).get(DonThuocViewModel::class.java)

        // Load toàn bộ thuốc kê từ Database (Ví dụ load recordId mặc định hoặc từ intent)
        donThuocViewModel.loadPrescriptionByRecord(-1L) // -1L để load mặc định hoặc truyền ID thực tế
    }

    private fun setControl() {
        recyclerView = findViewById(R.id.recyclerViewDonThuoc)
        searchView = findViewById(R.id.searchViewDonThuoc)
        iconBack = findViewById(R.id.iconBack)
    }

    private fun setupEventListeners() {
        iconBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Adapter nhận PrescriptionItem thay vì ChiTietDonThuocEntity cũ
        adapter = DonThuocAdapter(this) { item ->
            // Chuyển tới màn hình Sửa Đơn Thuốc
            val intent = Intent(this, SuaDonThuoc::class.java).apply {
                putExtra("ITEM_ID", item.itemId)
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Quan sát LiveData đã lọc từ DonThuocViewModel
        donThuocViewModel.filteredPrescriptionItems.observe(this) { items ->
            items?.let {
                adapter.submitList(it)

                val currentQuery = searchView.query.toString()
                if (it.isEmpty() && currentQuery.isNotEmpty()) {
                    Toast.makeText(this, "Không tìm thấy kết quả cho '$currentQuery'", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                donThuocViewModel.search(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                donThuocViewModel.search(newText ?: "")
                return true
            }
        })
    }
}