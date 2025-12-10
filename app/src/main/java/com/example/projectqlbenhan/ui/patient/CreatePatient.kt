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
import com.example.projectqlbenhan.entity.Patient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).patientDao()
    }

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
        val ageStr = etAge.text.toString().trim()
        val recordNumber = etRecordId.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validate
        if (name.isEmpty()) return toast("Vui lòng nhập họ tên")
        if (ageStr.isEmpty()) return toast("Vui lòng nhập tuổi")

        val age = ageStr.toIntOrNull()
        if (age == null || age <= 0) return toast("Tuổi không hợp lệ")

        if (recordNumber.isEmpty()) return toast("Vui lòng nhập mã hồ sơ")

        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Nam"
            R.id.rbFemale -> "Nữ"
            else -> return toast("Vui lòng chọn giới tính")
        }

        val dateOfBirthTimestamp = convertAgeToDob(age)



        // Tạo  Patient
        val newPatient = Patient(
            fullName = name,
            dateOfBirth = dateOfBirthTimestamp,
            gender = gender,
            phoneNumber = phone,
            address = address,
            medicalRecordNumber = recordNumber
        )

        //luu xuong database
        CoroutineScope(Dispatchers.IO).launch {
            val id = dao.insertPatient(newPatient)

            withContext(Dispatchers.Main) {
                if (id > 0) {
                    toast("Thêm bệnh nhân thành công!")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    toast("Thêm thất bại!")
                }
            }
        }
        finish()

        //log test
        Log.d("new_patient", "patient: $newPatient")

    }

    //convert tuoi sang date of birth
    fun convertAgeToDob(age: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -age)
        return cal.timeInMillis
    }
    //xu ly toast thong bao
    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    }
}