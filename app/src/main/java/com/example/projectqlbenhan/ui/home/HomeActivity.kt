package com.example.projectqlbenhan.ui.home

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.AppointmentWithPatient
import com.example.projectqlbenhan.entity.medicalRecord.DiseaseStat
import com.example.projectqlbenhan.ui.BaseActivity
import com.example.projectqlbenhan.ui.medicalRecord.UpdateMedicalRecord
import com.example.projectqlbenhan.utils.SessionManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : BaseActivity() {

    override fun getLayoutResId() = R.layout.activity_main

    private lateinit var tvTotalPatients: TextView
    private lateinit var tvTotalRecords: TextView
    private lateinit var tvTodayAppointments: TextView
    private lateinit var tvTotalPrescriptions: TextView
    private lateinit var rcAppointments: RecyclerView
    private lateinit var layoutEmptyAppointments: LinearLayout
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var btnMenu: ImageView
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var pieChart: PieChart
    private lateinit var btnPickDate: LinearLayout
    private lateinit var tvCurrentDate: TextView

    private var selectedCalendar: Calendar = Calendar.getInstance()
    private lateinit var appointmentAdapter: AppointmentAdapter

    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }
    private val appointmentDao by lazy { db.appointmentDao() }
    private val patientDao by lazy { db.patientDao() }
    private val medicalRecordDao by lazy { db.medicalRecordDao() }
    private val prescriptionItemDao by lazy { db.prescriptionItemDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setControl()

        //Kiểm tra role - Trí
        checkRole()

        setEvent()
        checkNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        reloadDashboard()
//         checkUpcomingAppointments()
    }

    private fun setControl() {
        // Ánh xạ
        tvTotalPatients = findViewById(R.id.tvTotalPatients)
        tvTotalRecords = findViewById(R.id.tvTotalRecords)
        tvTodayAppointments = findViewById(R.id.tvTodayAppointments)
        tvTotalPrescriptions = findViewById(R.id.tvTotalPrescriptions)
        rcAppointments = findViewById(R.id.rcAppointments)
        layoutEmptyAppointments = findViewById(R.id.layoutEmptyAppointments)
        btnAdd = findViewById(R.id.btnAdd)
        btnMenu = findViewById(R.id.btnMenu)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
        pieChart = findViewById(R.id.pieDiseaseChart)
        btnPickDate = findViewById(R.id.btnPickDate)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)

        setupRecyclerView()

        setupPieChartConfig()

        updateDateDisplay()
    }

    private fun setEvent() {
        btnMenu.setOnClickListener { openDrawer() }
        btnAdd.setOnClickListener {
            AddQuickAppointmentBottomSheet { reloadDashboard() }
                .show(supportFragmentManager, "ADD_APPOINTMENT")
        }

        // Chọn ngày
        btnPickDate.setOnClickListener { showDatePicker() }

        // Bộ lọc Chips
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(R.id.chipToday)) {
                selectedCalendar = Calendar.getInstance()
                updateDateDisplay()
            } else if (checkedIds.contains(R.id.chipTomorrow)) {
                selectedCalendar = Calendar.getInstance()
                selectedCalendar.add(Calendar.DAY_OF_YEAR, 1)
                updateDateDisplay()
            }
            loadAppointments()
        }
    }


    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter(
            emptyList(),
            onItemClick = { item -> handleAppointmentClick(item) },
            onItemLongClick = { item -> showCancelDialog(item) }
        )
        rcAppointments.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = appointmentAdapter
        }
    }

    private fun setupPieChartConfig() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 55f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }

    private fun handleAppointmentClick(item: AppointmentWithPatient) {
        val currentDoctorId = SessionManager.getSpecificId(this)

        // Nếu ID bác sĩ trong lịch KHÁC với ID bác sĩ đang đăng nhập -> Chặn luôn
        if (item.appointment.doctorId != currentDoctorId) {
            Toast.makeText(this, "Bạn không phụ trách ca khám này!", Toast.LENGTH_SHORT).show()
            return // Dừng lại, không chạy code bên dưới
        }
        if (item.appointment.status == "SCHEDULED" || item.appointment.status == "MISSED") {
            val intent = Intent(this, UpdateMedicalRecord::class.java)
            intent.putExtra("patient_id", item.patient.patientId)
            intent.putExtra("appointment_id", item.appointment.appointmentId)
            intent.putExtra("record_id", -1L)
            startActivity(intent)
        } else {
            // (Optional) Toast báo nếu trạng thái đã hoàn thành/hủy
            Toast.makeText(this, "Lịch hẹn này đã kết thúc hoặc bị hủy.", Toast.LENGTH_SHORT).show()
        }
    }

    // chọn ngày hiển thị lịch hẹn của bác sĩ

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(year, month, dayOfMonth)
                resetCalendarTime(selectedCalendar)
                updateDateDisplay()
                chipGroupFilter.clearCheck() // Bỏ chọn chip để tránh conflict
                reloadDashboard()
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = Calendar.getInstance()
        if (isSameDay(selectedCalendar.timeInMillis, today)) {
            tvCurrentDate.text = "Hôm nay (${sdf.format(selectedCalendar.time)})"
        } else {
            tvCurrentDate.text = sdf.format(selectedCalendar.time)
        }
    }

    //load dashboard

    private fun reloadDashboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            checkMissedAppointments() // Cập nhật trạng thái lỡ hẹn

            withContext(Dispatchers.Main) {
                loadStatistics()
                loadAppointments()
                loadDiseaseChart()
            }
        }
    }

    private suspend fun checkMissedAppointments() {
        val now = System.currentTimeMillis()
        val todayStart = getStartOfToday()
        val upcomingList = appointmentDao.getUpcomingAppointments(todayStart)
        val bufferTime = 30 * 60 * 1000 // 30 phút

        var hasChange = false
        upcomingList.forEach { appointment ->
            if (appointment.status == "SCHEDULED" &&
                appointment.appointmentDate < (now - bufferTime)
            ) {
                appointmentDao.updateStatus(appointment.appointmentId, "MISSED")
                hasChange = true
            }
        }
    }

    //load thống kê
    private fun loadStatistics() {
        lifecycleScope.launch {
            val startOfDay = getStartOfToday()
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

            val countPatient = withContext(Dispatchers.IO) { patientDao.countPatients() }
            val countRecord = withContext(Dispatchers.IO) { medicalRecordDao.countMedicalRecords() }
            val todayAppt = withContext(Dispatchers.IO) {
                appointmentDao.countTodayAppointments(startOfDay, endOfDay)
            }
            val countPrescription = withContext(Dispatchers.IO) {
                prescriptionItemDao.countTotalPrescriptions()
            }
            tvTotalPatients.text = countPatient.toString()
            tvTotalRecords.text = countRecord.toString()
            tvTodayAppointments.text = todayAppt.toString()
            tvTotalPrescriptions.text = countPrescription.toString()
        }
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            val checkedId = chipGroupFilter.checkedChipId

            // Tính range ngày dựa trên selectedCalendar
            val startOfDay = selectedCalendar.clone() as Calendar
            resetCalendarTime(startOfDay)
            val endOfDay = startOfDay.clone() as Calendar
            endOfDay.add(Calendar.DAY_OF_MONTH, 1)
            endOfDay.add(Calendar.MILLISECOND, -1)

            val dataToShow = withContext(Dispatchers.IO) {
                when (checkedId) {
                    R.id.chipMissed -> appointmentDao.getAppointmentsByStatus("MISSED")
                    R.id.chipCancelled -> appointmentDao.getAppointmentsByStatus("CANCELLED")
                    R.id.chipToday -> {
                        val todayStart = getStartOfToday()
                        val todayEnd = todayStart + (24 * 60 * 60 * 1000) - 1
                        appointmentDao.getAppointmentsByDateRange(todayStart, todayEnd)
                    }

                    R.id.chipTomorrow -> {
                        // Nếu chọn Chip Tomorrow -> Load ngày mai
                        val tmrStart = getStartOfToday() + (24 * 60 * 60 * 1000)
                        val tmrEnd = tmrStart + (24 * 60 * 60 * 1000) - 1
                        appointmentDao.getAppointmentsByDateRange(tmrStart, tmrEnd)
                    }

                    else -> appointmentDao.getAppointmentsByDateRange(
                        startOfDay.timeInMillis,
                        endOfDay.timeInMillis
                    )
                }
            }

            if (dataToShow.isEmpty()) {
                layoutEmptyAppointments.visibility = View.VISIBLE
                rcAppointments.visibility = View.GONE
            } else {
                layoutEmptyAppointments.visibility = View.GONE
                rcAppointments.visibility = View.VISIBLE
                appointmentAdapter.submitList(dataToShow)
            }
        }
    }

    private fun loadDiseaseChart() {
        lifecycleScope.launch {
            val stats = withContext(Dispatchers.IO) { medicalRecordDao.getDiseaseStats() }
            if (stats.isEmpty()) {
                pieChart.visibility = View.GONE
            } else {
                pieChart.visibility = View.VISIBLE
                val entries = stats.map { PieEntry(it.total.toFloat(), it.diseaseType) }
                val dataSet = PieDataSet(entries, "").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    valueTextSize = 12f
                    valueTextColor = Color.WHITE
                }
                pieChart.data = PieData(dataSet)
                pieChart.invalidate() // Vẽ lại
            }
        }
    }


    private fun showCancelDialog(item: AppointmentWithPatient) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Hủy lịch hẹn")
            .setMessage("Bạn muốn hủy lịch hẹn với ${item.patient.fullName}?")
            .setPositiveButton("Hủy lịch") { _, _ ->
                cancelAppointment(item.appointment.appointmentId)
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun cancelAppointment(appointmentId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            appointmentDao.updateStatus(appointmentId, "CANCELLED")
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeActivity, "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show()
                reloadDashboard()
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun isSameDay(dateMillis: Long, calendarCompare: Calendar): Boolean {
        val dateCal = Calendar.getInstance()
        dateCal.timeInMillis = dateMillis
        return dateCal.get(Calendar.YEAR) == calendarCompare.get(Calendar.YEAR) &&
                dateCal.get(Calendar.DAY_OF_YEAR) == calendarCompare.get(Calendar.DAY_OF_YEAR)
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        resetCalendarTime(cal)
        return cal.timeInMillis
    }

    private fun resetCalendarTime(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }


    private fun ThongBaoTaiKham_LichGanNhat() {
        lifecycleScope.launch {
            val startToday = getStartOfToday()
            val endToday = startToday + (24 * 60 * 60 * 1000) - 1

            val todayAppointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsByDateRange(startToday, endToday)
            }

            if (todayAppointments.isNotEmpty()) {
                val now = System.currentTimeMillis()
                val nearest = todayAppointments.firstOrNull {
                    it.appointment.status == "SCHEDULED" && it.appointment.appointmentDate > now
                }

                if (nearest != null) {
                    if (ActivityCompat.checkSelfPermission(
                            this@HomeActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@launch
                    }

                }
            }
        }
    }


    //Hàm kiểm tra role để làm quản lí bác sĩ - Trí
    private fun checkRole() {
        val role = SessionManager.getRole(this)
        val menu = navigationView.menu
        if (role != "ADMIN") {
            menu.findItem(R.id.menu_BacSi).isVisible = false
        }
    }
}