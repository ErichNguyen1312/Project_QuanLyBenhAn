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
import com.example.projectqlbenhan.dao.thuoc.DonThuocRepository
import com.example.projectqlbenhan.dao.thuoc.PhongKhamDatabase
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity
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

    // Khai báo TextViews trên thẻ đơn thuốc gần đây (từ activity_don_thuoc.xml)
    private lateinit var tvRecentMaBenhNhan: TextView
    private lateinit var tvRecentTenBenhNhan: TextView
    private lateinit var tvRecentNgayKetThuc: TextView

    // Khai báo ViewModel và biến lưu ID mới nhất
    private lateinit var donThuocViewModel: DonThuocViewModel
    private var latestDonThuocId: Long = -1L

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

        // Bắt đầu quan sát dữ liệu
        observeRecentDonThuoc()
    }

    private fun khoiTaoMVVM() {
        val application = requireNotNull(this).application
        val database = PhongKhamDatabase.layDatabase(application)

        val repository = DonThuocRepository(database.donThuocDao())
        val factory = DonThuocViewModelFactory(repository)
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

        // Ánh xạ các TextView (ID đã được fix trong XML)
        tvRecentMaBenhNhan = findViewById(R.id.tvRecentMaBenhNhan)
        tvRecentTenBenhNhan = findViewById(R.id.tvRecentTenBenhNhan)
        tvRecentNgayKetThuc = findViewById(R.id.tvRecentNgayKetThuc)
    }

    private fun observeRecentDonThuoc() {
        // Observer LiveData để lấy đơn thuốc mới nhất
        donThuocViewModel.tatCaDonThuoc.observe(this) { donThuocList ->
            val recentDonThuoc = donThuocList?.firstOrNull()

            if (recentDonThuoc != null) {
                latestDonThuocId = recentDonThuoc.donThuocId // Lưu ID
                updateRecentCardUI(recentDonThuoc)
                recentPrescriptionCard.visibility = View.VISIBLE
            } else {
                latestDonThuocId = -1L
                // Hiển thị placeholder khi không có data
                tvRecentMaBenhNhan.text = "N/A"
                tvRecentTenBenhNhan.text = "Chưa có đơn thuốc"
                tvRecentNgayKetThuc.text = "--/--/----"
                recentPrescriptionCard.visibility = View.VISIBLE
            }
        }
    }

    private fun updateRecentCardUI(donThuoc: ChiTietDonThuocEntity) {

        tvRecentMaBenhNhan.text = donThuoc.donThuocId.toString()
        // Tên BN (trong XML) giờ là Tên bệnh nhân
        tvRecentTenBenhNhan.text = donThuoc.tenBenhNhan

        // Tính ngày kết thúc
        val ngayKetThucMillis = donThuoc.ngayTao + (donThuoc.soLanDung * 24 * 60 * 60 * 1000L)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvRecentNgayKetThuc.text = dateFormat.format(Date(ngayKetThucMillis))
    }

    private fun setEvent() {
        iconSearch.setOnClickListener {
            openActivity(DanhSachDonThuoc::class.java, "Danh sách Đơn Thuốc")
        }
        iconAdd.setOnClickListener {
            openActivity(ThemDonThuoc::class.java, "Thêm Đơn Thuốc")
        }

        // ⭐️ FIX: Xử lý sự kiện click thẻ đơn thuốc gần đây
        recentPrescriptionCard.setOnClickListener {
            if (latestDonThuocId != -1L) {
                openChiTietDonThuoc(latestDonThuocId)
            } else {
                Toast.makeText(this, "Không có đơn thuốc gần đây để xem.", Toast.LENGTH_SHORT).show()
            }
        }

        cardTotalPatients.setOnClickListener {
            openActivity(DanhSachDonThuoc::class.java, "Quản lý Bệnh nhân")
        }
        cardTotalRx.setOnClickListener { openActivity(DanhSachDonThuoc::class.java, "Danh sách Đơn Thuốc (Tổng quan)") }
        cardExpiring.setOnClickListener { openActivity(DanhSachDonThuoc::class.java, "Danh sách Đơn Thuốc (Sắp hết hạn)") }
        cardCompleted.setOnClickListener { openActivity(DanhSachDonThuoc::class.java, "Danh sách Đơn Thuốc (Đã hoàn thành)") }
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
            Toast.makeText(this, "Đang mở: $message", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Activity ${cls.simpleName} (Kiểm tra Manifest!)", Toast.LENGTH_LONG).show()
        }
    }

    private fun openChiTietDonThuoc(id: Long) {
        try {
            val intent = Intent(this, ChiTietDonThuoc::class.java).apply {
                putExtra("DON_THUOC_ID", id)
            }
            startActivity(intent)
            Toast.makeText(this, "Đang mở: Chi tiết Đơn Thuốc (ID: $id)", Toast.LENGTH_SHORT).show()
        } catch (
            e: Exception) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Activity ChiTietDonThuoc", Toast.LENGTH_LONG).show()
        }
    }
}