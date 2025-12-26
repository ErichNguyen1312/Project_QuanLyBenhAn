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
import androidx.lifecycle.lifecycleScope // Dùng cái này an toàn hơn CoroutineScope tự tạo
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.utils.MrnGenerator
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

    // Lazy load DAO
    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).patientDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_patient)

        setControl()
        autoFillMrn()
        setEvent()
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { savePatient() }
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

    // --- CÁC HÀM XỬ LÝ ---
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

        // Tạo Patient khớp với Entity mới
        val newPatient = Patient(
            patientId = 0, // Mặc định để AutoGenerate
            accountId = null, // ⭐️ MỚI: Thêm trường này (null vì tạo offline)
            fullName = name,
            medicalRecordNumber = recordNumber,
            dateOfBirth = dateOfBirthTimestamp,
            gender = gender,
            phoneNumber = phone,
            address = address
            // createdAt tự động lấy thời gian hiện tại
        )

        // Lưu xuống database (Dùng lifecycleScope)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                dao.insertPatient(newPatient)

                // ⭐️ QUAN TRỌNG: Chỉ đóng màn hình khi đã lưu xong
                withContext(Dispatchers.Main) {
                    toast("Thêm bệnh nhân thành công!")
                    finish() // <--- Finish ở đây mới đúng logic
                }
            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                // Xử lý trùng mã hồ sơ
                val newMrn = MrnGenerator.generateUnique(dao)
                withContext(Dispatchers.Main) {
                    etRecordId.setText(newMrn)
                    Toast.makeText(this@CreatePatient, "Mã hồ sơ bị trùng. Hệ thống đã tạo mã mới: $newMrn", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CreatePatient", "Error", e)
                    toast("Lỗi: ${e.message}")
                }
            }
        }
        // ❌ Đã xóa finish() ở ngoài này để tránh lỗi đóng app sớm
    }

    private fun convertAgeToDob(age: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -age)
        return cal.timeInMillis
    }

    private fun autoFillMrn() {
        lifecycleScope.launch(Dispatchers.IO) {
            val mrn = MrnGenerator.generateUnique(dao)
            withContext(Dispatchers.Main) {
                etRecordId.setText(mrn)
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}