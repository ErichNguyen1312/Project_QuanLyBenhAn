package com.example.projectqlbenhan.ui.authService

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.R

import com.example.projectqlbenhan.ui.home.HomeActivity
import com.example.projectqlbenhan.utils.DatabaseSeeder
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {
    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authService = AuthService(this)

        val db = MedicalRecordDatabase.getDatabase(this)
        lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(db)
        }

        setControl()
        setEvent()
    }

    private fun setControl() {
        edtUsername = findViewById(R.id.edtUsername)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setEvent() {
        btnLogin.setOnClickListener {
            doLogin()
        }
    }

    private fun doLogin() {
        val username = edtUsername.text.toString().trim()
        val password = edtPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            toast("Vui lòng nhập đầy đủ thông tin")
            return
        }

        lifecycleScope.launch {
            val result = authService.login(username, password)
            handleLoginResult(result)
        }
    }

    private fun handleLoginResult(result: AuthService.LoginResult) {
        when (result) {
            is AuthService.LoginResult.SuccessDoctor -> {
                val doctor = result.doctor
                toast("Xin chào BS. ${doctor.fullName}")

                // luu session cua bac si
                SessionManager.saveUserSession(
                    context = this,
                    accountId = doctor.accountId,
                    role = "DOCTOR",
                    specificId = doctor.doctorId,
                    fullName = doctor.fullName
                )
                navigateToHome()
            }

            is AuthService.LoginResult.SuccessPatient -> {

                val patient = result.patient
                val patientName = patient?.fullName ?: "Bệnh nhân mới"
                val patientId = patient?.patientId ?: -1L

                toast("Xin chào $patientName")

                // luu session benh nhan
                SessionManager.saveUserSession(
                    context = this,
                    accountId = result.accountId,
                    role = "PATIENT",
                    specificId = patientId,
                    fullName = patientName
                )
                navigateToHome()
            }

            is AuthService.LoginResult.SuccessAdmin -> {

                toast("Xin chào Quản trị viên")

                // luu session admin
                SessionManager.saveUserSession(
                    context = this,
                    accountId = result.account.accountId,
                    role = "ADMIN",
                    // admin khoong can truong nay, vi quan ly bac si
                    specificId = -1L,
                    fullName = "Admin"
                )
                navigateToHome()
            }

            is AuthService.LoginResult.Error -> {
                toast(result.message)
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}