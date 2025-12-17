package com.example.projectqlbenhan.ui.loginService

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.home.HomeActivity
import com.example.projectqlbenhan.ui.patient.Patients
import com.example.projectqlbenhan.utils.Doctor
import com.example.projectqlbenhan.utils.PasswordUtils
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {
    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).doctorDao()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setControl()
        setEvent()
    }

    private fun setEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            Doctor.seed(dao)
        }

        btnLogin.setOnClickListener {
            doLogin()
        }
    }

    private fun setControl() {
        edtUsername = findViewById(R.id.edtUsername)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    //cac ham xu ly

    private fun doLogin() {
        val username = edtUsername.text.toString().trim()
        val password = edtPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            toast("Vui lòng nhập đầy đủ thông tin")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val hash = PasswordUtils.hash(password)
            val doctor = dao.login(username, hash)

            withContext(Dispatchers.Main) {
                if (doctor != null) {
                    SessionManager.saveDoctorSession(context = this@Login, doctor.doctorId, doctor.fullName)
                    toast("Đăng nhập thành công")

                    startActivity(
                        Intent(this@Login, HomeActivity::class.java)
                    )
                    finish()
                } else {
                    toast("Sai tài khoản hoặc mật khẩu")
                }
            }
        }
    }

//    private fun saveSession(doctor: com.example.projectqlbenhan.entity.doctor.Doctor) {
//        val prefs = getSharedPreferences("session", MODE_PRIVATE)
//        prefs.edit()
//            .putLong("doctor_id", doctor.doctorId)
//            .putString("doctor_name", doctor.fullName)
//            .apply()
//    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}