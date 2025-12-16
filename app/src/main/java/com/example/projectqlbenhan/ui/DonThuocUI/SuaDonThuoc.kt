package com.example.projectqlbenhan.ui.DonThuocUI

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope // ⭐ Cần import lifecycleScope
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository
import com.example.projectqlbenhan.dao.thuoc.PhongKhamDatabase
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity
import com.example.projectqlbenhan.utils.SessionManager // ⭐ Cần import SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NumberFormatException
class SuaDonThuoc : AppCompatActivity() {

    // ⭐ THÊM: Views Bệnh nhân
    private lateinit var edtMaBenhNhan: EditText
    private lateinit var edtTenBenhNhan: EditText

    // Khai báo Views Đơn thuốc
    private lateinit var edtTenThuoc: EditText
    private lateinit var edtLieuDung: EditText
    private lateinit var edtDangThuoc: EditText
    private lateinit var edtSoLanDung: EditText
    private lateinit var edtGhiChu: EditText
    private lateinit var btnCapNhat: Button
    private lateinit var btnXoa: Button

    private lateinit var iconBack: ImageView

    private lateinit var donThuocViewModel: DonThuocViewModel
    private var editingDonThuoc: ChiTietDonThuocEntity? = null

    // ⭐ Biến lưu tạm Tên Bệnh nhân từ Session (để đảm bảo không bị mất khi Update)
    private var currentPatientNameFromSession: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sua_don_thuoc)

        khoiTaoMVVM()
        setControl()

        val donThuocId = intent.getLongExtra("DON_THUOC_ID", -1L)

        if (donThuocId != -1L) {
            // ⭐ GỌI HÀM MỚI: Tự động điền dữ liệu Bệnh nhân
            getPatientDataAndPrepopulate()

            // Tải dữ liệu Đơn thuốc cũ
            loadExistingData(donThuocId)
            setupEditModeEvents()
        } else {
            Toast.makeText(this, "Lỗi: Không có ID đơn thuốc để sửa.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun khoiTaoMVVM() {
        val application = requireNotNull(this).application
        val database = PhongKhamDatabase.layDatabase(application)
        val repo = DonThuocRepository(database.donThuocDao())
        val factory = DonThuocViewModelFactory(repo)
        donThuocViewModel = ViewModelProvider(this, factory).get(DonThuocViewModel::class.java)
    }

    private fun setControl() {
        // ⭐ FIX: Ánh xạ Views Bệnh nhân
        edtMaBenhNhan = findViewById(R.id.edtMaBenhNhan)
        edtTenBenhNhan = findViewById(R.id.edtTenBenhNhan)

        edtTenThuoc = findViewById(R.id.edtTenThuoc)
        edtLieuDung = findViewById(R.id.edtLieuDung)
        edtDangThuoc = findViewById(R.id.edtDangThuoc)
        edtSoLanDung = findViewById(R.id.edtSoLanDung)
        edtGhiChu = findViewById(R.id.edtGhiChu)

        btnCapNhat = findViewById(R.id.btnCapNhat)
        btnXoa = findViewById(R.id.btnXoa)

        iconBack = findViewById(R.id.iconBack)
    }

    // ⭐ HÀM MỚI: Tự động điền ID và Tên Bệnh nhân từ Session
    private fun getPatientDataAndPrepopulate() {
        val patientIdFromSession = SessionManager.getCurrentPatientId(this)
        val patientNameFromSession = SessionManager.getCurrentPatientName(this)

        // Lưu tên BN vào biến tạm
        currentPatientNameFromSession = patientNameFromSession

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
            // Nếu Session rỗng, vẫn cho phép người dùng nhìn thấy giá trị cũ (sẽ được điền trong loadExistingData)
            edtMaBenhNhan.isEnabled = true
            edtTenBenhNhan.isEnabled = true
            Toast.makeText(this, "Không tìm thấy bệnh nhân đang thao tác, cho phép nhập.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExistingData(id: Long) {
        donThuocViewModel.layDonThuocTheoId(id).observe(this) { donThuoc ->
            if (donThuoc != null) {
                editingDonThuoc = donThuoc

                // Điền dữ liệu vào form
                edtTenThuoc.setText(donThuoc.tenThuoc)
                edtDangThuoc.setText(donThuoc.dangThuoc)
                edtLieuDung.setText(donThuoc.lieuDung)
                edtSoLanDung.setText(donThuoc.soLanDung.toString())
                edtGhiChu.setText(donThuoc.ghiChu)
                btnCapNhat.text = "CẬP NHẬT"

                // ⭐ FIX: Nếu Session rỗng, phải điền giá trị tên BN cũ vào đây
                if (edtTenBenhNhan.text.isEmpty()) {
                    edtTenBenhNhan.setText(donThuoc.tenBenhNhan)
                }

            } else {
                Toast.makeText(this, "Không tìm thấy chi tiết đơn thuốc.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupEditModeEvents() {
        iconBack.setOnClickListener {
            onBackPressed()
        }

        btnCapNhat.setOnClickListener {
            capNhatDonThuoc()
        }

        btnXoa.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun capNhatDonThuoc() {
        val currentRx = editingDonThuoc

        if (currentRx == null) {
            Toast.makeText(this, "Lỗi: Không có dữ liệu để cập nhật.", Toast.LENGTH_LONG).show()
            return
        }

        val soLanDung: Int
        try {
            soLanDung = edtSoLanDung.text.toString().toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Số lần dùng không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐ Lấy tên BN: Ưu tiên tên từ Session, nếu không có thì lấy tên cũ từ Entity
        val finalTenBenhNhan = currentPatientNameFromSession ?: currentRx.tenBenhNhan


        val updatedDonThuoc = currentRx.copy(
            tenBenhNhan = finalTenBenhNhan, // ⭐ FIX: Sử dụng tên bệnh nhân đã được xác định
            tenThuoc = edtTenThuoc.text.toString().trim(),
            dangThuoc = edtDangThuoc.text.toString().trim(),
            lieuDung = edtLieuDung.text.toString().trim(),
            soLanDung = soLanDung,
            ghiChu = edtGhiChu.text.toString().trim()
        )

        // ⭐ SỬ DỤNG LIFECYCLE SCOPE VÀO LUỒNG NỀN
        lifecycleScope.launch(Dispatchers.IO) {
            donThuocViewModel.capNhatDonThuoc(updatedDonThuoc)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@SuaDonThuoc, "Đã cập nhật đơn thuốc thành công!", Toast.LENGTH_LONG).show()

                // ⭐ DỌN DẸP SESSION sau khi cập nhật thành công
                SessionManager.clearCurrentPatientInfo(this@SuaDonThuoc)
                finish()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xóa Đơn Thuốc")
            .setMessage("Bạn có chắc chắn muốn xóa đơn thuốc này không?")
            .setPositiveButton("XÓA") { _, _ ->
                editingDonThuoc?.let { rx ->
                    // ⭐ SỬ DỤNG LIFECYCLE SCOPE VÀO LUỒNG NỀN
                    lifecycleScope.launch(Dispatchers.IO) {
                        donThuocViewModel.xoaDonThuocTheoId(rx.donThuocId)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SuaDonThuoc, "Đã xóa đơn thuốc.", Toast.LENGTH_SHORT).show()

                            // ⭐ DỌN DẸP SESSION sau khi xóa thành công
                            SessionManager.clearCurrentPatientInfo(this@SuaDonThuoc)
                            finish()
                        }
                    }
                }
            }
            .setNegativeButton("HỦY", null)
            .show()
    }
}