package com.example.projectqlbenhan.ui.BacSi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.ui.home.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class screenBacSi_Edit : AppCompatActivity() {

    private var doctorId: Long = -1
    private var currentDoctor: Doctor? = null

    // Mã đánh dấu hành động xóa để trả về cho DetailActivity biết
    companion object {
        const val RESULT_DELETE = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_bac_si_edit)

        doctorId = intent.getLongExtra("doctor_id", -1)
        if (doctorId == -1L) { finish(); return }

        initViewAndData()
    }

    private fun initViewAndData() {
        val etName = findViewById<EditText>(R.id.etFullName)
        val etSpec = findViewById<EditText>(R.id.etSpecialization)
        val etDesc = findViewById<EditText>(R.id.etDescription)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        btnBack.setOnClickListener { finish() }

        // 1. Load dữ liệu cũ
        lifecycleScope.launch(Dispatchers.IO) {
            val db = MedicalRecordDatabase.getDatabase(this@screenBacSi_Edit)
            currentDoctor = db.doctorDao().getDoctorById(doctorId)

            withContext(Dispatchers.Main) {
                currentDoctor?.let {
                    etName.setText(it.fullName)
                    etSpec.setText(it.specialization)
                    etDesc.setText(it.description)
                }
            }
        }

        // 2. Xử lý CẬP NHẬT
        btnUpdate.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) return@setOnClickListener

            lifecycleScope.launch(Dispatchers.IO) {
                val db = MedicalRecordDatabase.getDatabase(this@screenBacSi_Edit)

                currentDoctor?.let { oldData ->
                    val updatedDoctor = oldData.copy(
                        fullName = name,
                        specialization = etSpec.text.toString(),
                        description = etDesc.text.toString()
                    )
                    db.doctorDao().update(updatedDoctor)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@screenBacSi_Edit, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        // 3. Xử lý XÓA
        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cảnh báo")
            .setMessage("Bạn có chắc muốn xóa bác sĩ này?\nTài khoản đăng nhập tương ứng cũng sẽ bị xóa.")
            .setPositiveButton("Xóa") { _, _ ->
                deleteDoctorAndAccount()


                val intent = Intent(this, screenBacSi_Main::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteDoctorAndAccount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = MedicalRecordDatabase.getDatabase(this@screenBacSi_Edit)
            currentDoctor?.let { doc ->
                // Bước 1: Xóa Bác sĩ
                db.doctorDao().Delete(doc)

                // Bước 2: Xóa luôn Account tương ứng (để sạch Data)
                val account = db.accountDao().getAccountById(doc.accountId)
                if (account != null) {
                    db.accountDao().Delete(account)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@screenBacSi_Edit, "Đã xóa bác sĩ", Toast.LENGTH_SHORT).show()
                setResult(RESULT_DELETE)
                finish()
            }
        }
    }
}