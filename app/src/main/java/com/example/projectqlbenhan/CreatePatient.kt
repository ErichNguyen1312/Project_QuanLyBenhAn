package com.example.projectqlbenhan

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_patient)

        setControl()
        setEvent()
    }

    private fun setEvent() {

        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            savePatient()
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

        btnSave = findViewById(R.id.btnSave)
    }

    //cac ham xu ly

    private fun savePatient() {

        val name = etName.text.toString().trim()
        val ageText = etAge.text.toString().trim()
        val recordId = etRecordId.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validate
        if (name.isEmpty()) {
            toast("Vui lòng nhập họ tên")
            return
        }
        if (ageText.isEmpty()) {
            toast("Vui lòng nhập tuổi")
            return
        }

        val age = ageText.toIntOrNull()
        if (age == null || age <= 0) {
            toast("Tuổi không hợp lệ")
            return
        }

        if (recordId.isEmpty()) {
            toast("Vui lòng nhập mã hồ sơ")
            return
        }

        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Nam"
            R.id.rbFemale -> "Nữ"
            else -> {
                toast("Vui lòng chọn giới tính")
                return
            }
        }

        // Tạo object Patient
        val newPatient = Patient(
            name = name,
            age = age,
            gender = gender,
            recordId = recordId,
            address = address,
            phone = phone
        )

        // TRẢ DỮ LIỆU LẠI CHO MÀN PATIENT LIST
        val resultIntent = intent
        resultIntent.putExtra("newPatient_name", newPatient.name)
        resultIntent.putExtra("newPatient_age", newPatient.age)
        resultIntent.putExtra("newPatient_gender", newPatient.gender)
        resultIntent.putExtra("newPatient_recordId", newPatient.recordId)
        resultIntent.putExtra("newPatient_address", newPatient.address)
        resultIntent.putExtra("newPatient_phone", newPatient.phone)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    //xu ly toast thong bao
    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    }
}