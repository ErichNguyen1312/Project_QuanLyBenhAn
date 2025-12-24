package com.example.projectqlbenhan.ui.medicalRecord

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R

import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.ui.patient.RecentMedicalRecordAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePatientMedicalRecord : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvHeader: TextView
    private lateinit var rcRecyclerMedicalRecord: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var btnAddMedicalRecord: Button

    private var patientId: Long = -1
    private var patientName: String = ""

    private val medicalRecordList = mutableListOf<MedicalRecord>()
    private lateinit var adapter: RecentMedicalRecordAdapter

    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).medicalRecordDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_patient_medical_record)


        setControl()
        getIntentData()
        setEvent()
        loadMedicalRecords()
    }

    private fun getIntentData() {
        patientId = intent.getLongExtra("patient_id", -1)
        patientName = intent.getStringExtra("patient_name") ?: "Bệnh nhân"

        tvHeader.text = "Bệnh án của $patientName"
    }

    private fun setControl() {
        btnBack = findViewById(R.id.btnBack)
        rcRecyclerMedicalRecord = findViewById(R.id.rcRecyclerMedicalRecord)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnAddMedicalRecord = findViewById(R.id.btnAddMedicalRecord)
        tvHeader = findViewById(R.id.tvHeader)

        // Setup RecyclerView
        adapter = RecentMedicalRecordAdapter(medicalRecordList) { record ->
            // ⭐️ CLICK ITEM: Mở màn hình UpdateMedicalRecord để XEM/SỬA
            // (Thay vì mở PatientMedicalRecordDetail như cũ)
            val intent = Intent(this, UpdateMedicalRecord::class.java)
            intent.putExtra("record_id", record.recordId) // Truyền ID để load dữ liệu cũ
            intent.putExtra("patient_id", patientId)
            launcher.launch(intent)
        }

        rcRecyclerMedicalRecord.layoutManager = LinearLayoutManager(this)
        rcRecyclerMedicalRecord.adapter = adapter
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

        btnAddMedicalRecord.setOnClickListener {
            // ⭐️ CLICK THÊM: Mở màn hình UpdateMedicalRecord để TẠO MỚI
            // (Truyền record_id = -1)
            val intent = Intent(this, UpdateMedicalRecord::class.java)
            intent.putExtra("patient_id", patientId)
            intent.putExtra("appointment_id", -1L) // Không từ lịch hẹn
            intent.putExtra("record_id", -1L)      // Tạo mới
            launcher.launch(intent)
        }
    }

    private fun loadMedicalRecords() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = dao.getRecordsByPatient(patientId)

            withContext(Dispatchers.Main) {
                medicalRecordList.clear()
                medicalRecordList.addAll(list)
                adapter.notifyDataSetChanged()

                // Ẩn hiện view Empty
                if (medicalRecordList.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rcRecyclerMedicalRecord.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rcRecyclerMedicalRecord.visibility = View.VISIBLE
                }
            }
        }
    }

    // Dùng 1 launcher chung cho cả Thêm và Sửa để reload list khi quay lại
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadMedicalRecords()
        }
    }
}