package com.example.projectqlbenhan.ui.patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.projectqlbenhan.ui.TaiKham.screenTaiKham_Main
import com.example.projectqlbenhan.ui.medicalRecord.ProfilePatientMedicalRecord
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

        btnPatientMedicalRecordDetail.setOnClickListener {
            val intent = Intent(this, ProfilePatientMedicalRecord::class.java)
            intent.putExtra("patient_id", patientId)
            startActivity(intent)
        }

        btnTaiKham.setOnClickListener {
            val intent = Intent(this, screenTaiKham_Main::class.java)
            intent.putExtra("patient_id", patientId)
            startActivity(intent)
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

        // Setup RecyclerView
        recentAdapter = RecentMedicalRecordAdapter(recentRecordList)
        rcRecentRecords.layoutManager = LinearLayoutManager(this)
        rcRecentRecords.adapter = recentAdapter

    }

    //cac ham xu ly
    //set data
    private fun setData() {
        CoroutineScope(Dispatchers.IO).launch {

            patientId = intent.getLongExtra("patient_id", -1)
            val patient = dao.getPatientById(patientId)

            withContext(Dispatchers.Main) {

                if (patient != null) {
                    val name = patient.fullName ?: "Không rõ"
                    val age = patient.calculateAge(patient.dateOfBirth) ?: 0
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

    //show dialog form comfirm
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

    override fun onResume() {
        super.onResume()
        loadRecentRecords()
    }

    // put record id  to list patient
    private fun returnDeleteResult() {
        val intent = Intent().apply {
            putExtra("delete_recordId", recordId)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun deletePatient() {
        CoroutineScope(Dispatchers.IO).launch {

            dao.deletePatientById(patientId)

            withContext(Dispatchers.Main) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    //update patient
    private fun updatePatient() {

        val intent = Intent(this, UpdatePatient::class.java)
        intent.putExtra("patient_id", patientId)
        Log.d("patient_id_fromProfile", "patient_id: $patientId")
        updateLauncher.launch(intent)
    }


    private val updateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadPatient()
            }
        }

    private fun reloadPatient() {
        CoroutineScope(Dispatchers.IO).launch {
            patientId = intent.getLongExtra("update_patient_id", -1)
            if (patientId == -1L) {
                Log.d("patient_id_fromProfile", "patient_id not found: $patientId")
                finish()
            }
            val patient = dao.getPatientById(patientId)

            withContext(Dispatchers.Main) {

                if (patient != null) {

                    val name = patient.fullName ?: "Không rõ"
                    val age = patient.calculateAge(patient.dateOfBirth) ?: 0
                    val gender = patient.gender ?: "Không rõ"
                    recordId = (patient.medicalRecordNumber ?: 0) as String
                    tvHeaderName.text = name
                    tvName.text = "Tên: $name"
                    tvInfo.text = "Tuổi: $age | Giới tính: $gender | Mã HS: $recordId"
                }
            }
        }
    }

}


