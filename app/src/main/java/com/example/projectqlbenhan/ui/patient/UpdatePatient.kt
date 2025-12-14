package com.example.projectqlbenhan.ui.patient

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.patient.Patient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class UpdatePatient : AppCompatActivity() {
    lateinit var btnBack: ImageView
    lateinit var etName: EditText
    lateinit var etAge: EditText
    lateinit var etRecordId: EditText
    lateinit var etAddress: EditText
    lateinit var etPhone: EditText
    lateinit var rgGender: RadioGroup
    lateinit var rbMale: RadioButton
    lateinit var rbFemale: RadioButton
    lateinit var btnUpdate: Button

    private var patientId: Long = 0L

    private val db: MedicalRecordDatabase by lazy {
        MedicalRecordDatabase.getDatabase(this)
    }
    private val dao by lazy { db.patientDao() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_patient)

        patientId = intent.getLongExtra("patient_id", -1)
        if (patientId == -1L) {
            Toast.makeText(this, "Lỗi: không tìm thấy bệnh nhân", Toast.LENGTH_SHORT).show()
            finish()
        }
        setControl()
        loadData()
        setEvent()
    }


    //cac ham xu ly
    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            val patient = dao.getPatientById(patientId)

            runOnUiThread {
                if (patient != null) {
                    etName.setText(patient.fullName)
                    etRecordId.setText(patient.medicalRecordNumber)
                    etAddress.setText(patient.address ?: "")
                    etPhone.setText(patient.phoneNumber ?: "")

                    etAge.setText(patient.calculateAge(patient.dateOfBirth).toString())

                    if (patient.gender == "Nam") rbMale.isChecked = true
                    else rbFemale.isChecked = true


                    if (patient.gender == "Nam") rbMale.isChecked = true else rbFemale.isChecked =
                        true
                }
            }
        }


    }

    private fun updatePatient() {
        val name = etName.text.toString().trim()
        val age = etAge.text.toString().trim().toIntOrNull()
        val recordId = etRecordId.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (name.isEmpty()) return toast("Vui lòng nhập họ tên")
        if (age == null || age <= 0) return toast("Tuổi không hợp lệ")
        if (recordId.isEmpty()) return toast("Vui lòng nhập mã hồ sơ")

        val gender =
            if (rbMale.isChecked) "Nam"
            else "Nữ"

        // age → dateOfBirth
        val dob = ageToDateOfBirth(age)

        val patientUpdated = Patient(
            patientId = patientId,
            fullName = name,
            dateOfBirth = dob,
            gender = gender,
            phoneNumber = phone,
            address = address,
            medicalRecordNumber = recordId
        )


        //luu xuong database
        CoroutineScope(Dispatchers.IO).launch {
            dao.updatePatient(patientUpdated)
            withContext(Dispatchers.Main) {
                toast("Update bệnh nhân thành công!")
                setResult(RESULT_OK)
                //PUT EXTRA to profile
                intent.putExtra("update_patient_id", patientId)
                //log test

                Log.d("update_patient", "update_patient: $patientUpdated")
                Log.d("update_patient_id", "update_patient_id: ${patientUpdated.patientId}")
                intent.putExtra("updated", true)
                setResult(RESULT_OK, intent)
                finish()

            }
        }


    }


    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun ageToDateOfBirth(age: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -age)
        return cal.timeInMillis
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish()
        }

        btnUpdate.setOnClickListener {
            updatePatient()
        }
    }


    private fun setControl() {
        btnBack = findViewById(R.id.btnBack)

        etName = findViewById(R.id.etName)
        etAge = findViewById(R.id.etAge)
        etRecordId = findViewById(R.id.etRecordId)
        etAddress = findViewById(R.id.etAddress)
        etPhone = findViewById(R.id.etPhone)

        rgGender = findViewById(R.id.rgGender)
        rbMale = findViewById(R.id.rbMale)
        rbFemale = findViewById(R.id.rbFemale)

        btnUpdate = findViewById(R.id.btnUpdate)
    }

}