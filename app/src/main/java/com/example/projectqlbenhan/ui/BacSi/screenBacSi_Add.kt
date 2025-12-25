package com.example.projectqlbenhan.ui.BacSi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.account.Account
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class screenBacSi_Add : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_bac_si_add)

        val etName = findViewById<EditText>(R.id.etFullName)
        val etSpec = findViewById<EditText>(R.id.etSpecialization)
        val etDesc = findViewById<EditText>(R.id.etDescription)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Nhập tên bác sĩ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val db = MedicalRecordDatabase.getDatabase(this@screenBacSi_Add)

                val index = db.doctorDao().countDoctors() + 1

                val newAccount = Account(
                    username = "doctor0${index}",
                    passwordHash = PasswordUtils.hash("123456"),
                    role = "DOCTOR")

                val idAccount = db.accountDao().insertAccount(newAccount)

                val newDoctor = Doctor(
                    accountId = idAccount,
                    fullName = name,
                    specialization = etSpec.text.toString(),
                    description = etDesc.text.toString()
                )
                db.doctorDao().insert(newDoctor)

                withContext(Dispatchers.Main) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }
}