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
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.DonThuocUI.DanhSachDonThuoc
import com.example.projectqlbenhan.ui.DonThuocUI.DonThuoc
import com.example.projectqlbenhan.ui.DonThuocUI.ThemDonThuoc
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
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
    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).medicalRecordDao()
    }

    private val doc by lazy {
        MedicalRecordDatabase.getDatabase(this).doctorDao()
    }


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

        // ⭐ FIX: Thay đổi Activity đích từ DanhSachDonThuoc sang ThemDonThuoc
        btnPatientPrescription.setOnClickListener {
            val intent = Intent(this, ThemDonThuoc::class.java) // Đã sửa
            intent.putExtra("record_id", recordId) // Vẫn truyền ID Hồ sơ Bệnh án
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
        CoroutineScope(Dispatchers.IO).launch {
            val record = dao.getRecordById(recordId)

            withContext(Dispatchers.Main) {

                if (record == null) {
                    Log.e("DETAIL", "Record not found!")
                    return@withContext
                }

                tvDiagnosis.text = record.diagnosis

                loadSymptoms(record.symptoms)

                tvType.text = record.diseaseType
                tvDate.text = formatDate(record.examinationDate)
                tvDoctor.text = doc.getDoctorNameById(record.doctorId)
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

        CoroutineScope(Dispatchers.IO).launch {

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