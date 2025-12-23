package com.example.projectqlbenhan.ui.patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.patient.Patient
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

    // Biến để lưu giữ thông tin cũ (tránh bị mất khi update)
    private var currentAccountId: Long? = null
    private var currentCreatedAt: Long = System.currentTimeMillis()

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
            return
        }
        setControl()
        loadData()
        setEvent()
    }


    //cac ham xu ly
    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val patient = dao.getPatientById(patientId)

            withContext(Dispatchers.Main) {
                if (patient != null) {
                    // 1. Lưu lại dữ liệu cũ quan trọng
                    currentAccountId = patient.accountId
                    currentCreatedAt = patient.createdAt

                    // 2. Đổ dữ liệu lên giao diện
                    etName.setText(patient.fullName)
                    etRecordId.setText(patient.medicalRecordNumber)
                    etAddress.setText(patient.address ?: "")
                    etPhone.setText(patient.phoneNumber ?: "")

                    etAge.setText(calculateAge(patient.dateOfBirth).toString())

                    if (patient.gender == "Nam") rbMale.isChecked = true
                    else rbFemale.isChecked = true
                } else {
                    toast("Không tìm thấy thông tin bệnh nhân")
                    finish()
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

        val gender = if (rbMale.isChecked) "Nam" else "Nữ"
        val dob = ageToDateOfBirth(age)

        // ⭐️ FIX LỖI Ở ĐÂY: Truyền đủ tham số cho Constructor mới
        val patientUpdated = Patient(
            patientId = patientId,
            accountId = currentAccountId, // Truyền lại accountId cũ (quan trọng!)
            fullName = name,
            medicalRecordNumber = recordId,
            dateOfBirth = dob,
            gender = gender,
            phoneNumber = phone,
            address = address,
            createdAt = currentCreatedAt // Giữ nguyên ngày tạo cũ
        )

        // Lưu xuống database
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                dao.updatePatient(patientUpdated)
                withContext(Dispatchers.Main) {
                    toast("Cập nhật thành công!")

                    // Trả kết quả về
                    val resultIntent = Intent()
                    resultIntent.putExtra("update_patient_id", patientId)
                    resultIntent.putExtra("updated", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Lỗi cập nhật: ${e.message}")
                    Log.e("UpdatePatient", "Error: ", e)
                }
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
        btnBack.setOnClickListener { finish() }
        btnUpdate.setOnClickListener { updatePatient() }
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

    private fun calculateAge(dob: Long): Int {
        if (dob == 0L) return 0
        val dobCal = java.util.Calendar.getInstance()
        dobCal.timeInMillis = dob
        val today = java.util.Calendar.getInstance()
        var age = today.get(java.util.Calendar.YEAR) - dobCal.get(java.util.Calendar.YEAR)
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < dobCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--
        }
        return if (age < 0) 0 else age
    }
}