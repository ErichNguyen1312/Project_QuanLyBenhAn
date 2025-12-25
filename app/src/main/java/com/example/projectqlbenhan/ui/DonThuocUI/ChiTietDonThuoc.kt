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
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChiTietDonThuoc : AppCompatActivity() {

    private lateinit var iconBack: ImageView
    private lateinit var iconEdit: ImageView
    private lateinit var btnDelete: Button
    private lateinit var tvTenThuoc: TextView
    private lateinit var tvDangThuoc: TextView
    private lateinit var tvLieuDung: TextView
    private lateinit var tvSoLanDung: TextView
    private lateinit var tvSoNgayDung: TextView
    private lateinit var tvAlertHetHan: TextView
    private lateinit var tvGhiChu: TextView
    private lateinit var tvNgayBatDau: TextView
    private lateinit var tvNgayKetThuc: TextView

    private lateinit var donThuocViewModel: DonThuocViewModel
    private var currentItemId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chi_tiet_don_thuoc)

        currentItemId = intent.getLongExtra("ITEM_ID", -1L)

        khoiTaoMVVM()
        setControl()
        setupEventListeners()

        if (currentItemId != -1L) {
            loadPrescriptionData(currentItemId)
        }
    }

    private fun khoiTaoMVVM() {
        val database = MedicalRecordDatabase.getDatabase(this)
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory).get(DonThuocViewModel::class.java)
    }

    private fun setControl() {
        iconBack = findViewById(R.id.iconBack)
        iconEdit = findViewById(R.id.iconEdit)
        btnDelete = findViewById(R.id.btnDelete)
        tvTenThuoc = findViewById(R.id.tvTenThuoc)
        tvDangThuoc = findViewById(R.id.tvDangThuoc)
        tvLieuDung = findViewById(R.id.tvLieuDung)
        tvSoLanDung = findViewById(R.id.tvSoLanDung)
        tvSoNgayDung = findViewById(R.id.tvSoNgayDung)
        tvAlertHetHan = findViewById(R.id.tvAlertHetHan)
        tvGhiChu = findViewById(R.id.tvGhiChu)
        tvNgayBatDau = findViewById(R.id.tvNgayBatDau)
        tvNgayKetThuc = findViewById(R.id.tvNgayKetThuc)
    }

    private fun setupEventListeners() {
        iconBack.setOnClickListener { finish() }

        iconEdit.setOnClickListener {
            if (currentItemId != -1L) {
                val intent = Intent(this, SuaDonThuoc::class.java).apply {
                    putExtra("ITEM_ID", currentItemId)
                }
                startActivity(intent)
            }
        }

        btnDelete.setOnClickListener {
            if (currentItemId != -1L) {
                showDeleteConfirmationDialog()
            }
        }
    }

    // ⭐ SỬA LỖI: Sử dụng lifecycleScope.launch để gọi hàm suspend getById
    private fun loadPrescriptionData(id: Long) {
        lifecycleScope.launch {
            val item = donThuocViewModel.getById(id)
            if (item != null) {
                displayPrescription(item)
            } else {
                Toast.makeText(this@ChiTietDonThuoc, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayPrescription(item: PrescriptionItem) {
        tvTenThuoc.text = item.medicineName
        tvDangThuoc.text = item.unit
        tvLieuDung.text = item.dosage
        tvSoLanDung.text = item.quantity.toString()
        tvGhiChu.text = item.dosage

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvNgayBatDau.text = dateFormat.format(Date())
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xác nhận")
            .setMessage("Bạn có chắc chắn muốn xóa thuốc này?")
            .setPositiveButton("Xóa") { _, _ ->
                // ⭐ SỬA LỖI: Gọi hàm xóa trong Coroutine
                lifecycleScope.launch {
                    val item = donThuocViewModel.getById(currentItemId)
                    item?.let {
                        donThuocViewModel.xoaThuoc(it)
                        Toast.makeText(this@ChiTietDonThuoc, "Đã xóa", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}