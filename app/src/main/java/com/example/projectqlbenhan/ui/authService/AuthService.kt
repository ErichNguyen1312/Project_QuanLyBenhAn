package com.example.projectqlbenhan.ui.authService

import android.content.Context
import com.example.projectqlbenhan.database.MedicalRecordDatabase

import com.example.projectqlbenhan.entity.account.Account
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.patient.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthService(context: Context) {
    // Khởi tạo DB và DAO
    private val db = MedicalRecordDatabase.getDatabase(context)
    private val accountDao = db.accountDao()
    private val doctorDao = db.doctorDao()
    private val patientDao = db.patientDao()

    sealed class LoginResult {
        data class SuccessDoctor(val doctor: Doctor) : LoginResult()
        data class SuccessPatient(val patient: Patient?, val accountId: Long) : LoginResult()
        data class SuccessAdmin(val account: Account) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    // Hàm Login chính (Chạy trong Background thread)
    suspend fun login(username: String, rawPass: String): LoginResult = withContext(Dispatchers.IO) {
        val hashedPass = hashPassword(rawPass)

        val account = accountDao.login(username, hashedPass)
            ?: return@withContext LoginResult.Error("Sai tên đăng nhập hoặc mật khẩu")

        if (!account.isActive) return@withContext LoginResult.Error("Tài khoản đã bị khóa")

        //kiem tra role de phan quyen
        return@withContext when (account.role) {
            "DOCTOR" -> {
                val doctor = doctorDao.getDoctorByAccountId(account.accountId)
                if (doctor != null) LoginResult.SuccessDoctor(doctor)
                else LoginResult.Error("Lỗi dữ liệu bác sĩ (Không tìm thấy profile)")
            }
            "PATIENT" -> {
                val patient = patientDao.getPatientByAccountId(account.accountId)
                LoginResult.SuccessPatient(patient, account.accountId)
            }
            "ADMIN" -> LoginResult.SuccessAdmin(account)
            else -> LoginResult.Error("Vai trò không hợp lệ")
        }
    }

    // đổi mật khẩu
    suspend fun changePassword(accountId: Long, newPass: String) = withContext(Dispatchers.IO) {
        val hashedNewPass = hashPassword(newPass)
        accountDao.updatePassword(accountId, hashedNewPass)
    }


    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}