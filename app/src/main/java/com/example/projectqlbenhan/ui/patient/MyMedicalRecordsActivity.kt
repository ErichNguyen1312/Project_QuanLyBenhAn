package com.example.projectqlbenhan.ui.medicalRecord

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyMedicalRecordsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var rcMyRecords: RecyclerView
    private lateinit var adapter: MedicalRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_medical_records)

        setControl()
        setEvent()
        loadData()
    }

    private fun setControl() {
        btnBack = findViewById(R.id.btnBack)
        rcMyRecords = findViewById(R.id.rcMyRecords)
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish()
        }
    }

//    private fun loadData() {
//        // Lấy ID từ Session
//        val patientId = SessionManager.getSpecificId(this)
//
//        if (patientId == -1L) {
//            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val db = MedicalRecordDatabase.getDatabase(this@MyMedicalRecordsActivity)
//            val list = db.medicalRecordDao().getRecordsByPatient(patientId)
//
//            withContext(Dispatchers.Main) {
//                adapter = MedicalRecordAdapter(list) { record ->
//                    // Sự kiện click vào item
//                    val intent = Intent(this@MyMedicalRecordsActivity, UpdateMedicalRecord::class.java)
//                    intent.putExtra("record_id", record.recordId)
//                    intent.putExtra("is_view_only", true)
//                    // (Optional) Gửi thêm cờ is_view_only nếu muốn chặn sửa
//                    startActivity(intent)
//                }
//
//                rcMyRecords.layoutManager = LinearLayoutManager(this@MyMedicalRecordsActivity)
//                rcMyRecords.adapter = adapter
//            }
//        }
//    }

    private fun loadData() {
        val patientId = SessionManager.getSpecificId(this)

        if (patientId == -1L) {
            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val db = MedicalRecordDatabase.getDatabase(this@MyMedicalRecordsActivity)
            val list = db.medicalRecordDao().getRecordsByPatient(patientId)

            withContext(Dispatchers.Main) {
                // 👇 SỬA ĐOẠN CLICK NÀY
                adapter = MedicalRecordAdapter(list) { record ->

                    // 1. Thay đổi đích đến: Không qua Update nữa, qua thẳng Detail
                    val intent = Intent(this@MyMedicalRecordsActivity, PatientMedicalRecordDetail::class.java)

                    // 2. Truyền ID để bên kia query dữ liệu
                    intent.putExtra("record_id", record.recordId)

                    // Note: Không cần truyền cờ "is_view_only" nữa
                    // vì PatientMedicalRecordDetail mặc định đã là View-Only rồi.

                    startActivity(intent)
                }

                rcMyRecords.layoutManager = LinearLayoutManager(this@MyMedicalRecordsActivity)
                rcMyRecords.adapter = adapter
            }
        }
    }
}