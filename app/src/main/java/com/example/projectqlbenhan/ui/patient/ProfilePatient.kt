package com.example.projectqlbenhan.ui.patient

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.ui.medicalRecord.ProfilePatientMedicalRecord
import com.example.projectqlbenhan.ui.medicalRecord.UpdateMedicalRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePatient : AppCompatActivity() {
    lateinit var tvHeaderName: TextView
    lateinit var tvName: TextView
    lateinit var tvInfo: TextView
    lateinit var btnBack: ImageView
    lateinit var btnUpdate: Button
    lateinit var btnTaiKham: Button

    lateinit var btnPatientMedicalRecordDetail: Button
    lateinit var btnDelete: Button
    private var recordId: String = ""

    lateinit var dao: PatientDao

    private lateinit var rcRecentRecords: RecyclerView
    private lateinit var recentAdapter: RecentMedicalRecordAdapter
    private val recentRecordList = ArrayList<MedicalRecord>()
    private lateinit var layoutRecentEmpty: View

    private var patientId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_patient)
        dao = MedicalRecordDatabase.getDatabase(this).patientDao()

        setControl()
        setData()
        setEvent()
    }

    private fun setEvent() {
        btnUpdate.setOnClickListener { updatePatient() }
        btnDelete.setOnClickListener { showDeleteConfirm() }

        // Nút "Xem tất cả bệnh án"
        btnPatientMedicalRecordDetail.setOnClickListener {
            val intent = Intent(this, ProfilePatientMedicalRecord::class.java)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("patient_name", tvHeaderName.text.toString()) // Truyền tên qua cho đẹp
            generalLauncher.launch(intent)
        }

        btnTaiKham.setOnClickListener {
//            val intent = Intent(this, screenTaiKham_Main::class.java)
//            intent.putExtra("patient_id", patientId)
//            startActivity(intent)
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun setControl() {
        tvHeaderName = findViewById(R.id.tvHeaderName)
        tvName = findViewById(R.id.tvName)
        tvInfo = findViewById(R.id.tvInfo)
        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnTaiKham = findViewById(R.id.btnTaiKham)
        btnPatientMedicalRecordDetail = findViewById(R.id.btnPatientMedicalRecordDetail)

        rcRecentRecords = findViewById(R.id.rcRecentRecords)
        layoutRecentEmpty = findViewById(R.id.layoutRecentEmpty)

        // Setup RecyclerView với sự kiện Click
        recentAdapter = RecentMedicalRecordAdapter(recentRecordList) { record ->
            // ⭐️ CLICK VÀO LỊCH SỬ -> Mở màn hình Khám (Mode Sửa)
            val intent = Intent(this, UpdateMedicalRecord::class.java)
            intent.putExtra("record_id", record.recordId)
            intent.putExtra("patient_id", patientId)
            generalLauncher.launch(intent)
        }

        rcRecentRecords.layoutManager = LinearLayoutManager(this)
        rcRecentRecords.adapter = recentAdapter
    }

    // --- DATA HANDLING ---
    private fun setData() {
        CoroutineScope(Dispatchers.IO).launch {
            patientId = intent.getLongExtra("patient_id", -1)
            val patient = dao.getPatientById(patientId)

            withContext(Dispatchers.Main) {
                if (patient != null) {
                    val name = patient.fullName ?: "Không rõ"
                    val age = calculateAge(patient.dateOfBirth) ?: 0
                    val gender = patient.gender ?: "Không rõ"

                    recordId = patient.medicalRecordNumber?.toString() ?: ""

                    tvHeaderName.text = name
                    tvName.text = "Tên: $name"
                    tvInfo.text = "Tuổi: $age | Giới tính: $gender | Mã HS: $recordId"
                }
                loadRecentRecords()
            }
        }
    }

    private fun loadRecentRecords() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = dao.getRecentMedicalRecords(patientId)

            withContext(Dispatchers.Main) {
                recentRecordList.clear()
                recentRecordList.addAll(list)
                recentAdapter.notifyDataSetChanged()

                layoutRecentEmpty.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xoá")
            .setMessage("Bạn có chắc muốn xoá bệnh nhân này không?")
            .setPositiveButton("Đồng ý") { _, _ ->
                deletePatient()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // --- CRUD ---
    private fun deletePatient() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.deletePatientById(patientId)
            withContext(Dispatchers.Main) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun updatePatient() {
        val intent = Intent(this, UpdatePatient::class.java)
        intent.putExtra("patient_id", patientId)
        updateLauncher.launch(intent)
    }

    // Launcher dùng riêng cho update thông tin cá nhân
    private val updateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setData() // Reload thông tin cá nhân
            }
        }

    // Launcher dùng chung (Reload list bệnh án khi quay lại)
    private val generalLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadRecentRecords()
            }
        }

    private fun calculateAge(dob: Long): Int {
        if (dob == 0L) return 0
        val dobCal = java.util.Calendar.getInstance()
        dobCal.timeInMillis = dob
        val today = java.util.Calendar.getInstance()
        var age = today.get(java.util.Calendar.YEAR) - dobCal.get(java.util.Calendar.YEAR)
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < dobCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--
        }
        return if (age < 0) 0 else age
    }
}