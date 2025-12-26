package com.example.projectqlbenhan.ui.DonThuocUI

import android.os.Bundle
import android.widget.*
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

class SuaDonThuoc : AppCompatActivity() {

    private lateinit var edtTenThuoc: EditText
    private lateinit var edtLieuDung: EditText
    private lateinit var edtDangThuoc: EditText
    private lateinit var edtSoLanDung: EditText
    private lateinit var edtGhiChu: EditText
    private lateinit var btnCapNhat: Button
    private lateinit var btnXoa: Button
    private lateinit var iconBack: ImageView

    private lateinit var donThuocViewModel: DonThuocViewModel
    private var editingItem: PrescriptionItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sua_don_thuoc)

        khoiTaoMVVM()
        setControl()

        val itemId = intent.getLongExtra("ITEM_ID", -1L)
        if (itemId != -1L) {
            loadExistingData(itemId)
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu thuốc.", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupEvents()
    }

    private fun khoiTaoMVVM() {
        val database = MedicalRecordDatabase.getDatabase(this)
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory)[DonThuocViewModel::class.java]
    }

    private fun setControl() {
        edtTenThuoc = findViewById(R.id.edtTenThuoc)
        edtLieuDung = findViewById(R.id.edtLieuDung)
        edtDangThuoc = findViewById(R.id.edtDangThuoc)
        edtSoLanDung = findViewById(R.id.edtSoLanDung)
        edtGhiChu = findViewById(R.id.edtGhiChu)
        btnCapNhat = findViewById(R.id.btnCapNhat)
        btnXoa = findViewById(R.id.btnXoa)
        iconBack = findViewById(R.id.iconBack)
    }

    private fun loadExistingData(id: Long) {
        lifecycleScope.launch {
            val item = donThuocViewModel.getById(id)
            if (item != null) {
                editingItem = item
                withContext(Dispatchers.Main) {
                    edtTenThuoc.setText(item.medicineName)
                    edtDangThuoc.setText(item.unit)
                    edtLieuDung.setText(item.dosage)
                    edtSoLanDung.setText(item.quantity.toString())
                    edtGhiChu.setText(item.instruction)
                }
            }
        }
    }

    private fun setupEvents() {
        iconBack.setOnClickListener { finish() }

        btnCapNhat.setOnClickListener {
            val current = editingItem ?: return@setOnClickListener

            val name = edtTenThuoc.text.toString().trim()
            val unit = edtDangThuoc.text.toString().trim()
            val dosage = edtLieuDung.text.toString().trim()
            val qty = edtSoLanDung.text.toString().toIntOrNull() ?: 0
            val note = edtGhiChu.text.toString().trim()

            if (name.isEmpty()) {
                edtTenThuoc.error = "Vui lòng nhập tên thuốc"
                return@setOnClickListener
            }

            // ⭐ Copy dữ liệu mới vào Object cũ để giữ ID
            val updated = current.copy(
                medicineName = name,
                unit = unit,
                dosage = dosage,
                quantity = qty,
                instruction = note
            )

            lifecycleScope.launch(Dispatchers.IO) {
                donThuocViewModel.capNhatThuoc(updated)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SuaDonThuoc, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        btnXoa.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa thuốc '${editingItem?.medicineName}'?")
                .setPositiveButton("Xóa") { _, _ ->
                    editingItem?.let { item ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            donThuocViewModel.xoaThuoc(item)
                            withContext(Dispatchers.Main) {
                                finish()
                            }
                        }
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
}