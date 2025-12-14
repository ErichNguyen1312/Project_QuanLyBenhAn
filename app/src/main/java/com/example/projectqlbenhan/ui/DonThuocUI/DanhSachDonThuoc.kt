package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository
import com.example.projectqlbenhan.dao.thuoc.PhongKhamDatabase

class   DanhSachDonThuoc : AppCompatActivity() {

    private lateinit var donThuocViewModel: DonThuocViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: DonThuocAdapter

    // ⭐️ FIX: Khai báo nút Quay lại
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
        setupEventListeners() // ⭐️ FIX: Gọi hàm setup sự kiện
    }

    private fun khoiTaoMVVM() {
        val application = requireNotNull(this).application
        val database = PhongKhamDatabase.layDatabase(application)

        val repository = DonThuocRepository(database.donThuocDao())
        val factory = DonThuocViewModelFactory(repository)
        donThuocViewModel = ViewModelProvider(this, factory).get(DonThuocViewModel::class.java)
    }

    private fun setControl() {
        recyclerView = findViewById(R.id.recyclerViewDonThuoc)
        searchView = findViewById(R.id.searchViewDonThuoc)

        // ⭐️ FIX: Ánh xạ nút Quay lại
        iconBack = findViewById(R.id.iconBack)
    }

    private fun setupEventListeners() {
        // ⭐️ FIX: Xử lý sự kiện click nút Quay lại
        iconBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // Giả định DonThuocAdapter constructor nhận Context và lambda click
        adapter = DonThuocAdapter(this) { donThuoc ->
            val intent = Intent(this, ChiTietDonThuoc::class.java).apply {
                putExtra("DON_THUOC_ID", donThuoc.donThuocId)
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        donThuocViewModel.tatCaDonThuoc.observe(this) { donThuocList ->
            donThuocList?.let {
                adapter.submitList(it)

                val currentQuery = searchView.query.toString()
                if (it.isEmpty() && currentQuery.isNotEmpty()) {
                    Toast.makeText(this, "Không tìm thấy đơn thuốc khớp với '$currentQuery'.", Toast.LENGTH_LONG).show()
                } else if (it.isEmpty() && currentQuery.isEmpty()) {
                    Toast.makeText(this, "Chưa có đơn thuốc nào.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                // ⭐️ FIX: Sử dụng hàm search đã được fix trong ViewModel
                donThuocViewModel.search(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // ⭐️ FIX: Sử dụng hàm search đã được fix trong ViewModel
                donThuocViewModel.search(newText ?: "")
                return true
            }
        })
    }
}