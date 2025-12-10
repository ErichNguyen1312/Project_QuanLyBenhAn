package com.example.projectqlbenhan.ui.patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.PatientDao
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

    lateinit var btnDelete: Button
    private var recordId: String = ""

    lateinit var dao: PatientDao

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

        btnUpdate.setOnClickListener {
            updatePatient()
        }
        btnDelete.setOnClickListener {
            showDeleteConfirm()

        }
        btnBack.setOnClickListener {
            finish()
        }
    }


    private fun setControl() {
        tvHeaderName = findViewById(R.id.tvHeaderName)
        tvName = findViewById(R.id.tvName)
        tvInfo = findViewById(R.id.tvInfo)
        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
    }

    //cac ham xu ly
    //set data
    private fun setData() {


        CoroutineScope(Dispatchers.IO).launch {
            patientId = intent.getLongExtra("patient_id", -1)
            Log.d("patient_id_fromProfile", "patient_id: $patientId")
            val patient = dao.getPatientById(patientId)
            Log.d("patient_fromProfile", "patient: $patient")


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

    //show dialog form comfirm
    private fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xoá")
            .setMessage("Bạn có chắc muốn xoá bệnh nhân này không?")
            .setPositiveButton("Đồng ý") { _, _ ->
                returnDeleteResult()

            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // put record id  to list patient
    private fun returnDeleteResult() {
        val intent = Intent().apply {
            putExtra("delete_recordId", recordId)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    //update patient
    private fun updatePatient() {

        val intent = Intent(this, UpdatePatient::class.java)
        intent.putExtra("patient_id", patientId)
        Log.d("patient_id_fromProfile", "patient_id: $patientId")
//        startActivity(intent)
        updateLauncher.launch(intent)
    }

    //reload list sau khi update
//    override fun onResume() {
//        super.onResume()
//        reloadPatient()
//    }
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


