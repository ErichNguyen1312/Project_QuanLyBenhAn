package com.example.projectqlbenhan.ui.DonThuocUI

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository
import com.example.projectqlbenhan.dao.thuoc.PhongKhamDatabase
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.NumberFormatException

class SuaDonThuoc : AppCompatActivity() {

    // Khai báo Views Đơn thuốc
    private lateinit var edtTenThuoc: EditText
    private lateinit var edtLieuDung: EditText
    private lateinit var edtDangThuoc: EditText
    private lateinit var edtSoLanDung: EditText
    private lateinit var edtGhiChu: EditText
    private lateinit var btnCapNhat: Button
    private lateinit var btnXoa: Button

    // ⭐️ FIX: Khai báo nút Quay lại
    private lateinit var iconBack: ImageView

    private lateinit var donThuocViewModel: DonThuocViewModel
    private var editingDonThuoc: ChiTietDonThuocEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sua_don_thuoc)

        khoiTaoMVVM()
        setControl()

        val donThuocId = intent.getLongExtra("DON_THUOC_ID", -1L)

        if (donThuocId != -1L) {
            loadExistingData(donThuocId)
            setupEditModeEvents()
        } else {
            // FIX: Thông báo lỗi rõ ràng và đóng Activity
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
        // ❌ FIX: KHÔNG Ánh xạ các Views Bệnh nhân đã bị xóa
        // edtMaBenhNhan = findViewById(R.id.edtMaBenhNhan)
        // edtTenBenhNhan = findViewById(R.id.edtTenBenhNhan)

        edtTenThuoc = findViewById(R.id.edtTenThuoc)
        edtLieuDung = findViewById(R.id.edtLieuDung)
        edtDangThuoc = findViewById(R.id.edtDangThuoc)
        edtSoLanDung = findViewById(R.id.edtSoLanDung)
        edtGhiChu = findViewById(R.id.edtGhiChu)

        btnCapNhat = findViewById(R.id.btnCapNhat)
        btnXoa = findViewById(R.id.btnXoa)

        // ⭐️ FIX: Ánh xạ nút Quay lại
        iconBack = findViewById(R.id.iconBack)
    }

    private fun loadExistingData(id: Long) {
        donThuocViewModel.layDonThuocTheoId(id).observe(this) { donThuoc ->
            if (donThuoc != null) {
                editingDonThuoc = donThuoc

                // ❌ Nếu bạn đã giữ edtTenBenhNhan trong XML nhưng không muốn sửa:
                // findViewById<EditText>(R.id.edtTenBenhNhan).setText(donThuoc.tenBenhNhan)

                // Điền dữ liệu vào form
                edtTenThuoc.setText(donThuoc.tenThuoc)
                edtDangThuoc.setText(donThuoc.dangThuoc)
                edtLieuDung.setText(donThuoc.lieuDung)
                edtSoLanDung.setText(donThuoc.soLanDung.toString())
                edtGhiChu.setText(donThuoc.ghiChu)
                btnCapNhat.text = "CẬP NHẬT"
            } else {
                Toast.makeText(this, "Không tìm thấy chi tiết đơn thuốc.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupEditModeEvents() {
        // ⭐️ FIX: Xử lý sự kiện click nút Quay lại
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

        // ⭐️ FIX: Đảm bảo không null trước khi tiếp tục
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

        // ⭐️ FIX: Sử dụng hàm copy() với các trường dữ liệu còn tồn tại
        val updatedDonThuoc = currentRx.copy(
            // Giữ nguyên tenBenhNhan vì không cho sửa trên màn hình này
            tenBenhNhan = currentRx.tenBenhNhan,
            tenThuoc = edtTenThuoc.text.toString().trim(),
            dangThuoc = edtDangThuoc.text.toString().trim(),
            lieuDung = edtLieuDung.text.toString().trim(),
            soLanDung = soLanDung,
            ghiChu = edtGhiChu.text.toString().trim()
        )

        donThuocViewModel.capNhatDonThuoc(updatedDonThuoc)
        Toast.makeText(this, "Đã cập nhật đơn thuốc thành công!", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xóa Đơn Thuốc")
            .setMessage("Bạn có chắc chắn muốn xóa đơn thuốc này không?")
            .setPositiveButton("XÓA") { _, _ ->
                editingDonThuoc?.let { rx ->
                    donThuocViewModel.xoaDonThuocTheoId(rx.donThuocId)
                    Toast.makeText(this, "Đã xóa đơn thuốc.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("HỦY", null)
            .show()
    }
}