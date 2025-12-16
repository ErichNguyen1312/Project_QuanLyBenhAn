package com.example.projectqlbenhan.ui.medicalRecord


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.DonThuocUI.DonThuoc // Dashboard Đơn Thuốc
import com.example.projectqlbenhan.utils.SessionManager // ⭐ Cần SessionManager để lưu trạng thái
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientMedicalRecordDetail : AppCompatActivity() {

    private lateinit var tvDiagnosis: TextView
    private lateinit var layoutSymptoms: LinearLayout
    private lateinit var tvType: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDoctor: TextView
    private lateinit var tvNotes: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnUpdate: TextView
    private lateinit var btnDelete: ImageButton
    private lateinit var btnPatientPrescription : Button

    private var recordId: Long = -1
    private var patientId: Long = -1
    private var patientName: String? = null

    private val db by lazy {
        MedicalRecordDatabase.getDatabase(this)
    }
    private val dao by lazy { db.medicalRecordDao() }
    private val doc by lazy { db.doctorDao() }
    private val patDao by lazy { db.patientDao() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_medical_record_detail)

        getIntentData()
        setControl()
        setEvent()
        loadRecordDetail()
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish()
        }
        btnUpdate.setOnClickListener {
            val intent = Intent(this, UpdateMedicalRecord::class.java)
            intent.putExtra("record_id", recordId)
            updateLauncher.launch(intent)
        }
        btnDelete.setOnClickListener {
            showDeleteConfirm()
        }

        // ⭐ FIX CHỨC NĂNG: Chuyển sang Dashboard Đơn Thuốc (DonThuoc)
        btnPatientPrescription.setOnClickListener {
            val intent = Intent(this, DonThuoc::class.java)
            startActivity(intent)
        }
    }

    private fun setControl() {
        tvDiagnosis = findViewById(R.id.tvDiagnosis)
        layoutSymptoms = findViewById(R.id.layoutSymptoms)
        tvType = findViewById(R.id.tvType)
        tvDate = findViewById(R.id.tvDate)
        tvDoctor = findViewById(R.id.tvDoctor)
        tvNotes = findViewById(R.id.tvNotes)

        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnPatientPrescription= findViewById(R.id.btnPatientPrescription)
    }

    //cac ham xu ly
    private fun getIntentData() {
        recordId = intent.getLongExtra("record_id", -1)
        Log.d("DETAIL", "Received recordId = $recordId")
    }

    private fun loadRecordDetail() {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = dao.getRecordById(recordId)

            if (record == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PatientMedicalRecordDetail, "Không tìm thấy bệnh án!", Toast.LENGTH_LONG).show()
                    finish()
                }
                return@launch
            }

            patientId = record.patientId
            val doctorName = doc.getDoctorNameById(record.doctorId)
            val patient = patDao.getPatientById(patientId)
            patientName = patient?.fullName

            // ⭐ LƯU THÔNG TIN BỆNH NHÂN VÀO SESSION KHI XEM HỒ SƠ
            if (patientId != -1L && !patientName.isNullOrEmpty()) {
                SessionManager.saveCurrentPatientInfo(this@PatientMedicalRecordDetail, patientId, patientName!!)
            } else {
                SessionManager.clearCurrentPatientInfo(this@PatientMedicalRecordDetail)
            }

            withContext(Dispatchers.Main) {

                tvDiagnosis.text = record.diagnosis
                loadSymptoms(record.symptoms)
                tvType.text = record.diseaseType
                tvDate.text = formatDate(record.examinationDate)
                tvDoctor.text = doctorName
                tvNotes.text = record.notes
            }
        }
    }

    private fun loadSymptoms(symptomString: String) {
        layoutSymptoms.removeAllViews()
        if (symptomString.isBlank()) return

        val inflater = LayoutInflater.from(this)

        val symptoms = symptomString.split(",", ";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        for (s in symptoms) {
            val view =
                inflater.inflate(R.layout.item_symptom, layoutSymptoms, false)

            val text = view.findViewById<TextView>(R.id.tvSymptom)
            text.text = s

            layoutSymptoms.addView(view)
        }
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private val updateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadRecordDetail()
                setResult(RESULT_OK)
            }
        }

    private fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Xóa bệnh án")
            .setMessage("Bạn có chắc chắn muốn xóa bệnh án này?")
            .setPositiveButton("Xóa") { _: DialogInterface, _: Int ->
                deleteRecord()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteRecord() {

        lifecycleScope.launch(Dispatchers.IO) {
            dao.deleteRecord(recordId)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@PatientMedicalRecordDetail,
                    "Đã xóa bệnh án!",
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}