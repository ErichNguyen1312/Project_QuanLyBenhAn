package com.example.projectqlbenhan.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.example.projectqlbenhan.ui.ThongBaoTaiKham.Helper_ThongBaoTaiKham
import com.example.projectqlbenhan.ui.ThongBaoTaiKham.Worker_ThongBaoTaiKham
import com.example.projectqlbenhan.utils.DateTimeUtils
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
import java.util.Date
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

    // Data & Adapter
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var fullList: List<AppointmentWithPatient> = listOf()

    // DAOs (Lazy init)
    private val appointmentDao by lazy { MedicalRecordDatabase.getDatabase(this).appointmentDao() }
    private val patientDao by lazy { MedicalRecordDatabase.getDatabase(this).patientDao() }
    private val medicalRecordDao by lazy {
        MedicalRecordDatabase.getDatabase(this).medicalRecordDao()
    }
    private val prescriptionDao by lazy {
        MedicalRecordDatabase.getDatabase(this).prescriptionDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setControl()
        setEvent()

        //Cáp quyền thông báo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

    }

    override fun onResume() {
        super.onResume()
        reloadDashboard()

        //Chạy hàm thông báo - Trí
        //LichThongBaoTaiKham()
        startThongBaoTaiKhamWorker()
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


        appointmentAdapter = AppointmentAdapter(
            emptyList(),
            onItemClick = { item ->
                if (item.appointment.status == "SCHEDULED" || item.appointment.status == "MISSED") {
                    val intent = Intent(
                        this,
                        UpdateMedicalRecord::class.java
                    )
                    // id lich hen de xu ly trang thai
                    intent.putExtra("patient_id", item.patient.patientId)
                    intent.putExtra(
                        "appointment_id",
                        item.appointment.appointmentId
                    )
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
        btnMenu.setOnClickListener {
            openDrawer()
        }

        // Add Appointment
        btnAdd.setOnClickListener {
            AddQuickAppointmentBottomSheet {

                reloadDashboard()
            }.show(supportFragmentManager, "ADD_APPOINTMENT")
        }

        // Filter
        setupChipFilter()
    }

    private fun reloadDashboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            val today = getStartOfToday()

            //kiem benh nhan missed
            appointmentDao.markPastAppointmentsAsMissed(today)

            val upcomingList = appointmentDao.getUpcomingAppointments(today)


            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)


            val todayList = upcomingList.filter { isSameDay(it.appointmentDate, calendar) }

            var hasChange = false
            todayList.forEach { appointment ->
                if (isTimePassed(appointment.appointmentTime, currentHour, currentMinute)) {
                    appointmentDao.updateStatus(appointment.appointmentId, "MISSED")
                    hasChange = true
                }
            }


            withContext(Dispatchers.Main) {

                if (hasChange) loadStatistics()

                loadStatistics()
                loadAppointments()
                loadDiseaseChart()
            }
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {

            //tinh thoi gian trong ngay de hien thi
            val startOfDay = getStartOfToday()
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

            val countPatientDeferred = withContext(Dispatchers.IO) { patientDao.countPatients() }
            val countRecordDeferred =
                withContext(Dispatchers.IO) { medicalRecordDao.countMedicalRecords() }

            val appointmentsTodayDeferred = withContext(Dispatchers.IO) {
                appointmentDao.countTodayAppointments(startOfDay, endOfDay)
            }

            val countPrescriptionDeferred =
                withContext(Dispatchers.IO) { prescriptionDao.countPrescriptions() }


            tvTotalPatients.text = countPatientDeferred.toString()
            tvTotalRecords.text = countRecordDeferred.toString()
            tvTodayAppointments.text = appointmentsTodayDeferred.toString()
            tvTotalPrescriptions.text =
                if (countPrescriptionDeferred > 0) countPrescriptionDeferred.toString() else "0"
        }
    }


    private fun isTimePassed(
        appointmentTimeStr: String,
        currentHour: Int,
        currentMinute: Int
    ): Boolean {
        return try {
            val parts = appointmentTimeStr.split(":")
            val apptHour = parts[0].toInt()
            val apptMinute = parts[1].toInt()

            // Quy đổi ra phút
            val apptTotalMinutes = apptHour * 60 + apptMinute
            val currentTotalMinutes = currentHour * 60 + currentMinute

            // Logic: Nếu phút hiện tại > (phút hẹn + 30) thì là MISSED
            return currentTotalMinutes > (apptTotalMinutes + 30)
        } catch (e: Exception) {
            false
        }
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            val checkedId = findViewById<ChipGroup>(R.id.chipGroupFilter).checkedChipId
            val today = getStartOfToday()

            //chip status
            val dataToShow = withContext(Dispatchers.IO) {
                when (checkedId) {
                    R.id.chipMissed -> {
                        appointmentDao.getAppointmentsByStatus("MISSED")
                    }

                    R.id.chipCancelled -> {
                        appointmentDao.getAppointmentsByStatus("CANCELLED")
                    }

                    else -> {
                        //kiem tra lich hen hien tai
                        val upcoming =
                            appointmentDao.getUpcomingAppointmentsWithPatientSorting(today)

                        // loc benh nha theo ngay
                        when (checkedId) {
                            R.id.chipToday -> {
                                val todayCal = Calendar.getInstance()
                                upcoming.filter {
                                    isSameDay(
                                        it.appointment.appointmentDate,
                                        todayCal
                                    )
                                }
                            }

                            R.id.chipTomorrow -> {
                                val tomorrowCal = Calendar.getInstance()
                                tomorrowCal.add(Calendar.DAY_OF_YEAR, 1)
                                upcoming.filter {
                                    isSameDay(
                                        it.appointment.appointmentDate,
                                        tomorrowCal
                                    )
                                }
                            }

                            else -> upcoming
                        }
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
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFilter)
        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            loadAppointments()
        }
    }


    private fun filterList(type: String) {
        val filteredList = when (type) {
            "TODAY" -> {
                val todayCal = Calendar.getInstance()
                fullList.filter { isSameDay(it.appointment.appointmentDate, todayCal) }
            }

            "TOMORROW" -> {
                val tomorrowCal = Calendar.getInstance()
                tomorrowCal.add(Calendar.DAY_OF_YEAR, 1)
                fullList.filter { isSameDay(it.appointment.appointmentDate, tomorrowCal) }
            }

            else -> fullList // "ALL"
        }

        updateRecyclerUI(filteredList)
    }

    private fun updateRecyclerUI(data: List<AppointmentWithPatient>) {
        if (data.isEmpty()) {
            layoutEmptyAppointments.visibility = View.VISIBLE
            rcAppointments.visibility = View.GONE
        } else {
            layoutEmptyAppointments.visibility = View.GONE
            rcAppointments.visibility = View.VISIBLE
            appointmentAdapter.submitList(data)
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


    private fun isSameDay(dateMillis: Long, calendarCompare: Calendar): Boolean {
        val dateCal = Calendar.getInstance()
        dateCal.timeInMillis = dateMillis
        return dateCal.get(Calendar.YEAR) == calendarCompare.get(Calendar.YEAR) &&
                dateCal.get(Calendar.DAY_OF_YEAR) == calendarCompare.get(Calendar.DAY_OF_YEAR)
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    //Các hàm cho chức năng thông báo tái khám - Trí

    /*
    private fun LichThongBaoTaiKham() {
        val workRequest =
            PeriodicWorkRequestBuilder<Worker_ThongBaoTaiKham>(
                2, TimeUnit.HOURS
            ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "ThongBaoTaiKham",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
    */

    private fun startThongBaoTaiKhamWorker() {

        val workRequest =
            PeriodicWorkRequestBuilder<Worker_ThongBaoTaiKham>(
                15, TimeUnit.MINUTES
            ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "ThongBaoTaiKham",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    private fun ThongBaoTaiKham_LichGanNhat() {
        lifecycleScope.launch {


            val startToday = DateTimeUtils.getStartOfDay()
            val endToday = DateTimeUtils.getEndOfDay()

            val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date())

            val todayAppointments =
                appointmentDao.getTodayUpcomingAppointments(
                    startToday,
                    endToday,
                    nowTime
                )

            if (todayAppointments.isNotEmpty()) {
                val nearest = todayAppointments.first()
                if (ActivityCompat.checkSelfPermission(
                        this@HomeActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@launch
                }
                Helper_ThongBaoTaiKham.notifyNearestIfNeeded(
                    this@HomeActivity,
                    nearest
                )
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
            // Update trạng thái thành CANCELLED
            appointmentDao.updateStatus(appointmentId, "CANCELLED")

            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    this@HomeActivity,
                    "Đã hủy lịch hẹn",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                loadAppointments() // Load lại danh sách ngay
            }
        }
    }
}