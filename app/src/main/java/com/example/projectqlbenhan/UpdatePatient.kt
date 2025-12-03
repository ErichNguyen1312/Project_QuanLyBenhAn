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

    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_patient)
        setControl()
        loadData()
        setEvent()
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

    //cac ham xu ly
    private fun loadData() {
        val intent = intent

        etName.setText(intent.getStringExtra("name"))
        etAge.setText(intent.getIntExtra("age", 0).toString())
        etRecordId.setText(intent.getStringExtra("recordId"))
        etAddress.setText(intent.getStringExtra("address"))
        etPhone.setText(intent.getStringExtra("phone"))

        val gender = intent.getStringExtra("gender") ?: ""

        if (gender == "Nam") rbMale.isChecked = true
        else rbFemale.isChecked = true

        index = intent.getIntExtra("index", -1)
    }

    private fun updatePatient() {
        val name = etName.text.toString().trim()
        val age = etAge.text.toString().trim()
        val recordId = etRecordId.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (name.isEmpty()) {
            toast("Vui lòng nhập họ tên"); return
        }
        if (age.isEmpty()) {
            toast("Vui lòng nhập tuổi"); return
        }
        if (recordId.isEmpty()) {
            toast("Vui lòng nhập mã hồ sơ"); return
        }

        val gender =
            if (rbMale.isChecked) "Nam"
            else "Nữ"

        val updatedPatient = Patient(
            name = name,
            age = age.toInt(),
            gender = gender,
            recordId = recordId,
            address = address,
            phone = phone
        )

        val resultIntent = intent
        resultIntent.putExtra("updated_name", updatedPatient.name)
        resultIntent.putExtra("updated_age", updatedPatient.age)
        resultIntent.putExtra("updated_gender", updatedPatient.gender)
        resultIntent.putExtra("updated_recordId", updatedPatient.recordId)
        resultIntent.putExtra("updated_address", updatedPatient.address)
        resultIntent.putExtra("updated_phone", updatedPatient.phone)

        resultIntent.putExtra("index", index)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}