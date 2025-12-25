package com.example.projectqlbenhan.ui.patient.booking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.doctor.DoctorWithRating
import com.example.projectqlbenhan.ui.home.BookingDate
import com.example.projectqlbenhan.ui.home.DateAdapter
import com.example.projectqlbenhan.ui.home.DoctorAdapter
import com.example.projectqlbenhan.ui.home.TimeSlot
import com.example.projectqlbenhan.ui.home.TimeSlotAdapter
import com.example.projectqlbenhan.ui.patient_home.PatientHomeActivity
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Booking : AppCompatActivity() {


    private lateinit var rcDoctors: RecyclerView
    private lateinit var rcDates: RecyclerView
    private lateinit var rcTimeSlots: RecyclerView
    private lateinit var edtReason: EditText
    private lateinit var btnBook: Button
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnBack: ImageView
    private lateinit var edtSearchDoctor: EditText

    private var selectedDoctor: Doctor? = null
    private var selectedDate: BookingDate? = null
    private var selectedTimeSlot: TimeSlot? = null

    private var originalDoctorList: List<DoctorWithRating> = emptyList()

    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var dateAdapter: DateAdapter
    private lateinit var timeSlotAdapter: TimeSlotAdapter


    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        setControl()
        loadData()
        setEvent()
    }

    private fun setControl() {
        rcDoctors = findViewById(R.id.rcDoctors)
        rcDates = findViewById(R.id.rcDates)
        rcTimeSlots = findViewById(R.id.rcTimeSlots)
        edtReason = findViewById(R.id.edtReason)
        btnBook = findViewById(R.id.btnBookAppointment)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        btnBack = findViewById(R.id.btnBack)
        edtSearchDoctor = findViewById(R.id.edtSearchDoctor)
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {

            val listData = try {
                db.doctorDao().getDoctorsWithRating()
            } catch (e: Exception) {
                emptyList()
            }

            withContext(Dispatchers.Main) {

                originalDoctorList = listData
                setupDoctorList(listData)
                if (listData.isNotEmpty()) {
                    selectedDoctor = listData[0].doctor
                }

                val dates = generateNext14Days()
                setupDateList(dates)

                if (dates.isNotEmpty()) {
                    selectedDate = dates[0]
                    tvCurrentMonth.text = SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.ENGLISH
                    ).format(selectedDate!!.fullDate)
                }

                loadAvailableTimeSlots()
            }
        }
    }

    private fun setEvent() {
        // Nút Back
        btnBack.setOnClickListener {
            startActivity(Intent(this, PatientHomeActivity::class.java))
            finish()
        }

        btnBook.setOnClickListener {
            handleBooking()
        }

        edtSearchDoctor.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val keyword = s.toString().trim()
                filterDoctors(keyword)
            }
        })
    }

    // --- LOGIC SEARCH THÔNG MINH ---
    private fun filterDoctors(keyword: String) {
        if (originalDoctorList.isEmpty()) return

        val filteredList = originalDoctorList.filter {
            // Tìm theo Tên HOẶC Chuyên khoa (Không phân biệt hoa thường)
            it.doctor.fullName.contains(keyword, ignoreCase = true)
        }

        setupDoctorList(filteredList)

        // Nếu có kết quả lọc, tự chọn người đầu tiên để UX mượt hơn
        if (filteredList.isNotEmpty()) {
            selectedDoctor = filteredList[0].doctor
            loadAvailableTimeSlots() // Load lại giờ theo bác sĩ mới
        }
    }

    // --- LOGIC ĐẶT LỊCH (VALIDATE KỸ) ---
    private fun handleBooking() {
        // 1. Validate Bác sĩ
        if (selectedDoctor == null) {
            Toast.makeText(this, "Vui lòng chọn bác sĩ!", Toast.LENGTH_SHORT).show()
            return
        }
        // 2. Validate Giờ
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Vui lòng chọn giờ khám!", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Validate Lý do (Quan trọng)
        val reason = edtReason.text.toString().trim()
        if (reason.isEmpty()) {
            edtReason.error = "Vui lòng nhập lý do khám"
            edtReason.requestFocus()
            return
        }
        if (reason.length < 10) {
            edtReason.error = "Mô tả ít nhất 10 ký tự để bác sĩ nắm rõ tình hình"
            edtReason.requestFocus()
            return
        }

        // 4. Validate Session
        val patientId = SessionManager.getSpecificId(this)
        if (patientId == -1L || patientId == 0L) {
            Toast.makeText(this, "Phiên đăng nhập lỗi. Vui lòng đăng nhập lại", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val finalTime = convertToMillis(selectedDate!!.fullDate, selectedTimeSlot!!.time)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val newAppt = Appointment(
                    patientId = patientId,
                    doctorId = selectedDoctor!!.doctorId,
                    appointmentDate = finalTime,
                    reason = reason,
                    status = "SCHEDULED"
                )
                db.appointmentDao().insert(newAppt)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Booking, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show()
                    finish() // Quay về Home
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Booking, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- RECYCLERVIEW SETUP HELPERS ---

    private fun setupDoctorList(doctors: List<DoctorWithRating>) {
        doctorAdapter = DoctorAdapter(doctors) { doctor ->
            selectedDoctor = doctor
            loadAvailableTimeSlots()
        }
        rcDoctors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rcDoctors.adapter = doctorAdapter
    }

    private fun setupDateList(dates: List<BookingDate>) {
        dateAdapter = DateAdapter(dates) { date ->
            selectedDate = date
            tvCurrentMonth.text =
                SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(date.fullDate)
            loadAvailableTimeSlots()
        }
        rcDates.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rcDates.adapter = dateAdapter
    }

    private fun setupTimeSlotList(slots: List<TimeSlot>) {
        timeSlotAdapter = TimeSlotAdapter(slots) { slot ->
            selectedTimeSlot = slot
        }
        rcTimeSlots.layoutManager = GridLayoutManager(this, 3)
        rcTimeSlots.adapter = timeSlotAdapter
    }

    private fun loadAvailableTimeSlots() {
        if (selectedDoctor == null || selectedDate == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val startOfDay = selectedDate!!.fullDate
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

            val bookedApps = db.appointmentDao().getAppointmentsByDoctorAndDate(
                selectedDoctor!!.doctorId, startOfDay, endOfDay
            )

            val allSlots = generateTimeSlots(selectedDate!!.fullDate)
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)

            allSlots.forEach { slot ->
                val isBooked = bookedApps.any { app ->
                    val appTime = sdf.format(app.appointmentDate)
                    appTime == slot.time
                }
                slot.isAvailable = !isBooked
                slot.isSelected = false
            }

            withContext(Dispatchers.Main) {
                setupTimeSlotList(allSlots)
            }
        }
    }

    // --- UTILS GENERATOR ---

    private fun generateNext14Days(): List<BookingDate> {
        val list = mutableListOf<BookingDate>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val sdfDay = SimpleDateFormat("EEE", Locale.ENGLISH)
        val sdfDate = SimpleDateFormat("dd", Locale.ENGLISH)

        for (i in 0..13) {
            list.add(
                BookingDate(
                    dayNumber = sdfDate.format(cal.time),
                    dayOfWeek = sdfDay.format(cal.time),
                    fullDate = cal.timeInMillis
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun generateTimeSlots(dateMillis: Long): List<TimeSlot> {
        val list = mutableListOf<TimeSlot>()
        val startHour = 8
        val endHour = 17

        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val sdf = SimpleDateFormat("hh:mm a", Locale.US)

        for (hour in startHour until endHour) {
            cal.set(Calendar.HOUR_OF_DAY, hour)

            // Slot chẵn (00)
            cal.set(Calendar.MINUTE, 0)
            list.add(TimeSlot(sdf.format(cal.time)))

            // Slot lẻ (30)
            cal.set(Calendar.MINUTE, 30)
            list.add(TimeSlot(sdf.format(cal.time)))
        }
        return list
    }

    private fun convertToMillis(dateMillis: Long, timeString: String): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis

        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val timeDate = sdf.parse(timeString)

            if (timeDate != null) {
                val calTime = Calendar.getInstance()
                calTime.time = timeDate

                cal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal.timeInMillis
    }
}