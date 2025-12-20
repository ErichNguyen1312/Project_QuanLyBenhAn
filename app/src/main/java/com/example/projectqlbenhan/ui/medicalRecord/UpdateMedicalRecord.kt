package com.example.projectqlbenhan.ui.medicalRecord

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.paging.LOG_TAG
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.utils.SessionManager
import com.github.mikephil.charting.charts.Chart.LOG_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdateMedicalRecord : AppCompatActivity() {

    private lateinit var edtDiagnosis: EditText
    private lateinit var edtSymptoms: EditText
    private lateinit var edtType: EditText
    private lateinit var edtDate: EditText
    private lateinit var edtDoctor: EditText
    private lateinit var edtNotes: EditText
    private lateinit var btnUpdate: TextView
    private lateinit var btnDelete: TextView
    private lateinit var btnCancel: TextView
    private var recordId: Long = -1
    private var patientId: Long = -1
    private var selectedDateMillis: Long = 0L

    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).medicalRecordDao()
    }
    private val appointmentDao by lazy { MedicalRecordDatabase.getDatabase(this).appointmentDao() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_medical_record)
        getIntentData()
        setControl()
        setEvent()
        loadRecord()

    }

    private fun setEvent() {
        btnCancel.setOnClickListener {
            finish()
        }


        edtDate.setOnClickListener {
            showDatePicker()
        }


        btnUpdate.setOnClickListener {
            saveUpdatedRecord()
        }


        btnDelete.setOnClickListener {
            showDeleteConfirm()
        }
    }

    private fun getIntentData() {
        recordId = intent.getLongExtra("record_id", -1)
        patientId = intent.getLongExtra("patient_id", -1)
        Log.d("patient_id_fromUpdate", "patient_id: $patientId")
    }

    private fun setControl() {
        edtDiagnosis = findViewById(R.id.edtDiagnosis)
        edtSymptoms = findViewById(R.id.edtSymptoms)
        edtType = findViewById(R.id.edtDiseaseType)
        edtDate = findViewById(R.id.edtDate)
        edtDoctor = findViewById(R.id.edtDoctor)
        edtNotes = findViewById(R.id.edtNotes)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnCancel = findViewById(R.id.tvCancel)
    }

    //cac ham xu ly
    private fun loadRecord() {

        CoroutineScope(Dispatchers.IO).launch {
            val record = dao.getRecordById(recordId)
//            val record = dao.getRecordByPatientId(patientId)
            Log.d("record", record.toString())
            val doctorUpdateRecord = SessionManager.getDoctorName(this@UpdateMedicalRecord)
            withContext(Dispatchers.Main) {
                if (record != null) {

                    edtDiagnosis.setText(record.diagnosis)
                    edtSymptoms.setText(record.symptoms)
                    edtType.setText(record.diseaseType)
                    edtDoctor.setText(doctorUpdateRecord ?: "")
                    edtNotes.setText(record.notes)

                    selectedDateMillis = record.examinationDate
                    edtDate.setText(formatDate(record.examinationDate))
                }
            }
        }
    }

    private fun saveUpdatedRecord() {

        val diagnosis = edtDiagnosis.text.toString().trim()
        val symptoms = edtSymptoms.text.toString().trim()
        val type = edtType.text.toString().trim()
        val notes = edtNotes.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {

            dao.updateRecord(
                recordId,
                diagnosis,
                symptoms,
                type,
                selectedDateMillis,
                notes
            )
            //lay id tu lich hen dashboard
            val appointmentId = intent.getLongExtra("appointment_id", -1)
            if (appointmentId != -1L) {
                appointmentDao.updateStatus(appointmentId, "COMPLETED")
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateMedicalRecord, "Cập nhật thành công!", Toast.LENGTH_SHORT)
                    .show()
                setResult(RESULT_OK)
                finish()
            }
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
                Toast.makeText(this@UpdateMedicalRecord, "Đã xoá bệnh án!", Toast.LENGTH_SHORT)
                    .show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
        }


        val dp = DatePickerDialog(
            this,
            { _, y, m, d ->
                val cal = Calendar.getInstance()
                cal.set(y, m, d, 0, 0, 0)
                selectedDateMillis = cal.timeInMillis
                edtDate.setText(formatDate(selectedDateMillis))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dp.datePicker.maxDate = System.currentTimeMillis()
        dp.show()
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}