package com.example.projectqlbenhan.ui.DonThuocUI

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuaDonThuoc : AppCompatActivity() {

    // Khai báo các biến khớp với XML
    private lateinit var edtMaBenhNhan: EditText
    private lateinit var edtTenBenhNhan: EditText
    private lateinit var edtTenThuoc: EditText
    private lateinit var edtDangThuoc: EditText
    private lateinit var edtLieuDung: EditText
    private lateinit var edtSoLanDung: EditText
    private lateinit var edtGhiChu: EditText
    private lateinit var btnCapNhat: Button
    private lateinit var btnXoa: Button
    private lateinit var iconBack: ImageView

    private var currentItemId: Long = -1L
    private var currentRecordId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sua_don_thuoc)

        setControl()
        loadData()
        setEvent()
    }

    private fun setControl() {
        // Ánh xạ ID từ XML vào Code
        edtMaBenhNhan = findViewById(R.id.edtMaBenhNhan)
        edtTenBenhNhan = findViewById(R.id.edtTenBenhNhan)
        edtTenThuoc = findViewById(R.id.edtTenThuoc)
        edtDangThuoc = findViewById(R.id.edtDangThuoc)
        edtLieuDung = findViewById(R.id.edtLieuDung)
        edtSoLanDung = findViewById(R.id.edtSoLanDung)
        edtGhiChu = findViewById(R.id.edtGhiChu)
        btnCapNhat = findViewById(R.id.btnCapNhat)
        btnXoa = findViewById(R.id.btnXoa)
        iconBack = findViewById(R.id.iconBack)
    }

    private fun loadData() {
        currentItemId = intent.getLongExtra("ITEM_ID", -1L)

        // Hiển thị thông tin và khóa các trường định danh bệnh nhân
        edtMaBenhNhan.setText(SessionManager.getSpecificId(this).toString())
        edtTenBenhNhan.setText(SessionManager.getFullName(this))

        if (currentItemId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = MedicalRecordDatabase.getDatabase(this@SuaDonThuoc)
                val item: PrescriptionItem? = db.prescriptionItemDao().getItemById(currentItemId)

                withContext(Dispatchers.Main) {
                    item?.let {
                        currentRecordId = it.recordId
                        edtTenThuoc.setText(it.medicineName)
                        edtDangThuoc.setText(it.unit)
                        edtLieuDung.setText(it.quantity.toString())
                        // Giả sử dosage chứa thông tin liều dùng chi tiết
                        edtGhiChu.setText(it.dosage)
                    }
                }
            }
        }
    }

    private fun setEvent() {
        iconBack.setOnClickListener { finish() }

        btnCapNhat.setOnClickListener {
            updatePrescriptionItem()
        }

        btnXoa.setOnClickListener {
            confirmDelete()
        }
    }

    private fun updatePrescriptionItem() {
        val medicineNameStr = edtTenThuoc.text.toString().trim()
        val quantityStr = edtSoLanDung.text.toString().trim() // Sử dụng edtSoLanDung làm quantity

        if (medicineNameStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = MedicalRecordDatabase.getDatabase(this@SuaDonThuoc)
            val updatedItem = PrescriptionItem(
                itemId = currentItemId,
                recordId = currentRecordId,
                medicineName = medicineNameStr,
                quantity = quantityStr.toIntOrNull() ?: 0,
                unit = edtDangThuoc.text.toString(),
                dosage = edtGhiChu.text.toString()
            )
            db.prescriptionItemDao().updateItem(updatedItem)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@SuaDonThuoc, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa thuốc này?")
            .setPositiveButton("Xóa") { _, _ -> deleteItem() }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteItem() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = MedicalRecordDatabase.getDatabase(this@SuaDonThuoc)
            val itemToDelete = PrescriptionItem(itemId = currentItemId, recordId = 0, medicineName = "", quantity = 0, unit = "", dosage = "")
            db.prescriptionItemDao().deleteSingleItem(itemToDelete)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@SuaDonThuoc, "Đã xóa thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}