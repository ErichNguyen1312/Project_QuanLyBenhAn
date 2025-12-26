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
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChiTietDonThuoc : AppCompatActivity() {

    private lateinit var tvTenThuoc: TextView
    private lateinit var tvDangThuoc: TextView
    private lateinit var tvLieuDung: TextView
    private lateinit var tvSoLanDung: TextView
    private lateinit var tvSoNgayDung: TextView
    private lateinit var tvGhiChu: TextView
    private lateinit var tvNgayBatDau: TextView
    private lateinit var tvNgayKetThuc: TextView
    private lateinit var tvAlertHetHan: TextView
    private lateinit var iconBack: ImageView
    private lateinit var iconEdit: ImageView
    private lateinit var btnDelete: Button

    private lateinit var donThuocViewModel: DonThuocViewModel
    private var currentItem: PrescriptionItem? = null
    private var itemId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chi_tiet_don_thuoc)

        setControl()
        khoiTaoMVVM()

        // 1. Nhận ID thuốc từ Intent
        itemId = intent.getLongExtra("ITEM_ID", -1L)
        if (itemId != -1L) {
            taiDuLieuChiTiet(itemId)
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID thuốc!", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupEvents()
    }

    /**
     * ⭐ QUAN TRỌNG: Cập nhật lại dữ liệu khi quay lại từ màn hình SuaDonThuoc
     */
    override fun onRestart() {
        super.onRestart()
        if (itemId != -1L) {
            taiDuLieuChiTiet(itemId)
        }
    }

    private fun setControl() {
        tvTenThuoc = findViewById(R.id.tvTenThuoc)
        tvDangThuoc = findViewById(R.id.tvDangThuoc)
        tvLieuDung = findViewById(R.id.tvLieuDung)
        tvSoLanDung = findViewById(R.id.tvSoLanDung)
        tvSoNgayDung = findViewById(R.id.tvSoNgayDung)
        tvGhiChu = findViewById(R.id.tvGhiChu)
        tvNgayBatDau = findViewById(R.id.tvNgayBatDau)
        tvNgayKetThuc = findViewById(R.id.tvNgayKetThuc)
        tvAlertHetHan = findViewById(R.id.tvAlertHetHan)
        iconBack = findViewById(R.id.iconBack)
        iconEdit = findViewById(R.id.iconEdit)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun khoiTaoMVVM() {
        val database = MedicalRecordDatabase.getDatabase(this)
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory)[DonThuocViewModel::class.java]
    }

    private fun taiDuLieuChiTiet(id: Long) {
        lifecycleScope.launch {
            val item = donThuocViewModel.getById(id)
            if (item != null) {
                currentItem = item
                hienThiLenUI(item)
            } else {
                // Trường hợp thuốc đã bị xóa từ màn hình khác
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChiTietDonThuoc, "Dữ liệu thuốc không còn tồn tại!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun hienThiLenUI(item: PrescriptionItem) {
        tvTenThuoc.text = item.medicineName
        tvDangThuoc.text = item.unit
        tvLieuDung.text = item.dosage
        tvSoLanDung.text = "${item.quantity} lần"

        // ⭐ Load ghi chú (instruction)
        tvGhiChu.text = if (item.instruction.isNullOrBlank()) "Không có ghi chú đặc biệt." else item.instruction

        // Hiển thị ngày (Lấy từ trường createdAt trong Entity)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStart = sdf.format(Date(item.createdAt))
        tvNgayBatDau.text = dateStart
        tvNgayKetThuc.text = "Theo chỉ định bác sĩ"

        tvAlertHetHan.visibility = View.GONE
    }

    private fun setupEvents() {
        iconBack.setOnClickListener { finish() }

        // Chuyển sang SuaDonThuoc kèm ID
        iconEdit.setOnClickListener {
            currentItem?.let { item ->
                val intent = Intent(this, SuaDonThuoc::class.java).apply {
                    putExtra("ITEM_ID", item.itemId) // Key đồng bộ: ITEM_ID
                }
                startActivity(intent)
            }
        }

        btnDelete.setOnClickListener { thucHienXoa() }
    }

    private fun thucHienXoa() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa loại thuốc này khỏi đơn không?")
            .setPositiveButton("XÓA") { _, _ ->
                currentItem?.let { item ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        donThuocViewModel.xoaThuoc(item)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ChiTietDonThuoc, "Đã xóa đơn thuốc!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
            .setNegativeButton("HỦY", null)
            .show()
    }
}