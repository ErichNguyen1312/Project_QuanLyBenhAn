package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.projectqlbenhan.R
// Import đúng Database và Entity đã thống nhất
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DonThuoc : AppCompatActivity() {

    // Khai báo Views
    private lateinit var iconSearch: ImageView
    private lateinit var iconAdd: ImageView
    private lateinit var recentPrescriptionCard: CardView
    private lateinit var iconPerson: ImageView
    private lateinit var iconPill: ImageView

    private lateinit var cardTotalRx: CardView
    private lateinit var cardTotalPatients: CardView
    private lateinit var cardExpiring: CardView
    private lateinit var cardCompleted: CardView

    // Khai báo TextViews hiển thị đơn thuốc
    private lateinit var tvRecentMaBenhNhan: TextView
    private lateinit var tvRecentTenBenhNhan: TextView
    private lateinit var tvRecentNgayKetThuc: TextView

    // Khai báo ViewModel quản lý dữ liệu
    private lateinit var donThuocViewModel: DonThuocViewModel
    private var latestItemId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_don_thuoc)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        khoiTaoMVVM()
        setControl()
        setEvent()
        applyColorFixes()

        // Quan sát dữ liệu thuốc gần nhất
        observeRecentPrescription()
    }

    private fun khoiTaoMVVM() {
        // Sử dụng lớp Database trung tâm thay cho PhongKhamDatabase cũ
        val database = MedicalRecordDatabase.getDatabase(this)

        // Factory khởi tạo trực tiếp từ DAO quản lý đơn thuốc
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory).get(DonThuocViewModel::class.java)
    }

    private fun setControl() {
        iconSearch = findViewById(R.id.icon_search)
        iconAdd = findViewById(R.id.icon_add)
        iconPerson = findViewById(R.id.icon_person)
        iconPill = findViewById(R.id.icon_pill)

        cardTotalPatients = findViewById(R.id.card_total_patients)
        cardTotalRx = findViewById(R.id.card_total_rx)
        cardExpiring = findViewById(R.id.card_expiring)
        cardCompleted = findViewById(R.id.card_completed)

        recentPrescriptionCard = findViewById(R.id.card_recent_rx)

        tvRecentMaBenhNhan = findViewById(R.id.tvRecentMaBenhNhan)
        tvRecentTenBenhNhan = findViewById(R.id.tvRecentTenBenhNhan)
        tvRecentNgayKetThuc = findViewById(R.id.tvRecentNgayKetThuc)
    }

    private fun observeRecentPrescription() {
        // Quan sát LiveData chứa danh sách thuốc đã lọc
        donThuocViewModel.filteredPrescriptionItems.observe(this) { itemList ->
            val recentItem = itemList?.firstOrNull()

            if (recentItem != null) {
                latestItemId = recentItem.itemId // Lưu ID thuốc để xem chi tiết
                updateRecentCardUI(recentItem)
                recentPrescriptionCard.visibility = View.VISIBLE
            } else {
                latestItemId = -1L
                tvRecentMaBenhNhan.text = "N/A"
                tvRecentTenBenhNhan.text = "Chưa có thuốc kê"
                tvRecentNgayKetThuc.text = "--/--/----"
                recentPrescriptionCard.visibility = View.VISIBLE
            }
        }
    }

    private fun updateRecentCardUI(item: PrescriptionItem) {
        // Hiển thị mã thuốc và tên thuốc vừa kê
        tvRecentMaBenhNhan.text = "ID: ${item.itemId}"
        tvRecentTenBenhNhan.text = item.medicineName

        // Hiển thị ngày hệ thống hiện tại (vì bảng thuốc không lưu ngày)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvRecentNgayKetThuc.text = dateFormat.format(Date())
    }

    private fun setEvent() {
        iconSearch.setOnClickListener {
            openActivity(DanhSachDonThuoc::class.java, "Danh sách Thuốc")
        }
        iconAdd.setOnClickListener {
            openActivity(ThemDonThuoc::class.java, "Kê đơn thuốc")
        }

        recentPrescriptionCard.setOnClickListener {
            if (latestItemId != -1L) {
                openChiTietDonThuoc(latestItemId)
            } else {
                Toast.makeText(this, "Không có dữ liệu thuốc để xem.", Toast.LENGTH_SHORT).show()
            }
        }

        // Các sự kiện điều hướng khác
        cardTotalPatients.setOnClickListener { openActivity(DanhSachDonThuoc::class.java, "Bệnh nhân") }
        cardTotalRx.setOnClickListener { openActivity(DanhSachDonThuoc::class.java, "Tổng đơn thuốc") }
    }

    private fun applyColorFixes() {
        try {
            val blueColor = Color.parseColor("#316DF0")
            iconPerson.setColorFilter(blueColor)
            iconPill.setColorFilter(blueColor)
        } catch (e: Exception) { /* ignored */ }
    }

    private fun openActivity(cls: Class<out AppCompatActivity>, message: String) {
        try {
            val intent = Intent(this, cls)
            startActivity(intent)
            Toast.makeText(this, "Mở: $message", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi Manifest: ${cls.simpleName}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openChiTietDonThuoc(id: Long) {
        try {
            // Chuyển tới màn hình Chỉnh sửa đơn thuốc
            val intent = Intent(this, SuaDonThuoc::class.java).apply {
                putExtra("ITEM_ID", id)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: Không tìm thấy màn hình SuaDonThuoc", Toast.LENGTH_LONG).show()
        }
    }
}