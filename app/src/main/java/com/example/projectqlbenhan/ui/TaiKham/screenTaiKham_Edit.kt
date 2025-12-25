package com.example.projectqlbenhan.ui.TaiKham

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
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
    private lateinit var ct_tvTieuDe: TextView
    private lateinit var ct_btnBack: ImageButton

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
        ct_tvTieuDe = findViewById(R.id.ct_tvTieuDe)
        ct_btnBack = findViewById(R.id.ct_btnBack)

        ct_tvTieuDe.text = "Chỉnh sửa lịch tái khám - Trần Thiện Trí"
    }

    private fun loadDoctorsAndSetSelection() {
        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Edit)
            doctorList = withContext(Dispatchers.IO) {
                db.doctorDao().getAll()
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
        tvNgayTaiKham.setOnClickListener {
            val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                selectedDateCalendar.set(Calendar.YEAR, year)
                selectedDateCalendar.set(Calendar.MONTH, month)
                selectedDateCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateTimeDisplay()
            }
            DatePickerDialog(this, dateListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        tvChonGio.setOnClickListener {
            val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hour)
                selectedDateCalendar.set(Calendar.MINUTE, minute)
                selectedDateCalendar.set(Calendar.SECOND, 0)
                updateTimeDisplay()
            }
            TimePickerDialog(this, timeListener,
                selectedDateCalendar.get(Calendar.HOUR_OF_DAY),
                selectedDateCalendar.get(Calendar.MINUTE), true).show()
        }

        btnSave.setOnClickListener {
            updateAppointment()
        }

        btnXoa.setOnClickListener {
            showConfirmDelete()
        }

        ct_btnBack.setOnClickListener { finish() }
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

            db.appointmentDao().updateAppointmentTri(
                id = appointmentId,
                date = selectedDateCalendar.timeInMillis,
                notes = edtGhiChu.text.toString(),
                doctorId = newSelectedDoctorId
            )

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