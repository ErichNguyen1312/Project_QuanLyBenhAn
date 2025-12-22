package com.example.projectqlbenhan.ui.TaiKham

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class screenTaiKham_Main : AppCompatActivity() {

    private lateinit var listTaiKham: ListView
    private lateinit var btnDatLichTK: Button
    private lateinit var ct_btnBack: ImageButton

    private lateinit var adapter: Adapter_TaiKham
    private var appointmentList = mutableListOf<Appointment>()
    private var doctorMap = mapOf<Long, String>() // Map ID -> Tên Bác sĩ

    private var patientId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_tai_kham_main)

        // Lấy PatientID
        patientId = intent.getLongExtra("patient_id", -1)

        initView()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun initView() {
        listTaiKham = findViewById(R.id.listTaiKham)
        btnDatLichTK = findViewById(R.id.btnDatLichTK)
        ct_btnBack = findViewById(R.id.btnBack) // Check lại ID trong custom_toolbar
    }

    private fun setEvent() {
        ct_btnBack.setOnClickListener { finish() }

        btnDatLichTK.setOnClickListener {
            val intent = Intent(this, screenTaiKham_Create::class.java)
            intent.putExtra("patient_id", patientId)
            startActivity(intent)
        }
    }

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshData()
        }
    }

    private fun refreshData() {
        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Main)

            // 1. Lấy danh sách lịch hẹn của Patient (Cần viết thêm query trong DAO nếu chưa có)
            // Ví dụ: db.appointmentDao().getAppointmentsByPatient(patientId)
            // Ở đây mình giả sử hàm getUpcomingAppointments lấy hết, bạn cần lọc theo patientId
            val appointments = withContext(Dispatchers.IO) {
                db.appointmentDao().getAppointmentsByPatient(patientId)
            }

            // 2. Lấy danh sách bác sĩ để Map tên
            val doctors = withContext(Dispatchers.IO) {
                db.doctorDao().getAll()
            }
            // Tạo Map: ID -> Name
            doctorMap = doctors.associate { it.doctorId to it.fullName }

            appointmentList.clear()
            appointmentList.addAll(appointments)

            // 3. Setup Adapter
            if (!::adapter.isInitialized) {
                adapter = Adapter_TaiKham(
                    this@screenTaiKham_Main,
                    appointmentList,
                    doctorMap // Truyền map vào adapter
                ) { appointment ->
                    // OnClick Edit
                    val intent = Intent(this@screenTaiKham_Main, screenTaiKham_Edit::class.java)
                    intent.putExtra("appointmentId", appointment.appointmentId)
                    intent.putExtra("appointmentDate", appointment.appointmentDate)
                    intent.putExtra("doctorId", appointment.doctorId)
                    // intent.putExtra("ghiChu", appointment.reason) // Nếu có trường reason
                    editLauncher.launch(intent)
                }
                listTaiKham.adapter = adapter
            } else {
                adapter.notifyDataSetChanged()
            }
        }
    }
}