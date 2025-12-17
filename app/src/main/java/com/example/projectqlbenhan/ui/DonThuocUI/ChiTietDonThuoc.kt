package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository
import com.example.projectqlbenhan.dao.thuoc.PhongKhamDatabase
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChiTietDonThuoc : AppCompatActivity() {

    // Khai báo Views
    // Views trong Header
    private lateinit var iconBack: ImageView // ⭐️ FIX: Đã thêm nút Quay lại
    private lateinit var iconEdit: ImageView

    // Views chính
    private lateinit var btnDelete: Button
    private lateinit var tvTenThuoc: TextView
    private lateinit var tvDangThuoc: TextView
    private lateinit var tvLieuDung: TextView
    private lateinit var tvSoLanDung: TextView
    private lateinit var tvSoNgayDung: TextView // ⭐️ Khôi phục khai báo
    private lateinit var tvAlertHetHan: TextView // ⭐️ Khôi phục khai báo
    private lateinit var tvGhiChu: TextView
    private lateinit var tvNgayBatDau: TextView
    private lateinit var tvNgayKetThuc: TextView

    private lateinit var donThuocViewModel: DonThuocViewModel
    private var currentDonThuocId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chi_tiet_don_thuoc)

        currentDonThuocId = intent.getLongExtra("DON_THUOC_ID", -1L)

        khoiTaoMVVM()
        setControl()
        setupEventListeners()

        if (currentDonThuocId != -1L) {
            loadDonThuocData(currentDonThuocId) // ⭐️ FIX: Đổi tên hàm để phù hợp hơn
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID đơn thuốc.", Toast.LENGTH_LONG).show()
        }
    }

    private fun khoiTaoMVVM() {
        val application = requireNotNull(this).application
        val database = PhongKhamDatabase.layDatabase(application)

        val repository = DonThuocRepository(database.donThuocDao())
        val factory = DonThuocViewModelFactory(repository)
        donThuocViewModel = ViewModelProvider(this, factory).get(DonThuocViewModel::class.java)
    }

    private fun setControl() {
        // Views trong Header
        iconBack = findViewById(R.id.iconBack) // ⭐️ FIX: Ánh xạ nút Quay lại
        iconEdit = findViewById(R.id.iconEdit)

        // Views chính
        btnDelete = findViewById(R.id.btnDelete)
        tvTenThuoc = findViewById(R.id.tvTenThuoc)
        tvDangThuoc = findViewById(R.id.tvDangThuoc)
        tvLieuDung = findViewById(R.id.tvLieuDung)
        tvSoLanDung = findViewById(R.id.tvSoLanDung)
        tvSoNgayDung = findViewById(R.id.tvSoNgayDung) // ⭐️ Khôi phục ánh xạ
        tvAlertHetHan = findViewById(R.id.tvAlertHetHan) // ⭐️ Khôi phục ánh xạ
        tvGhiChu = findViewById(R.id.tvGhiChu)
        tvNgayBatDau = findViewById(R.id.tvNgayBatDau)
        tvNgayKetThuc = findViewById(R.id.tvNgayKetThuc)
    }

    private fun setupEventListeners() {
        // ⭐️ FIX: Xử lý sự kiện Quay lại
        iconBack.setOnClickListener {
            onBackPressed()
        }

        iconEdit.setOnClickListener {
            if (currentDonThuocId != -1L) {
                navigateToEditScreen(currentDonThuocId)
            }
        }

        btnDelete.setOnClickListener {
            if (currentDonThuocId != -1L) {
                showDeleteConfirmationDialog()
            }
        }
    }

    private fun loadDonThuocData(id: Long) {
        donThuocViewModel.layDonThuocTheoId(id).observe(this) { donThuoc ->
            if (donThuoc != null) {
                displayDonThuoc(donThuoc)
            } else {
                Toast.makeText(this, "Không tìm thấy chi tiết đơn thuốc.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayDonThuoc(donThuoc: ChiTietDonThuocEntity) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val ngayBatDau = Date(donThuoc.ngayTao)

        // Tính ngày kết thúc (Giả định soLanDung là TỔNG SỐ NGÀY)
        val soNgayDung = donThuoc.soLanDung
        val ngayKetThucMillis = donThuoc.ngayTao + (soNgayDung * 24 * 60 * 60 * 1000L)
        val ngayKetThuc = Date(ngayKetThucMillis)

        // 1. Gán dữ liệu vào TextViews
        tvTenThuoc.text = donThuoc.tenThuoc
        tvDangThuoc.text = donThuoc.dangThuoc
        tvLieuDung.text = donThuoc.lieuDung
        tvSoLanDung.text = donThuoc.soLanDung.toString()
        tvSoNgayDung.text = "${soNgayDung} ngày" // ⭐️ Khôi phục gán giá trị
        tvGhiChu.text = donThuoc.ghiChu ?: "Không có ghi chú."
        tvNgayBatDau.text = dateFormat.format(ngayBatDau)
        tvNgayKetThuc.text = dateFormat.format(ngayKetThuc)

        // 2. ⭐️ FIX: Khôi phục Logic kiểm tra cảnh báo
        checkExpiryAlert(ngayKetThucMillis)
    }

    private fun checkExpiryAlert(ngayKetThucMillis: Long) {
        val today = System.currentTimeMillis()
        val diffDays = (ngayKetThucMillis - today) / (24 * 60 * 60 * 1000)

        if (diffDays <= 3 && diffDays >= 0) {
            tvAlertHetHan.text = "⚠️ Sắp hết hạn - Còn ${diffDays} ngày"
            tvAlertHetHan.visibility = View.VISIBLE
        } else if (diffDays < 0) {
            tvAlertHetHan.text = "⚠️ Đã hết hạn (Hạn dùng: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ngayKetThucMillis))})"
            tvAlertHetHan.visibility = View.VISIBLE
        } else {
            tvAlertHetHan.visibility = View.GONE
        }
    }

    private fun navigateToEditScreen(id: Long) {
        val intent = Intent(this, SuaDonThuoc::class.java).apply {
            putExtra("DON_THUOC_ID", id)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xóa Đơn Thuốc")
            .setMessage("Bạn có chắc chắn muốn xóa đơn thuốc ID $currentDonThuocId không?")
            .setPositiveButton("XÓA") { dialog, which ->
                donThuocViewModel.xoaDonThuocTheoId(currentDonThuocId)
                Toast.makeText(this, "Đơn thuốc đã được xóa thành công.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("HỦY", null)
            .show()
    }
}