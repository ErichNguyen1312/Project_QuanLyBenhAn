package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var btnHuy: Button // Giả định ID là btnXoa

    // ⭐️ FIX: Khai báo nút Quay lại
    private lateinit var iconBack: ImageView

    private lateinit var donThuocViewModel: DonThuocViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_them_don_thuoc)

        khoiTaoMVVM()
        setControl()
        setEvent() // ⭐️ FIX: Gọi hàm setup sự kiện
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

        // ⭐️ FIX: Ánh xạ nút Quay lại
        iconBack = findViewById(R.id.iconBack)
    }

    private fun setEvent() {
        // ⭐️ FIX: Xử lý sự kiện click nút Quay lại
        iconBack.setOnClickListener {
            onBackPressed()
        }

        btnLuu.setOnClickListener {
            luuDonThuocMoi()
        }

        btnHuy.setOnClickListener {
            onBackPressed() // Sử dụng onBackPressed để đóng màn hình
        }
    }

    private fun luuDonThuocMoi() {
        val tenBN = edtTenBenhNhan.text.toString().trim()
        val tenThuoc = edtTenThuoc.text.toString().trim()
        val dangThuoc = edtDangThuoc.text.toString().trim()
        val lieuDung = edtLieuDung.text.toString().trim()
        val soLanDungText = edtSoLanDung.text.toString().trim()
        val ghiChu = edtGhiChu.text.toString().trim()

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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val donThuocMoi = ChiTietDonThuocEntity(
                    tenBenhNhan = tenBN,
                    tenThuoc = tenThuoc,
                    dangThuoc = dangThuoc,
                    lieuDung = lieuDung,
                    soLanDung = soLanDung,
                    ghiChu = if (ghiChu.isEmpty()) null else ghiChu
                )

                val newDonThuocId = donThuocViewModel.themDonThuocVaLayId(donThuocMoi)

                launch(Dispatchers.Main) {
                    if (newDonThuocId > 0) {
                        Toast.makeText(this@ThemDonThuoc, "Đã lưu đơn thuốc thành công!", Toast.LENGTH_LONG).show()

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
                launch(Dispatchers.Main) {
                    Toast.makeText(this@ThemDonThuoc, "Lỗi hệ thống khi lưu: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("ThemDonThuoc", "Lỗi khi lưu đơn thuốc", e)
                }
            }
        }
    }
}