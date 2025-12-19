package com.example.projectqlbenhan.ui.TaiKham

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.ui.ThongBaoTaiKham.AlarmScheduler
import com.example.projectqlbenhan.ui.ThongBaoTaiKham.Helper_ThongBaoTaiKham
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class screenTaiKham_Create : AppCompatActivity() {
    private lateinit var tvNgayTaiKham: TextView
    private lateinit var tvGioTaiKham: TextView
    private lateinit var tvBacSi: TextView
    private lateinit var edtGhiChu: EditText
    private lateinit var btnSave: Button
    private lateinit var edtBenhAn: EditText

    private lateinit var ct_btnBack: ImageButton
    private lateinit var ct_tvTieuDe: TextView

    private var selectedDateMillis: Long? = null
    private var selectedTime: String? = null
    private var selectedDoctorId: Long? = null
    private var _doctorId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_tai_kham_create)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setControl()
        setEvent()
    }

    private fun setControl() {
        tvNgayTaiKham = findViewById(R.id.tvNgayTaiKham)
        tvGioTaiKham = findViewById(R.id.tvGioTaiKham)
        tvBacSi = findViewById(R.id.tvBacSi)
        edtGhiChu = findViewById(R.id.edtGhiChu)
        edtBenhAn = findViewById(R.id.edtBenhAn)
        btnSave = findViewById(R.id.btnSave)

        ct_btnBack = findViewById(R.id.ct_btnBack)
        ct_tvTieuDe = findViewById(R.id.ct_tvTieuDe)

        ct_tvTieuDe.text = "Tạo lịch tái khám"


        selectedDoctorId = SessionManager.getDoctorId(this)
        val doctorName = SessionManager.getDoctorName(this)
        tvBacSi.text = "$doctorName"
    }


    private fun setEvent() {
        ct_btnBack.setOnClickListener { finish() }
        tvNgayTaiKham.setOnClickListener { showDatePicker() }
        tvGioTaiKham.setOnClickListener { showTimePicker() }
        btnSave.setOnClickListener {
            if (validateInput()) {
                saveAppointment()
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val c = Calendar.getInstance()
                c.set(year, month, day, 0, 0)
                selectedDateMillis = c.timeInMillis

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvNgayTaiKham.text = sdf.format(Date(selectedDateMillis!!))
                tvNgayTaiKham.setTextColor(Color.BLACK)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                tvGioTaiKham.text = selectedTime
                tvGioTaiKham.setTextColor(Color.BLACK)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateInput(): Boolean {

        if (edtBenhAn.text.toString().trim().isEmpty()) {
            toast("Vui lòng nhập mã bệnh án")
            return false
        }

        if (selectedDateMillis == null) {
            toast("Vui lòng chọn ngày tái khám")
            return false
        }

        if (selectedTime.isNullOrEmpty()) {
            toast("Vui lòng chọn giờ tái khám")
            return false
        }

        if (selectedDoctorId == null) {
            toast("Vui lòng chọn bác sĩ")
            return false
        }

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val ngay = sdfDate.format(Date(selectedDateMillis!!))

        val lich = runCatching {
            sdfDateTime.parse("$ngay $selectedTime")?.time
        }.getOrNull()

        if (lich == null || lich <= System.currentTimeMillis()) {
            toast("Thời gian tái khám phải lớn hơn hiện tại")
            return false
        }

        return true
    }


    private fun saveAppointment() {

        if (!validateInput()) return

        val recordIdText = edtBenhAn.text.toString().trim()
        if (recordIdText.isEmpty()) {
            toast("Vui lòng nhập mã bệnh án")
            return
        }

        val recordId = recordIdText.toLongOrNull()
        if (recordId == null) {
            toast("Mã bệnh án không hợp lệ")
            return
        }

        val patientId = intent.getLongExtra("patient_id", -1)
        if (patientId == -1L) {
            toast("Không xác định được bệnh nhân")
            return
        }

        lifecycleScope.launch {

            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Create)

            val record = db.medicalRecordDao()
                .getRecordOfPatient( recordId, patientId)

            if (record == null) {
                toast("Mã bệnh án không thuộc bệnh nhân này")
                return@launch
            }

            val appointment = Appointment(
                recordId = recordId,
                patientId = patientId,
                appointmentDate = selectedDateMillis!!,
                appointmentTime = selectedTime!!,
                location = null,
                doctorId = selectedDoctorId!!,
                notes = edtGhiChu.text.toString()
            )

            val appointmentId = db.appointmentDao().insert(appointment)

            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val dateStr = java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date(selectedDateMillis!!))

            val triggerTime =
                sdf.parse("$dateStr $selectedTime")!!.time

            withContext(Dispatchers.Main) {
                AlarmScheduler.schedule(
                    this@screenTaiKham_Create,
                    appointmentId = appointmentId,
                    triggerAtMillis = triggerTime,
                    timeText = selectedTime!!
                )
            }



            toast("Lưu lịch tái khám thành công")
            finish()
        }
    }




    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
