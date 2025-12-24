package com.example.projectqlbenhan.ui.patient_home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.authService.Login
import com.example.projectqlbenhan.ui.medicalRecord.MyMedicalRecordsActivity
import com.example.projectqlbenhan.ui.patient.UpdatePatient
import com.example.projectqlbenhan.utils.SessionManager

class PatientHomeActivity : AppCompatActivity() {

    // Khai báo View
    private lateinit var tvWelcome: TextView
    private lateinit var cardMyRecords: CardView
    private lateinit var cardBooking: CardView
    private lateinit var cardProfile: CardView
    private lateinit var cardLogout: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home)

        setControl() // 1. Ánh xạ View
        showWelcome() // 2. Hiển thị dữ liệu
        setEvent()   // 3. Gán sự kiện
    }

    private fun setControl() {
        tvWelcome = findViewById(R.id.tvWelcome)
        cardMyRecords = findViewById(R.id.cardMyRecords)
        cardBooking = findViewById(R.id.cardBooking)
        cardProfile = findViewById(R.id.cardProfile)
        cardLogout = findViewById(R.id.cardLogout)
    }

    private fun showWelcome() {
        val name = SessionManager.getFullName(this)
        tvWelcome.text = "Xin chào,\n$name"
    }

    private fun setEvent() {
        // 1. Click "Hồ sơ của tôi"
        cardMyRecords.setOnClickListener {
            startActivity(Intent(this, MyMedicalRecordsActivity::class.java))
        }

        // 2. Click "Thông tin cá nhân"
        cardProfile.setOnClickListener {
            val patientId = SessionManager.getSpecificId(this)
            if (patientId != -1L) {
                val intent = Intent(this, UpdatePatient::class.java)
                intent.putExtra("patient_id", patientId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Lỗi: Không tìm thấy ID bệnh nhân", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Click "Đặt lịch"
        cardBooking.setOnClickListener {
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        // 4. Click "Đăng xuất"
        cardLogout.setOnClickListener {
            SessionManager.logout(this)
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}