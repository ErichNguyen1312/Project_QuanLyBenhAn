package com.example.projectqlbenhan.ui.TaiKham

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.ui.ThongBaoTaiKham.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class screenTaiKham_Create : AppCompatActivity() {

    private lateinit var tvChonNgay: TextView
    private lateinit var tvChonGio: TextView
    private lateinit var edtGhiChu: EditText

    private lateinit var spnBenhAn: Spinner

    private lateinit var btnSave: Button
    private lateinit var spnBacSi: Spinner

    private var selectedDateCalendar: Calendar = Calendar.getInstance()


    private var doctorList: List<Doctor> = listOf()
    private var medicalRecordList: List<MedicalRecord> = listOf()


    private var selectedDoctorId: Long? = null
    private var selectedRecordId: Long? = null


    private var patientId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_tai_kham_create)

        initView()


        patientId = intent.getLongExtra("patient_id", -1)

        loadDoctors()
        loadMedicalRecords()

        setEvent()
    }

    private fun initView() {
        tvChonNgay = findViewById(R.id.tvChonNgay)
        tvChonGio = findViewById(R.id.tvChonGio)
        edtGhiChu = findViewById(R.id.edtGhiChu)


        spnBenhAn = findViewById(R.id.spnBenhAn)

        btnSave = findViewById(R.id.btnSave)
        spnBacSi = findViewById(R.id.spnBacSi)
    }


    private fun loadMedicalRecords() {
        if (patientId == -1L) return

        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Create)


            medicalRecordList = withContext(Dispatchers.IO) {
                db.medicalRecordDao().getRecordsOfPatient(patientId)
            }

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val displayList = mutableListOf<String>()


            displayList.add("Chọn bệnh án cũ (Không bắt buộc)")

            displayList.addAll(medicalRecordList.map { record ->
                val dateStr = sdf.format(Date(record.examinationDate))
                "$dateStr - ${record.diagnosis}"
            })

            val adapter = ArrayAdapter(
                this@screenTaiKham_Create,
                android.R.layout.simple_spinner_dropdown_item,
                displayList
            )
            spnBenhAn.adapter = adapter

            spnBenhAn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        selectedRecordId = null
                    } else {

                        selectedRecordId = medicalRecordList.getOrNull(position - 1)?.recordId
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun loadDoctors() {
        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Create)
            doctorList = withContext(Dispatchers.IO) {
                db.doctorDao().getAll()
            }

            val doctorNames = doctorList.map { it.fullName }
            val adapter = ArrayAdapter(this@screenTaiKham_Create, android.R.layout.simple_spinner_dropdown_item, doctorNames)
            spnBacSi.adapter = adapter

            spnBacSi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (doctorList.isNotEmpty()) {
                        selectedDoctorId = doctorList[position].doctorId
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setEvent() {
        // Chọn Ngày
        tvChonNgay.setOnClickListener {
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

        // Chọn Giờ
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
            saveAppointment()
        }
    }

    private fun updateTimeDisplay() {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvChonNgay.text = sdfDate.format(selectedDateCalendar.time)
        tvChonGio.text = sdfTime.format(selectedDateCalendar.time)
    }

    private fun saveAppointment() {
        if (selectedDoctorId == null) {
            Toast.makeText(this, "Vui lòng chọn bác sĩ", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDateCalendar.timeInMillis < System.currentTimeMillis()) {
            Toast.makeText(this, "Thời gian hẹn phải ở tương lai", Toast.LENGTH_SHORT).show()
            return
        }

        var note = edtGhiChu.text.toString()
        val finalTime = selectedDateCalendar.timeInMillis


        if (selectedRecordId != null) {
            note = "$note (Tái khám theo hồ sơ ID: $selectedRecordId)"
        }

        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Create)

            // Tạo entity Appointment mới
            val newAppt = Appointment(
                patientId = patientId,
                doctorId = selectedDoctorId,
                appointmentDate = finalTime,
                status = "SCHEDULED",
                reason = note
            )

            val newId = db.appointmentDao().insert(newAppt)

            AlarmScheduler.schedule(
                this@screenTaiKham_Create,
                newId,
                finalTime
            )

            Toast.makeText(this@screenTaiKham_Create, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}