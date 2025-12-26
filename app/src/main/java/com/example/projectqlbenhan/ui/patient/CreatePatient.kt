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
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.account.Account
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.utils.MrnGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Calendar

class CreatePatient : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etRecordId: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var btnSave: Button

    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_patient)
        setControl()
        autoFillMrn()
        setEvent()
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
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { savePatientWithAccount() } // Đổi tên hàm
    }

    private fun savePatientWithAccount() {
        val name = etName.text.toString().trim()
        val ageStr = etAge.text.toString().trim()
        val recordNumber = etRecordId.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validate
        if (name.isEmpty()) return toast("Vui lòng nhập họ tên")
        if (phone.isEmpty()) return toast("Vui lòng nhập SĐT (để làm tài khoản)")
        if (ageStr.isEmpty()) return toast("Vui lòng nhập tuổi")
        val age = ageStr.toIntOrNull()
        if (age == null || age <= 0) return toast("Tuổi không hợp lệ")

        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Nam"
            R.id.rbFemale -> "Nữ"
            else -> return toast("Vui lòng chọn giới tính")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val isExist = db.accountDao().isUsernameExist(phone)
            if (isExist) {
                withContext(Dispatchers.Main) {
                    toast("Số điện thoại này đã có tài khoản!")
                }
                return@launch
            }

            try {
                val defaultPass = hashPassword("123456")
                val newAccount = Account(
                    username = phone,
                    passwordHash = defaultPass,
                    role = "PATIENT"
                )
                val newAccountId = db.accountDao().insertAccount(newAccount)

                val dob = convertAgeToDob(age)
                val newPatient = Patient(
                    accountId = newAccountId,
                    fullName = name,
                    dateOfBirth = dob,
                    gender = gender,
                    phoneNumber = phone,
                    address = address,
                    medicalRecordNumber = recordNumber
                )

                db.patientDao().insertPatient(newPatient)

                withContext(Dispatchers.Main) {
                    toast("Thêm thành công! Tài khoản: $phone, MK: 123456")
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Lỗi: ${e.message}")
                    Log.e("CreatePatient", "Error", e)
                }
            }
        }
    }

    private fun convertAgeToDob(age: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -age)
        return cal.timeInMillis
    }

    private fun autoFillMrn() {
        lifecycleScope.launch(Dispatchers.IO) {
            val mrn = MrnGenerator.generateUnique(db.patientDao())
            withContext(Dispatchers.Main) {
                etRecordId.setText(mrn)
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}