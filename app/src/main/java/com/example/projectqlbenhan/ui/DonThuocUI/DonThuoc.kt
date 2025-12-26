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
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.ui.home.HomeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DonThuoc : AppCompatActivity() {

    private lateinit var iconSearch: ImageView
    private lateinit var iconAdd: ImageView
    private lateinit var recentPrescriptionCard: CardView
    private lateinit var iconPerson: ImageView
    private lateinit var iconPill: ImageView

    private lateinit var tvRecentMaBenhNhan: TextView
    private lateinit var tvRecentTenBenhNhan: TextView
    private lateinit var tvRecentNgayKetThuc: TextView

    // Thêm các TextView thống kê
    private lateinit var tvTotalPatients: TextView
    private lateinit var tvTotalRx: TextView

    private lateinit var donThuocViewModel: DonThuocViewModel
    private var latestItemId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_don_thuoc)

        // Xử lý System Bar (Padding cho màn hình tràn viền)
        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        khoiTaoMVVM()
        setControl()
        setEvent()
        applyColorFixes()
        observeViewModel()

        // Tải dữ liệu ban đầu
        donThuocViewModel.loadAll()
    }

    override fun onStop() {
        intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        super.onStop()
    }

    private fun khoiTaoMVVM() {
        val database = MedicalRecordDatabase.getDatabase(this)
        val factory = DonThuocViewModelFactory(database.prescriptionItemDao())
        donThuocViewModel = ViewModelProvider(this, factory)[DonThuocViewModel::class.java]
    }

    private fun setControl() {
        iconSearch = findViewById(R.id.icon_search)
        iconAdd = findViewById(R.id.icon_add)
        iconPerson = findViewById(R.id.icon_person)
        iconPill = findViewById(R.id.icon_pill)
        recentPrescriptionCard = findViewById(R.id.card_recent_rx)

        tvRecentMaBenhNhan = findViewById(R.id.tvRecentMaBenhNhan)
        tvRecentTenBenhNhan = findViewById(R.id.tvRecentTenBenhNhan)
        tvRecentNgayKetThuc = findViewById(R.id.tvRecentNgayKetThuc)


    }

    private fun observeViewModel() {
        // Quan sát danh sách đơn thuốc để cập nhật "Đơn thuốc gần đây" và số lượng
        donThuocViewModel.filteredPrescriptionItems.observe(this) { donThuocList ->
            if (!donThuocList.isNullOrEmpty()) {
                val recentDonThuoc = donThuocList.first() // Lấy đơn thuốc mới nhất
                latestItemId = recentDonThuoc.itemId
                updateRecentCardUI(recentDonThuoc)
                recentPrescriptionCard.visibility = View.VISIBLE

                // Cập nhật con số tổng quát (nếu có TextView)
                // tvTotalRx.text = donThuocList.size.toString()
            } else {
                latestItemId = -1L
                recentPrescriptionCard.visibility = View.GONE
            }
        }
    }

    private fun updateRecentCardUI(item: PrescriptionItem) {
        // Hiển thị thông tin đơn thuốc mới nhất lên Card "Đơn thuốc gần đây"
        tvRecentMaBenhNhan.text = "Bệnh án: #${item.recordId}"
        tvRecentTenBenhNhan.text = item.medicineName

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvRecentNgayKetThuc.text = dateFormat.format(Date(item.createdAt))
    }

    private fun setEvent() {
        // ⭐ QUAN TRỌNG: Click kính lúp chuyển sang màn hình tìm kiếm danh sách
        iconSearch.setOnClickListener {
            val intent = Intent(this, DanhSachDonThuoc::class.java)
            startActivity(intent)
        }

        iconAdd.setOnClickListener {
            // Chuyển sang màn hình thêm đơn thuốc (Nếu bạn đã code activity này)
            // startActivity(Intent(this, ThemDonThuoc::class.java))
            Toast.makeText(this, "Tính năng đang cập nhật", Toast.LENGTH_SHORT).show()
        }

        // Click vào đơn thuốc gần đây để xem chi tiết
        recentPrescriptionCard.setOnClickListener {
            if (latestItemId != -1L) {
                val intent = Intent(this, ChiTietDonThuoc::class.java).apply {
                    putExtra("ITEM_ID", latestItemId)
                }
                startActivity(intent)
            }
        }
    }

    private fun applyColorFixes() {
        try {
            val blueColor = Color.parseColor("#316DF0")
            iconPerson.setColorFilter(blueColor)
            iconPill.setColorFilter(blueColor)
        } catch (e: Exception) {}
    }
}