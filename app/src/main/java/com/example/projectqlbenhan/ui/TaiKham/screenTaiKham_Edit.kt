package com.example.projectqlbenhan.ui.TaiKham

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class screenTaiKham_Edit : AppCompatActivity() {

    private lateinit var tvNgayTaiKham: TextView
    private lateinit var tvGioTaiKham: TextView
    private lateinit var edtGhiChu: EditText
    private lateinit var btnSave: Button
    private lateinit var btnXoa: Button

    private lateinit var ct_btnBack: ImageButton
    private lateinit var ct_tvTieuDe: TextView

    private var appointmentId: Long = -1
    private var patientId: Long = -1
    private var recordId: Long = -1
    private var doctorId: Long = -1

    private var selectedDateMillis: Long? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_tai_kham_edit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setControl()
        getIntentData()
        setEvent()
    }

    private fun setControl() {
        tvNgayTaiKham = findViewById(R.id.tvNgayTaiKham)
        tvGioTaiKham = findViewById(R.id.tvGioTaiKham)
        edtGhiChu = findViewById(R.id.edtGhiChu)
        btnSave = findViewById(R.id.btnSave)

        ct_btnBack = findViewById(R.id.ct_btnBack)
        ct_tvTieuDe = findViewById(R.id.ct_tvTieuDe)
        btnXoa = findViewById(R.id.btnXoa)

        ct_tvTieuDe.text = "Chỉnh sửa lịch tái khám"
        btnSave.text = "Cập nhật lịch tái khám"

    }

    private fun getIntentData() {
        appointmentId = intent.getLongExtra("maTaiKham", -1)
        patientId = intent.getLongExtra("maBenhNhan", -1)
        recordId = intent.getLongExtra("maBenhAn", -1)
        doctorId = intent.getLongExtra("maBacSi", -1)

        selectedDateMillis = intent.getLongExtra("ngayTaiKham", -1)
        if (selectedDateMillis == -1L) {
            toast("Không tìm thấy lịch tái khám")
            finish()
            return
        }

        selectedTime = intent.getStringExtra("gioTaiKham")

        edtGhiChu.setText(intent.getStringExtra("ghiChu"))

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvNgayTaiKham.text = sdf.format(Date(selectedDateMillis!!))
        tvNgayTaiKham.setTextColor(Color.BLACK)

        tvGioTaiKham.text = selectedTime
        tvGioTaiKham.setTextColor(Color.BLACK)
    }

    private fun setEvent() {
        ct_btnBack.setOnClickListener { finish() }

        tvNgayTaiKham.setOnClickListener { showDatePicker() }
        tvGioTaiKham.setOnClickListener { showTimePicker() }

        btnSave.setOnClickListener {
            if (validateInput()) {
                updateAppointment()
            }
        }
        btnXoa.setOnClickListener {
            showConfirmDelete()
        }

    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDateMillis ?: System.currentTimeMillis()

        DatePickerDialog(
            this,
            { _, y, m, d ->
                val c = Calendar.getInstance()
                c.set(y, m, d, 0, 0)
                selectedDateMillis = c.timeInMillis

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvNgayTaiKham.text = sdf.format(Date(selectedDateMillis!!))
                tvNgayTaiKham.setTextColor(Color.BLACK)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m)
                tvGioTaiKham.text = selectedTime
                tvGioTaiKham.setTextColor(Color.BLACK)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateInput(): Boolean {
        if (selectedDateMillis == null || selectedTime.isNullOrEmpty()) {
            toast("Vui lòng chọn ngày và giờ tái khám")
            return false
        }

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val ngay = sdfDate.format(Date(selectedDateMillis!!))
        val lich = sdfDateTime.parse("$ngay $selectedTime")?.time

        if (lich == null) {
            toast("Thời gian không hợp lệ")
            return false
        }

        if (lich <= System.currentTimeMillis()) {
            toast("Thời gian tái khám phải lớn hơn hiện tại")
            return false
        }

        return true
    }


    private fun updateAppointment() {
        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Edit)
            db.appointmentDao().updateAppointment(
                appointmentId,
                selectedDateMillis!!,
                selectedTime!!,
                edtGhiChu.text.toString()
            )


            toast("Cập nhật lịch tái khám thành công")
            setResult(RESULT_OK)
            finish()
        }
    }
    private fun showConfirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa lịch tái khám này không?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteAppointment()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun deleteAppointment() {
        btnXoa.isEnabled = false

        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Edit)
            db.appointmentDao().deleteById(appointmentId)

            toast("Đã xóa lịch tái khám")
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
