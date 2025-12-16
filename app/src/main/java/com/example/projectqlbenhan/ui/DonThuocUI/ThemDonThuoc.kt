package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository
import com.example.projectqlbenhan.dao.thuoc.PhongKhamDatabase
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity
import com.example.projectqlbenhan.utils.SessionManager // ⭐ Cần import SessionManager
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

    private lateinit var donThuocViewModel: DonThuocViewModel

    // ⭐ Biến lưu Record ID (nếu được truyền)
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
        val application = requireNotNull(this).application
        val database = PhongKhamDatabase.layDatabase(application)

        val donThuocRepo = DonThuocRepository(database.donThuocDao())
        val factory = DonThuocViewModelFactory(donThuocRepo)
        donThuocViewModel = ViewModelProvider(this, factory).get(DonThuocViewModel::class.java)
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

    // ⭐ FIX: Lấy dữ liệu từ Intent và Session để điền sẵn
    private fun getIntentDataAndPrepopulate() {
        currentRecordId = intent.getLongExtra("RECORD_ID", -1L)

        // Đọc thông tin Bệnh nhân từ Session Manager
        val patientIdFromSession = SessionManager.getCurrentPatientId(this)
        val patientNameFromSession = SessionManager.getCurrentPatientName(this)

        // Tự động gán và khóa trường Bệnh nhân nếu dữ liệu có sẵn trong Session
        if (patientIdFromSession != -1L && !patientNameFromSession.isNullOrEmpty()) {

            // 1. Gán ID Bệnh nhân và KHÓA
            edtMaBenhNhan.setText(patientIdFromSession.toString())
            edtMaBenhNhan.isEnabled = false
            edtMaBenhNhan.isFocusable = false

            // 2. Gán Tên Bệnh nhân và KHÓA
            edtTenBenhNhan.setText(patientNameFromSession)
            edtTenBenhNhan.isEnabled = false
            edtTenBenhNhan.isFocusable = false
        } else {
            // Nếu Session rỗng, cho phép người dùng nhập bình thường
            edtMaBenhNhan.isEnabled = true
            edtTenBenhNhan.isEnabled = true
        }
    }


    private fun setEvent() {
        iconBack.setOnClickListener {
            onBackPressed()
        }

        btnLuu.setOnClickListener {
            luuDonThuocMoi()
        }

        btnHuy.setOnClickListener {
            onBackPressed()
        }
    }

    private fun luuDonThuocMoi() {

        // 1. Lấy tên BN từ Session (nếu đã điền sẵn) hoặc từ EditText (nếu chưa điền sẵn)
        val tenBN = SessionManager.getCurrentPatientName(this) ?: edtTenBenhNhan.text.toString().trim()
        val tenThuoc = edtTenThuoc.text.toString().trim()
        val dangThuoc = edtDangThuoc.text.toString().trim()
        val lieuDung = edtLieuDung.text.toString().trim()
        val soLanDungText = edtSoLanDung.text.toString().trim()
        val ghiChu = edtGhiChu.text.toString().trim()

        // Validation
        if (tenBN.isEmpty() || tenThuoc.isEmpty() || dangThuoc.isEmpty() || lieuDung.isEmpty() || soLanDungText.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ các trường bắt buộc.", Toast.LENGTH_LONG).show()
            return
        }

        val soLanDung: Int
        try {
            soLanDung = soLanDungText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Số lần dùng không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val donThuocMoi = ChiTietDonThuocEntity(
                    tenBenhNhan = tenBN,
                    // ... (Thêm recordId nếu Entity Prescription của bạn có trường này) ...

                    tenThuoc = tenThuoc,
                    dangThuoc = dangThuoc,
                    lieuDung = lieuDung,
                    soLanDung = soLanDung,
                    ghiChu = if (ghiChu.isEmpty()) null else ghiChu
                )

                val newDonThuocId = donThuocViewModel.themDonThuocVaLayId(donThuocMoi)

                withContext(Dispatchers.Main) {
                    if (newDonThuocId > 0) {
                        Toast.makeText(this@ThemDonThuoc, "Đã lưu đơn thuốc thành công!", Toast.LENGTH_LONG).show()

                        // ⭐ DỌN DẸP SESSION sau khi lưu thành công
                        SessionManager.clearCurrentPatientInfo(this@ThemDonThuoc)

                        val intent = Intent(this@ThemDonThuoc, ChiTietDonThuoc::class.java).apply {
                            putExtra("DON_THUOC_ID", newDonThuocId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ThemDonThuoc, "Lưu đơn thuốc thất bại (ID = 0).", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ThemDonThuoc, "Lỗi hệ thống khi lưu: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("ThemDonThuoc", "Lỗi khi lưu đơn thuốc", e)
                }
            }
        }
    }
}