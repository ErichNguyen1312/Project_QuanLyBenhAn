package com.example.projectqlbenhan.ui.TaiKham

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.doctor.Doctor
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
    private lateinit var edtBenhAn: EditText // Hoặc dùng để nhập User nếu cần
    private lateinit var btnSave: Button
    private lateinit var spnBacSi: Spinner // Spinner chọn bác sĩ

    private var selectedDateCalendar: Calendar = Calendar.getInstance()
    private var doctorList: List<Doctor> = listOf()
    private var selectedDoctorId: Long? = null

    // ID bệnh nhân (Lấy từ Intent hoặc Session)
    private var patientId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_tai_kham_create)

        initView()

        // Lấy ID bệnh nhân truyền từ màn hình trước
        patientId = intent.getLongExtra("patient_id", -1)

        loadDoctors() // Load danh sách bác sĩ
        setEvent()
    }

    private fun initView() {
        tvChonNgay = findViewById(R.id.tvChonNgay)
        tvChonGio = findViewById(R.id.tvChonGio)
        edtGhiChu = findViewById(R.id.edtGhiChu)
        edtBenhAn = findViewById(R.id.edtBenhAn) // Có thể ẩn nếu tự động lấy patientId
        btnSave = findViewById(R.id.btnSave)
        spnBacSi = findViewById(R.id.spnBacSi)
    }

    private fun loadDoctors() {
        lifecycleScope.launch {
            val db = MedicalRecordDatabase.getDatabase(this@screenTaiKham_Create)
            // Giả sử có hàm getAllDoctors, nếu chưa có bạn thêm query "SELECT * FROM doctors" vào DoctorDao
            // Tạm thời dùng query thủ công hoặc hàm có sẵn
            // doctorList = db.doctorDao().getAll() -> Bạn cần check lại DoctorDao xem hàm lấy list tên là gì
            // Dưới đây mình viết demo lấy list, bạn thay bằng hàm DAO thực tế của bạn
            doctorList = withContext(Dispatchers.IO) {
                // Đây là ví dụ, bạn cần đảm bảo Dao có hàm lấy list doctors
                // db.doctorDao().getAllDoctors()
                emptyList() // Placeholder: Hãy thay bằng code gọi DAO thật
            }

            // Nếu bạn chưa viết hàm getAll trong DAO, hãy thêm: @Query("SELECT * FROM doctors") suspend fun getAll(): List<Doctor>

            // Map danh sách bác sĩ ra tên để hiển thị Spinner
            val doctorNames = doctorList.map { it.fullName }
            val adapter = ArrayAdapter(this@screenTaiKham_Create, android.R.layout.simple_spinner_dropdown_item, doctorNames)
            spnBacSi.adapter = adapter

            spnBacSi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
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

        val note = edtGhiChu.text.toString()
        val finalTime = selectedDateCalendar.timeInMillis

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

            // Đặt lịch thông báo (Alarm)
            // Lưu ý: Logic Alarm cũ của bạn có thể cần sửa để nhận timestamp thay vì string
            // ĐOẠN CODE ĐÚNG (Chỉ 3 tham số)
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