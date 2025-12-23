package com.example.projectqlbenhan.ui.home

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.AppointmentWithPatient
import com.example.projectqlbenhan.entity.medicalRecord.DiseaseStat
import com.example.projectqlbenhan.ui.BaseActivity

import com.example.projectqlbenhan.ui.medicalRecord.UpdateMedicalRecord
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
import java.util.concurrent.TimeUnit

class HomeActivity : BaseActivity() {

    override fun getLayoutResId() = R.layout.activity_main

    // Views
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

    // ⭐️ MỚI: View chọn ngày
    private lateinit var btnPickDate: LinearLayout
    private lateinit var tvCurrentDate: TextView

    // ⭐️ MỚI: Biến lưu ngày đang chọn (Mặc định là hôm nay)
    private var selectedCalendar: Calendar = Calendar.getInstance()

    // Adapter
    private lateinit var appointmentAdapter: AppointmentAdapter

    // DAOs (Lazy init)
    private val appointmentDao by lazy { MedicalRecordDatabase.getDatabase(this).appointmentDao() }
    private val patientDao by lazy { MedicalRecordDatabase.getDatabase(this).patientDao() }
    private val medicalRecordDao by lazy { MedicalRecordDatabase.getDatabase(this).medicalRecordDao() }
    // Lưu ý: Nếu bạn đã xóa PrescriptionDao cũ và dùng PrescriptionItemDao thì sửa dòng này lại,
    // còn nếu chưa thì tạm thời comment dòng dưới để tránh lỗi
     private val prescriptionDao by lazy { MedicalRecordDatabase.getDatabase(this).prescriptionItemDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setControl()
        setEvent()

        // Cấp quyền thông báo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    override fun onResume() {
        super.onResume()
        reloadDashboard()
//        startThongBaoTaiKhamWorker()
        ThongBaoTaiKham_LichGanNhat()
    }

    private fun setControl() {
        // Init Views
        btnAdd = findViewById(R.id.btnAdd)
        btnMenu = findViewById(R.id.btnMenu)
        tvTotalPatients = findViewById(R.id.tvTotalPatients)
        tvTotalRecords = findViewById(R.id.tvTotalRecords)
        tvTodayAppointments = findViewById(R.id.tvTodayAppointments)
        tvTotalPrescriptions = findViewById(R.id.tvTotalPrescriptions)
        rcAppointments = findViewById(R.id.rcAppointments)
        layoutEmptyAppointments = findViewById(R.id.layoutEmptyAppointments)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
        pieChart = findViewById(R.id.pieDiseaseChart)


        btnPickDate = findViewById(R.id.btnPickDate)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)


        updateDateDisplay()

        appointmentAdapter = AppointmentAdapter(
            emptyList(),
            onItemClick = { item ->
                if (item.appointment.status == "SCHEDULED" || item.appointment.status == "MISSED") {
//                    val intent = Intent(this, UpdateMedicalRecord::class.java)
//                    // id lich hen de xu ly trang thai
//                    intent.putExtra("patient_id", item.patient.patientId)
//                    // Lưu ý: Appointment mới không có recordId, logic này cần check lại bên UpdateMedicalRecord
//                    // intent.putExtra("record_id", item.appointment.recordId)
//                    intent.putExtra("appointment_id", item.appointment.appointmentId)
//                    startActivity(intent)
                    val intent = Intent(this, UpdateMedicalRecord::class.java)
                    intent.putExtra("patient_id", item.patient.patientId)
                    intent.putExtra("appointment_id", item.appointment.appointmentId)
                    intent.putExtra("record_id", -1L) // Tạo mới
                    startActivity(intent)
                }
            },
            onItemLongClick = { item ->
                showCancelDialog(item)
            }
        )

        rcAppointments.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = appointmentAdapter
        }
    }

    private fun setEvent() {
        // Menu Drawer
        btnMenu.setOnClickListener { openDrawer() }

        // Add Appointment
        btnAdd.setOnClickListener {
            AddQuickAppointmentBottomSheet {
                reloadDashboard()
            }.show(supportFragmentManager, "ADD_APPOINTMENT")
        }

        // ⭐️ Sự kiện mở DatePicker
        btnPickDate.setOnClickListener {
            showDatePicker()
        }

        // Filter Chips
        setupChipFilter()
    }

    // ⭐️ Hàm hiển thị DatePicker
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Cập nhật ngày được chọn
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Reset giờ về 0
                resetCalendarTime(selectedCalendar)

                updateDateDisplay()

                // Bỏ chọn Chip để tránh xung đột logic (đang chọn ngày cụ thể)
                chipGroupFilter.clearCheck()

                // Load lại dữ liệu
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

    private fun reloadDashboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val todayStart = getStartOfToday()
            val endToday = todayStart + (24 * 60 * 60 * 1000) - 1

            // ⭐️ LOGIC MỚI: Check Missed dựa trên Timestamp
            // Lấy các cuộc hẹn hôm nay chưa hoàn thành
            val upcomingList = appointmentDao.getUpcomingAppointments(todayStart) // Giả sử hàm này lấy >= todayStart

            var hasChange = false
            // Buffer 30 phút: Nếu quá giờ hẹn 30p mà chưa khám -> Missed
            val bufferTime = 30 * 60 * 1000

            upcomingList.forEach { appointment ->
                if (appointment.status == "SCHEDULED" &&
                    appointment.appointmentDate < (now - bufferTime)) {

                    appointmentDao.updateStatus(appointment.appointmentId, "MISSED")
                    hasChange = true
                }
            }

            withContext(Dispatchers.Main) {
                if (hasChange) loadStatistics() // Chỉ load lại stats nếu có thay đổi

                loadStatistics()
                loadAppointments()
                loadDiseaseChart()
            }
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            val startOfDay = getStartOfToday()
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

            val countPatientDeferred = withContext(Dispatchers.IO) { patientDao.countPatients() }
            val countRecordDeferred = withContext(Dispatchers.IO) { medicalRecordDao.countMedicalRecords() }
            val appointmentsTodayDeferred = withContext(Dispatchers.IO) {
                appointmentDao.countTodayAppointments(startOfDay, endOfDay)
            }
            // Tạm comment nếu chưa có prescriptionDao mới
            // val countPrescriptionDeferred = withContext(Dispatchers.IO) { prescriptionDao.countPrescriptions() }

            tvTotalPatients.text = countPatientDeferred.toString()
            tvTotalRecords.text = countRecordDeferred.toString()
            tvTodayAppointments.text = appointmentsTodayDeferred.toString()
            // tvTotalPrescriptions.text = if (countPrescriptionDeferred > 0) countPrescriptionDeferred.toString() else "0"
            tvTotalPrescriptions.text = "0" // Placeholder
        }
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            val checkedId = chipGroupFilter.checkedChipId

            // Tính toán khoảng thời gian của ngày đang chọn trong selectedCalendar
            val startOfDay = selectedCalendar.clone() as Calendar
            resetCalendarTime(startOfDay)

            val endOfDay = startOfDay.clone() as Calendar
            endOfDay.add(Calendar.DAY_OF_MONTH, 1)
            endOfDay.add(Calendar.MILLISECOND, -1)

            val dataToShow = withContext(Dispatchers.IO) {
                when (checkedId) {
                    R.id.chipMissed -> {
                        appointmentDao.getAppointmentsByStatus("MISSED")
                    }
                    R.id.chipCancelled -> {
                        appointmentDao.getAppointmentsByStatus("CANCELLED")
                    }
                    R.id.chipToday -> {
                        // Nếu chọn Chip Today -> Load hôm nay
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
                    else -> {
                        // ⭐️ Mặc định (hoặc Chip All/Không chọn chip): Load theo DatePicker
                        appointmentDao.getAppointmentsByDateRange(
                            startOfDay.timeInMillis,
                            endOfDay.timeInMillis
                        )
                    }
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

    private fun setupChipFilter() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            // Logic cập nhật UI ngày tháng khi bấm Chip
            if (checkedIds.contains(R.id.chipToday)) {
                selectedCalendar = Calendar.getInstance()
                updateDateDisplay()
            } else if (checkedIds.contains(R.id.chipTomorrow)) {
                selectedCalendar = Calendar.getInstance()
                selectedCalendar.add(Calendar.DAY_OF_YEAR, 1)
                updateDateDisplay()
            }
            // Load lại dữ liệu
            loadAppointments()
        }
    }

    private fun loadDiseaseChart() {
        lifecycleScope.launch {
            val stats = withContext(Dispatchers.IO) { medicalRecordDao.getDiseaseStats() }
            showDiseasePieChart(stats)
        }
    }

    private fun showDiseasePieChart(stats: List<DiseaseStat>) {
        if (stats.isEmpty()) {
            pieChart.visibility = View.GONE
            return
        }
        pieChart.visibility = View.VISIBLE

        val entries = stats.map { PieEntry(it.total.toFloat(), it.diseaseType) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        pieChart.data = PieData(dataSet)
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
            invalidate()
        }
    }

    // Helper functions
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

    // --- WORKER & NOTIFICATION LOGIC ---
//    private fun startThongBaoTaiKhamWorker() {
//        val workRequest = PeriodicWorkRequestBuilder<Worker_ThongBaoTaiKham>(15, TimeUnit.MINUTES).build()
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            "ThongBaoTaiKham",
//            ExistingPeriodicWorkPolicy.KEEP,
//            workRequest
//        )
//    }

    private fun ThongBaoTaiKham_LichGanNhat() {
        lifecycleScope.launch {
            val startToday = getStartOfToday()
            val endToday = startToday + (24 * 60 * 60 * 1000) - 1

            // Logic lấy thông báo - Cần điều chỉnh lại DAO nếu cần,
            // ở đây tạm thời gọi hàm getAppointmentsByDateRange rồi filter
            val todayAppointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsByDateRange(startToday, endToday)
            }

            if (todayAppointments.isNotEmpty()) {
                // Lấy cái đầu tiên chưa hoàn thành và chưa quá hạn
                val now = System.currentTimeMillis()
                val nearest = todayAppointments.firstOrNull {
                    it.appointment.status == "SCHEDULED" && it.appointment.appointmentDate > now
                }

                if (nearest != null) {
                    if (ActivityCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return@launch
                    }
                    // Cần map AppointmentWithPatient sang Appointment nếu Helper yêu cầu
                    // Helper_ThongBaoTaiKham.notifyNearestIfNeeded(this@HomeActivity, nearest.appointment)
                }
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
                reloadDashboard() // Reload lại toàn bộ
            }
        }
    }
}