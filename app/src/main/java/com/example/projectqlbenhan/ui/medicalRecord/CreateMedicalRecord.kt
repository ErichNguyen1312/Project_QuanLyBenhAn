package com.example.projectqlbenhan.ui.medicalRecord

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CreateMedicalRecord : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etDiagnosis: EditText
    private lateinit var etSymptoms: EditText
    private lateinit var etDiseaseType: EditText
    private lateinit var tvDate: TextView
    private lateinit var etDoctor: EditText
    private lateinit var etNote: EditText
    private lateinit var btnSave: Button

    private var selectedDateMillis: Long = 0L
    private var patientId: Long = -1

    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }
    private val dao by lazy { db.medicalRecordDao() }

    private val doc by lazy { db.doctorDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_medical_record)

        patientId = intent.getLongExtra("patient_id", -1)
        Log.d("patient_id_fromCreate", "patient_id: $patientId")
        if (patientId == -1L) {
            Toast.makeText(this, "Lỗi: không tìm thấy bệnh nhân!", Toast.LENGTH_SHORT).show()
            finish()
        }
        setControl()
        setEvent()
    }

    private fun setEvent() {
        loadDoctorInfo()
        btnBack.setOnClickListener { finish() }

        tvDate.setOnClickListener {
            openDatePicker()
        }

        btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun setControl() {
        btnBack = findViewById(R.id.btnBack)
        etDiagnosis = findViewById(R.id.etDiagnosis)
        etSymptoms = findViewById(R.id.etSymptoms)
        etDiseaseType = findViewById(R.id.etDiseaseType)
        tvDate = findViewById(R.id.tvDate)
        etDoctor = findViewById(R.id.etDoctor)
        etNote = findViewById(R.id.etNote)
        btnSave = findViewById(R.id.btnSave)

    }

    //cac ham xu ly
    private fun openDatePicker() {
        val cal = Calendar.getInstance()
        val dp = DatePickerDialog(
            this,
            { _, year, month, day ->
                val c = Calendar.getInstance()
                c.set(year, month, day)
                selectedDateMillis = c.timeInMillis
                tvDate.text = "$day/${month + 1}/$year"
                tvDate.setTextColor(0xFF000000.toInt())
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dp.show()
    }

    private fun saveRecord() {
        val diagnosis = etDiagnosis.text.toString().trim()
        val symptoms = etSymptoms.text.toString().trim()
        val type = etDiseaseType.text.toString().trim()
        val doctor = etDoctor.text.toString().trim()
        val note = etNote.text.toString().trim()
        val doctorId = SessionManager.getDoctorId(this)

        if (doctorId == -1L) {
            toast("Vui lòng đăng nhập")
            finish()
            return
        }

        // Validate
        if (diagnosis.isEmpty()) return toast("Vui lòng nhập chẩn đoán")
        if (symptoms.isEmpty()) return toast("Vui lòng nhập triệu chứng")
        if (type.isEmpty()) return toast("Vui lòng nhập loại bệnh")
        if (selectedDateMillis == 0L) return toast("Vui lòng chọn ngày khám")

        val record = MedicalRecord(
            patientId = patientId,
            diagnosis = diagnosis,
            symptoms = symptoms,
            diseaseType = type,
            examinationDate = selectedDateMillis,
//            doctorName = doctor,
            doctorId = doctorId,
            notes = note
        )

        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(record)

            withContext(Dispatchers.Main) {
                toast("Thêm bệnh án thành công!")
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun loadDoctorInfo() {
        val doctorId = SessionManager.getDoctorId(this)

        CoroutineScope(Dispatchers.IO).launch {
            val doctor = doc.getDoctorById(doctorId)

            withContext(Dispatchers.Main) {
                etDoctor.setText(doctor.fullName)
                etDoctor.isEnabled = false
                etDoctor.isFocusable = false
            }
        }
    }


    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}