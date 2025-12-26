package com.example.projectqlbenhan.ui.DonThuocUI

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemDonThuoc : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var edtMaBenhNhan: EditText
    private lateinit var edtTenBenhNhan: EditText
    private lateinit var edtTenThuoc: EditText
    private lateinit var edtDangThuoc: EditText // Đại diện cho 'unit'
    private lateinit var edtLieuDung: EditText  // Đại diện cho 'dosage'
    private lateinit var edtSoLanDung: EditText // Đại diện cho 'quantity'
    private lateinit var edtGhiChu: EditText    // Đại diện cho 'instruction'
    private lateinit var btnLuu: Button
    private lateinit var btnXoa: Button
    private lateinit var iconBack: ImageView

    // --- Khai báo Logic ---
    private lateinit var donThuocViewModel: DonThuocViewModel
    private var currentRecordId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_them_don_thuoc)

        khoiTaoMVVM()
        setControl()
        getIntentDataAndPrepopulate()
        setEvent()
    }

    private fun khoiTaoMVVM() {
        val database = MedicalRecordDatabase.getDatabase(this)
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory)[DonThuocViewModel::class.java]
    }

    private fun setControl() {
        edtMaBenhNhan = findViewById(R.id.edtMaBenhNhan)
        edtTenBenhNhan = findViewById(R.id.edtTenBenhNhan)
        edtTenThuoc = findViewById(R.id.edtTenThuoc)
        edtDangThuoc = findViewById(R.id.edtDangThuoc)
        edtLieuDung = findViewById(R.id.edtLieuDung)
        edtSoLanDung = findViewById(R.id.edtSoLanDung)
        edtGhiChu = findViewById(R.id.edtGhiChu)
        btnLuu = findViewById(R.id.btnLuu)
        btnXoa = findViewById(R.id.btnXoa)
        iconBack = findViewById(R.id.iconBack)
    }

    private fun getIntentDataAndPrepopulate() {
        // Nhận ID Bệnh án để liên kết thuốc vào đúng đơn
        currentRecordId = intent.getLongExtra("RECORD_ID", -1L)

        // Lấy thông tin bệnh nhân từ Session để hiển thị (không cho sửa)
        val patientId = SessionManager.getSpecificId(this)
        val patientName = SessionManager.getFullName(this)

        if (patientId != -1L) {
            edtMaBenhNhan.setText(patientId.toString())
            edtTenBenhNhan.setText(patientName ?: "N/A")
            edtMaBenhNhan.isEnabled = false
            edtTenBenhNhan.isEnabled = false
        }
    }

    private fun setEvent() {
        iconBack.setOnClickListener { finish() }
        btnXoa.setOnClickListener { finish() } // Đóng màn hình nếu hủy
        btnLuu.setOnClickListener { luuDonThuocMoi() }
    }

    private fun luuDonThuocMoi() {
        val name = edtTenThuoc.text.toString().trim()
        val qtyString = edtSoLanDung.text.toString().trim()
        val unitText = edtDangThuoc.text.toString().trim()
        val dose = edtLieuDung.text.toString().trim()
        val instructionText = edtGhiChu.text.toString().trim()

        // 1. Kiểm tra dữ liệu đầu vào
        if (name.isEmpty() || qtyString.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên thuốc và số lượng", Toast.LENGTH_SHORT).show()
            return
        }

        val qty = qtyString.toIntOrNull() ?: 0
        if (qty <= 0) {
            Toast.makeText(this, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Thực hiện lưu dữ liệu vào Database qua ViewModel
        lifecycleScope.launch(Dispatchers.IO) {
            // ⭐ CẬP NHẬT: Khớp chính xác với Entity PrescriptionItem (recordId)
            val item = PrescriptionItem(
                recordId = currentRecordId,    // Khớp với val recordId: Long
                medicineName = name,           // Khớp với val medicineName: String
                dosage = dose,                 // Khớp với val dosage: String
                unit = if (unitText.isNotEmpty()) unitText else "Viên", // Khớp với val unit: String
                quantity = qty,                // Khớp với val quantity: Int
                instruction = instructionText  // Khớp với val instruction: String
            )

            // Gọi hàm trong ViewModel
            donThuocViewModel.themDonThuoc(item)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ThemDonThuoc, "Đã thêm thuốc vào đơn!", Toast.LENGTH_SHORT).show()
                finish() // Quay lại màn hình trước đó
            }
        }
    }
}