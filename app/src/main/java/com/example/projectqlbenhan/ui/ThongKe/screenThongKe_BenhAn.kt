package com.example.projectqlbenhan.ui.ThongKe

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class screenThongKe_BenhAn : AppCompatActivity() {

    private lateinit var spDiseaseType: Spinner
    private lateinit var ct_btnBack: ImageButton
    private lateinit var ct_tvTieuDe: TextView
    private lateinit var lvMedicalRecord: ListView
    private lateinit var tvTotalCount: TextView // Text đếm số lượng

    private lateinit var adapter: Adapter_ThongKeBenhAn
    private lateinit var db: MedicalRecordDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_thong_ke_benh_an)

        // Khởi tạo Database
        db = MedicalRecordDatabase.getDatabase(this)

        initView()
        setupSpinnerData() // Load dữ liệu cho Spinner
        setEvent()
    }

    private fun initView() {
        spDiseaseType = findViewById(R.id.spDiseaseType)
        lvMedicalRecord = findViewById(R.id.lvMedicalRecord)
        tvTotalCount = findViewById(R.id.tvTotalCount) // ID mới thêm trong XML

        // Toolbar custom
        ct_btnBack = findViewById(R.id.btnBack) // Kiểm tra lại ID trong custom_toolbar
        ct_tvTieuDe = findViewById(R.id.tvTitle) // Kiểm tra lại ID trong custom_toolbar
        ct_tvTieuDe.text = "Thống kê Bệnh án - Trần Thiện Trí"

        // Khởi tạo Adapter rỗng ban đầu
        adapter = Adapter_ThongKeBenhAn(this, emptyList())
        lvMedicalRecord.adapter = adapter
    }

    private fun setupSpinnerData() {
        lifecycleScope.launch {
            // Lấy danh sách các loại bệnh (Diagnosis) duy nhất từ DB để làm bộ lọc
            val distinctTypes = withContext(Dispatchers.IO) {
                // Gọi hàm DAO lấy danh sách bệnh duy nhất
                // Nếu MedicalRecordDao chưa có hàm getAllDiseaseTypes, hãy thêm:
                // @Query("SELECT DISTINCT diagnosis FROM medical_records")
                db.medicalRecordDao().getAllDiseaseTypes()
            }

            // Tạo list cho Spinner, thêm mục "Tất cả" vào đầu
            val spinnerItems = mutableListOf("Tất cả")
            spinnerItems.addAll(distinctTypes)

            val spinnerAdapter = ArrayAdapter(
                this@screenThongKe_BenhAn,
                android.R.layout.simple_spinner_item,
                spinnerItems
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spDiseaseType.adapter = spinnerAdapter
        }
    }

    private fun setEvent() {
        // Nút Back
        ct_btnBack.setOnClickListener {
            finish() // Đóng Activity quay về màn hình trước
        }

        // Sự kiện chọn Spinner
        spDiseaseType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                filterData(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Không làm gì
            }
        }
    }

    private fun filterData(type: String) {
        lifecycleScope.launch {
            val listRecord = withContext(Dispatchers.IO) {
                if (type == "Tất cả") {
                    db.medicalRecordDao().getAll()
                } else {
                    // Tìm kiếm gần đúng hoặc chính xác theo loại bệnh
                    db.medicalRecordDao().getByDiseaseType(type)
                }
            }

            // Cập nhật UI trên Main Thread
            adapter.updateData(listRecord)
            tvTotalCount.text = "Tìm thấy: ${listRecord.size} hồ sơ"
        }
    }
}