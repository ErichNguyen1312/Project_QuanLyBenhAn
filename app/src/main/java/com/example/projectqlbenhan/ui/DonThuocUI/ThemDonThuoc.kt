package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase

import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem // Sử dụng Entity mới
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NumberFormatException

class ThemDonThuoc : AppCompatActivity() {

    private lateinit var edtMaBenhNhan: EditText
    private lateinit var edtTenBenhNhan: EditText
    private lateinit var edtTenThuoc: EditText
    private lateinit var edtDangThuoc: EditText
    private lateinit var edtLieuDung: EditText
    private lateinit var edtSoLanDung: EditText
    private lateinit var edtGhiChu: EditText
    private lateinit var btnLuu: Button
    private lateinit var btnHuy: Button
    private lateinit var iconBack: ImageView

    private var currentRecordId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_them_don_thuoc)

        setControl()
        getPatientDataAndPrepopulate() // Tự động điền thông tin bệnh nhân
        setEvent()
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
        btnHuy = findViewById(R.id.btnXoa)
        iconBack = findViewById(R.id.iconBack)
    }

    /**
     * Lấy dữ liệu từ SessionManager để điền sẵn vào Form và khóa chỉnh sửa
     */
    private fun getPatientDataAndPrepopulate() {
        // Lấy Record ID từ Intent nếu chuyển từ màn hình Chi tiết bệnh án
        currentRecordId = intent.getLongExtra("RECORD_ID", -1L)

        // Đọc thông tin Bệnh nhân từ Session Manager đã lưu trước đó
        val patientIdFromSession = SessionManager.getSpecificId(this)
        val patientNameFromSession = SessionManager.getFullName(this)

        if (patientIdFromSession != -1L && !patientNameFromSession.isNullOrEmpty()) {
            // 1. Gán ID Bệnh nhân và KHÓA trường nhập
            edtMaBenhNhan.setText(patientIdFromSession.toString())
            edtMaBenhNhan.isEnabled = false
            edtMaBenhNhan.isFocusable = false

            // 2. Gán Tên Bệnh nhân và KHÓA trường nhập
            edtTenBenhNhan.setText(patientNameFromSession)
            edtTenBenhNhan.isEnabled = false
            edtTenBenhNhan.isFocusable = false
        }
    }

    private fun setEvent() {
        iconBack.setOnClickListener { finish() }
        btnHuy.setOnClickListener { finish() }
        btnLuu.setOnClickListener { luuDonThuocMoi() }
    }

    private fun luuDonThuocMoi() {
        val tenThuoc = edtTenThuoc.text.toString().trim()
        val dangThuoc = edtDangThuoc.text.toString().trim()
        val lieuDung = edtLieuDung.text.toString().trim() // Dosage
        val quantityStr = edtSoLanDung.text.toString().trim() // Số lượng
        val ghiChu = edtGhiChu.text.toString().trim()

        // Kiểm tra dữ liệu bắt buộc
        if (tenThuoc.isEmpty() || quantityStr.isEmpty() || lieuDung.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ thông tin thuốc, liều dùng và số lượng.", Toast.LENGTH_LONG).show()
            return
        }

        val quantity: Int
        try {
            quantity = quantityStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Số lượng không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = MedicalRecordDatabase.getDatabase(this@ThemDonThuoc)

                // Khởi tạo Entity mới theo cấu trúc bảng prescription_items
                val newItem = PrescriptionItem(
                    recordId = currentRecordId,
                    medicineName = tenThuoc,
                    quantity = quantity,
                    unit = dangThuoc,
                    dosage = lieuDung,
                    // Lưu ý: Trường ghi chú có thể gán vào dosage hoặc mở rộng Entity nếu cần
                )

                // Lưu vào Database thông qua DAO
                db.prescriptionItemDao().insertPrescriptionItems(listOf(newItem))

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ThemDonThuoc, "Đã lưu đơn thuốc thành công!", Toast.LENGTH_LONG).show()

                    // ⭐ DỌN DẸP SESSION sau khi lưu thành công để đảm bảo an toàn dữ liệu cho lần sau
                    SessionManager.clearSelectedPatient(this@ThemDonThuoc)

                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ThemDonThuoc, "Lỗi khi lưu: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("ThemDonThuoc", "Lỗi DB", e)
                }
            }
        }
    }
}