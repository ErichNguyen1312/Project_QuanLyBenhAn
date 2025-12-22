package com.example.projectqlbenhan.ui.TaiKham

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.doctor.Doctor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class screenTaiKham_Edit : AppCompatActivity() {

    private lateinit var tvNgayTaiKham: TextView
    private lateinit var tvChonGio: TextView
    private lateinit var edtGhiChu: EditText
    private lateinit var btnSave: Button
    private lateinit var btnXoa: Button
    private lateinit var spnBacSi: Spinner

    private var appointmentId: Long = -1
    private var currentDoctorId: Long = -1
    private var selectedDateCalendar = Calendar.getInstance()

    private var doctorList: List<Doctor> = listOf()
    private var newSelectedDoctorId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_tai_kham_edit)

        initView()

        // Lấy dữ liệu từ Intent
        appointmentId = intent.getLongExtra("appointmentId", -1)
        val dateMillis = intent.getLongExtra("appointmentDate", System.currentTimeMillis())
        currentDoctorId = intent.getLongExtra("doctorId", -1)
        val note = intent.getStringExtra("ghiChu")

        selectedDateCalendar.timeInMillis = dateMillis

        // Hiển thị dữ liệu cũ
        updateTimeDisplay()
        edtGhiChu.setText(note)

        loadDoctorsAndSetSelection()
        setEvent()
    }

    private fun initView() {
        tvNgayTaiKham = findViewById(R.id.tvNgayTaiKham)
        tvChonGio = findViewById(R.id.tvChonGio)
        edtGhiChu = findViewById(R.id.edtGhiChu)
        btnSave = findViewById(R.id.btnSave)
        btnXoa = findViewById(R.id.btnXoa)
        spnBacSi = findViewById(R.id.spnBacSi)
    }

    private fun loadDoctorsAndSetSelection() {
        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Edit)
            // Fetch list bác sĩ (Giống bên Create)
            doctorList = withContext(Dispatchers.IO) {
                // db.doctorDao().getAll() // Thay bằng hàm DAO của bạn
                emptyList() // Placeholder
            }

            val doctorNames = doctorList.map { it.fullName }
            val adapter = ArrayAdapter(this@screenTaiKham_Edit, android.R.layout.simple_spinner_dropdown_item, doctorNames)
            spnBacSi.adapter = adapter

            // Tìm vị trí bác sĩ cũ để set selection
            val index = doctorList.indexOfFirst { it.doctorId == currentDoctorId }
            if (index != -1) {
                spnBacSi.setSelection(index)
            }

            spnBacSi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    if (doctorList.isNotEmpty()) {
                        newSelectedDoctorId = doctorList[position].doctorId
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setEvent() {
        // ... (Logic DatePicker và TimePicker GIỐNG HỆT bên Create, bạn copy sang nhé) ...
        tvNgayTaiKham.setOnClickListener { /* Copy logic DatePicker từ file Create */ }
        tvChonGio.setOnClickListener { /* Copy logic TimePicker từ file Create */ }

        btnSave.setOnClickListener {
            updateAppointment()
        }

        btnXoa.setOnClickListener {
            showConfirmDelete()
        }
    }

    private fun updateTimeDisplay() {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvNgayTaiKham.text = sdfDate.format(selectedDateCalendar.time)
        tvChonGio.text = sdfTime.format(selectedDateCalendar.time)
    }

    private fun updateAppointment() {
        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Edit)
            val note = edtGhiChu.text.toString()
            val newTime = selectedDateCalendar.timeInMillis

            // Vì AppointmentDao.updateAppointment(id, date, notes) của bạn chỉ update date và notes
            // Bạn cần viết thêm Query update cả DoctorId hoặc dùng hàm @Update update(appointment: Appointment)

            // Cách dùng update object (Recommended):
            // 1. Lấy object cũ
            // val oldAppt = db.appointmentDao().getById(appointmentId)
            // 2. Tạo object mới với data thay đổi
            // val newAppt = oldAppt.copy(appointmentDate = newTime, doctorId = newSelectedDoctorId, ...)
            // 3. db.appointmentDao().update(newAppt)

            // Tạm thời mình gọi hàm update custom (Bạn cần thêm param doctorId vào DAO nếu chưa có)
            db.appointmentDao().updateAppointment(appointmentId, newTime, note)
            // Lưu ý: Nếu muốn update cả bác sĩ, bạn phải sửa DAO:
            // @Query("UPDATE appointments SET appointmentDate=:d, reason=:n, doctor_id=:doc WHERE appointmentId=:id")

            Toast.makeText(this@screenTaiKham_Edit, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun showConfirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn chắc chắn muốn xóa lịch hẹn này?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Edit)
                    db.appointmentDao().deleteById(appointmentId)
                    Toast.makeText(this@screenTaiKham_Edit, "Đã xóa", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}