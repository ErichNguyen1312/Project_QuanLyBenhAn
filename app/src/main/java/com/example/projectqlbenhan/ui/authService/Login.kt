package com.example.projectqlbenhan.ui.authService

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.admin.MainScreen_Admin
import com.example.projectqlbenhan.ui.home.HomeActivity
import com.example.projectqlbenhan.ui.patient_home.PatientHomeActivity
import com.example.projectqlbenhan.utils.DatabaseSeeder
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class Login : AppCompatActivity() {

    private lateinit var etUser: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button


    private lateinit var db : MedicalRecordDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setControl()
        db = MedicalRecordDatabase.getDatabase(this)

        checkAlreadyLogin()
        setEvent()
    }

    private fun setControl() {
        etUser = findViewById(R.id.edtUsername)
        etPass = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setEvent() {
        btnLogin.setOnClickListener {
            handleLogin()
        }
    }



    private fun checkAlreadyLogin() {
        if (SessionManager.isLoggedIn(this)) {
            val role = SessionManager.getRole(this)
            when (role) {
                "DOCTOR" -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                "PATIENT" -> {
                    startActivity(Intent(this, PatientHomeActivity::class.java))
                    finish()
                }
                "ADMIN" -> {
                    startActivity(Intent(this, MainScreen_Admin::class.java))
                    finish()
                }
            }
        }
    }

    private fun handleLogin() {
        val username = etUser.text.toString().trim()
        val password = etPass.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val passwordHash = hashPassword(password)
            val account = db.accountDao().login(username, passwordHash)

            withContext(Dispatchers.Main) {
                if (account != null) {

                    SessionManager.saveAuthToken(this@Login, "token_demo")
                    SessionManager.saveUserRole(this@Login, account.role)
                    SessionManager.saveAccountId(this@Login, account.accountId)


                    when (account.role) {
                        "DOCTOR" -> {
                            checkDoctorAndRedirect(account.accountId)
                        }
                        "PATIENT" -> {
                            checkPatientAndRedirect(account.accountId)
                        }
                        "ADMIN" -> {
                            checkAdminAndRedirect(account.accountId)
                        }
                        else -> {
                            Toast.makeText(this@Login, "Role không hợp lệ: ${account.role}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@Login, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun checkDoctorAndRedirect(accountId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val doctor = db.doctorDao().getDoctorByAccountId(accountId)
            withContext(Dispatchers.Main) {
                if (doctor != null) {
                    SessionManager.saveSpecificId(this@Login, doctor.doctorId)
                    SessionManager.saveFullName(this@Login, doctor.fullName)


                    startActivity(Intent(this@Login, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@Login, "Tài khoản này chưa có hồ sơ Bác sĩ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun checkPatientAndRedirect(accountId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val patient = db.patientDao().getPatientByAccountId(accountId)
            withContext(Dispatchers.Main) {
                if (patient != null) {
                    SessionManager.saveSpecificId(this@Login, patient.patientId)
                    SessionManager.saveFullName(this@Login, patient.fullName)


                    startActivity(Intent(this@Login, PatientHomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@Login, "Tài khoản này chưa có hồ sơ Bệnh nhân", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkAdminAndRedirect(accountId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val admin = db.accountDao().getAccountById(accountId)
            withContext(Dispatchers.Main) {
                if (admin != null) {
                    SessionManager.saveSpecificId(this@Login, -1)
                    startActivity(Intent(this@Login, MainScreen_Admin::class.java))
                    finish()
                } else {
                    Toast.makeText(this@Login, "Không tìm tấy tài khoản", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}