package com.example.projectqlbenhan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThemBenhNhan : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etRecordId: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var btnSave: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_them_benh_nhan)
        setControl()
        setEvent()
    }

    val listBenhNhan = mutableListOf<BenhNhan>()
    private fun setControl() {
        etName = findViewById(R.id.etName)           // bạn gán id EditText trong XML
        etAge = findViewById(R.id.etAge)
        etRecordId = findViewById(R.id.etRecordId)
        etAddress = findViewById(R.id.etAddress)
        etPhone = findViewById(R.id.etPhone)
        rgGender = findViewById(R.id.rgGender)
        btnSave = findViewById(R.id.btnSave)
    }
    private fun setEvent() {
        btnSave.setOnClickListener {

            val name = etName.text.toString()
            val age = etAge.text.toString().toIntOrNull() ?: 0
            val recordId = etRecordId.text.toString()
            val address = etAddress.text.toString()
            val phone = etPhone.text.toString()

            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Nam"
                R.id.rbFemale -> "Nữ"
                else -> "Không rõ"
            }

            // Tạo bệnh nhân mới
            val bn = BenhNhan(
                name = name,
                age = age,
                gender = gender,
                recordId = recordId,
                address = address,
                phone = phone
            )

            // Thêm vào danh sách tĩnh
            listBenhNhan.add(bn)

            Toast.makeText(this, "Đã lưu bệnh nhân!", Toast.LENGTH_SHORT).show()

//            // Quay lại danh sách
//            finish()

            println("danh sach: ${listBenhNhan.toString()}  ")
        }
    }
}