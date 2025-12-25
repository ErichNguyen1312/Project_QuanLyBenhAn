package com.example.projectqlbenhan.ui.BacSi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.doctor.Doctor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class screenBacSi_Detail : AppCompatActivity() {

    private lateinit var btnBack: View
    private lateinit var btnEdit: ImageView
    private lateinit var tvAvatar: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvSpecialization: TextView
    private lateinit var tvId: TextView
    private lateinit var tvDescription: TextView // Thay cho phone/email

    private var doctorId: Long = -1
    private var currentDoctor: Doctor? = null

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadDoctorData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_bac_si_detail)

        doctorId = intent.getLongExtra("doctor_id", -1)
        if (doctorId == -1L) {
            finish()
            return
        }

        initView()
        setEvent()
        loadDoctorData()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        btnEdit = findViewById(R.id.btnEdit)
        tvAvatar = findViewById(R.id.tvAvatar)
        tvFullName = findViewById(R.id.tvFullName)
        tvSpecialization = findViewById(R.id.tvSpecialization)
        tvDescription = findViewById(R.id.tvDescription)
        tvId = findViewById(R.id.tvId)
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            intent = Intent(this, screenBacSi_Edit::class.java)
            intent.putExtra("doctor_id", doctorId)
            editLauncher.launch(intent)
        }
    }

    private fun loadDoctorData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = MedicalRecordDatabase.getDatabase(this@screenBacSi_Detail)
            currentDoctor = db.doctorDao().getDoctorById(doctorId)

            withContext(Dispatchers.Main) {
                currentDoctor?.let { doctor ->
                    // 1. Tên
                    tvFullName.text = "${doctor.fullName}"

                    tvId.text = "Mã bác sĩ: ${doctor.doctorId}"

                    // 2. Chuyên khoa
                    tvSpecialization.text = doctor.specialization ?: "Chưa cập nhật chuyên khoa"

                    // 3. Mô tả
                    tvDescription.text = if (doctor.description.isNullOrEmpty())
                        "Chưa có thông tin mô tả chi tiết."
                    else
                        doctor.description
                }
            }
        }
    }
}