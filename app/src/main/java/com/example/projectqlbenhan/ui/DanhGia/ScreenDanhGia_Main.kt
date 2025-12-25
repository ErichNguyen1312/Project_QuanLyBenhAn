package com.example.projectqlbenhan.ui.DanhGia

import android.content.Intent // <--- Nhớ import cái này
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.review.ReviewDetail
import com.example.projectqlbenhan.ui.admin.Adapter_DanhGia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenDanhGia_Main : AppCompatActivity() {

    // Khai báo biến
    private lateinit var db: MedicalRecordDatabase
    private lateinit var adapter: Adapter_DanhGia

    // Biến lưu trạng thái lọc
    private var selectedDocId: Long = -1
    private var selectedRating: Int = 0
    private val doctorMap = mutableListOf<Pair<String, Long>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_danh_gia_main)

        // 1. Khởi tạo Database
        db = MedicalRecordDatabase.getDatabase(this)

        // 2. Setup RecyclerView
        setupRecyclerView()

        // 3. Setup nút Back
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish() // Đóng màn hình
        }

        // 4. Setup 2 Spinner và bắt đầu load dữ liệu
        setupSpinners()
    }

    private fun setupRecyclerView() {
        val rcv = findViewById<RecyclerView>(R.id.rcvReviews)
        rcv.layoutManager = LinearLayoutManager(this)

        // Khởi tạo adapter với list rỗng ban đầu + truyền hàm onClickItem
        adapter = Adapter_DanhGia(emptyList(), this::onClickItem)
        rcv.adapter = adapter
    }

    // --- Hàm xử lý sự kiện khi click vào 1 dòng ---
    private fun onClickItem(item: ReviewDetail) {
        val intent = Intent(this, ScreenDanhGia_Detail::class.java)
        intent.putExtra("reviewId", item.reviewId)
        startActivity(intent)
    }
    // ----------------------------------------------

    private fun setupSpinners() {
        // --- A. SPINNER SỐ SAO (RATING) ---
        val listRating = listOf("Tất cả điểm", "5 Sao", "4 Sao", "3 Sao", "2 Sao", "1 Sao")
        val adapterRating = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listRating)
        val spinnerRating = findViewById<Spinner>(R.id.spinnerRating)
        spinnerRating.adapter = adapterRating

        spinnerRating.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRating = if (position == 0) 0 else (6 - position)
                loadReviews()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // Lấy danh sách bác sĩ từ DB
            val doctors = db.doctorDao().getAll()

            withContext(Dispatchers.Main) {
                doctorMap.clear()
                doctorMap.add("Tất cả bác sĩ" to -1L)

                doctors.forEach { doc ->
                    doctorMap.add(doc.fullName to doc.doctorId)
                }

                val docNames = doctorMap.map { it.first }

                val adapterDoc = ArrayAdapter(this@ScreenDanhGia_Main, android.R.layout.simple_spinner_dropdown_item, docNames)
                val spinnerDoctor = findViewById<Spinner>(R.id.spinnerDoctor)
                spinnerDoctor.adapter = adapterDoc

                // Sự kiện chọn Bác sĩ
                spinnerDoctor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedDocId = doctorMap[position].second
                        loadReviews() // Tải lại dữ liệu
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    private fun loadReviews() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Gọi hàm DAO lọc theo ID bác sĩ và Số sao
                val result = db.reviewDao().getFilteredReviews(selectedDocId, selectedRating)

                withContext(Dispatchers.Main) {
                    if (result.isEmpty()) {
                        // Toast.makeText(this@ScreenDanhGia_Main, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show()
                    }
                    // Cập nhật Adapter
                    adapter.updateData(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScreenDanhGia_Main, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}