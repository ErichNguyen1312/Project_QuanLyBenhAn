package com.example.projectqlbenhan.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.AppointmentWithPatient
import com.example.projectqlbenhan.entity.medicalRecord.DiseaseStat
import com.example.projectqlbenhan.ui.BaseActivity
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
import java.util.Calendar

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
    private val medicalRecordDao by lazy { MedicalRecordDatabase.getDatabase(this).medicalRecordDao() }
    private val prescriptionDao by lazy { MedicalRecordDatabase.getDatabase(this).prescriptionDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setControl()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        // Load lại dữ liệu khi quay lại màn hình này
        reloadDashboard()
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

        // Setup RecyclerView
        appointmentAdapter = AppointmentAdapter(emptyList())
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
                // Callback khi thêm thành công -> Reload lại dashboard
                // Tuy nhiên onResume sẽ tự chạy khi đóng dialog nên dòng này có thể thừa,
                // nhưng giữ lại cũng an toàn.
                reloadDashboard()
            }.show(supportFragmentManager, "ADD_APPOINTMENT")
        }

        // Filter Logic
        setupChipFilter()
    }

    // ================== DATA LOADING ==================

    private fun reloadDashboard() {
        loadStatistics()
        loadAppointments()
        loadDiseaseChart()
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            // Chạy song song các query count để tối ưu thời gian
            val countPatientDeferred = withContext(Dispatchers.IO) { patientDao.countPatients() }
            val countRecordDeferred = withContext(Dispatchers.IO) { medicalRecordDao.countMedicalRecords() }
            val appointmentsTodayDeferred = withContext(Dispatchers.IO) { appointmentDao.countTodayAppointments() }
            val countPrescriptionDeferred = withContext(Dispatchers.IO) { prescriptionDao.countPrescriptions() }

            // Update UI
            tvTotalPatients.text = countPatientDeferred.toString()
            tvTotalRecords.text = countRecordDeferred.toString()
            tvTodayAppointments.text = appointmentsTodayDeferred.toString()
            tvTotalPrescriptions.text = if (countPrescriptionDeferred > 0) countPrescriptionDeferred.toString() else "0"
        }
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            val today = getStartOfToday()

            // 1. Lấy dữ liệu thô
            val rawData = withContext(Dispatchers.IO) {
                appointmentDao.getUpcomingAppointmentsWithPatientSorting(today)
            }

            // 2. Sắp xếp: Ngày tăng dần -> Giờ tăng dần (Dùng chuỗi String sort là đủ)
            fullList = rawData.sortedWith(
                compareBy<AppointmentWithPatient> { it.appointment.appointmentDate }
                    .thenBy { it.appointment.appointmentTime }
            )

            // 3. Kiểm tra xem Chip nào đang được chọn để lọc đúng trạng thái
            val currentCheckedId = chipGroupFilter.checkedChipId
            when (currentCheckedId) {
                R.id.chipToday -> filterList("TODAY")
                R.id.chipTomorrow -> filterList("TOMORROW")
                else -> filterList("ALL") // Mặc định hoặc R.id.chipAll
            }
        }
    }

    // ================== FILTER LOGIC ==================

    private fun setupChipFilter() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipAll -> filterList("ALL")
                    R.id.chipToday -> filterList("TODAY")
                    R.id.chipTomorrow -> filterList("TOMORROW")
                }
            }
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

    // ================== CHART LOGIC ==================

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
        pieChart.visibility = View.VISIBLE // Đảm bảo hiện nếu có dữ liệu

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
            invalidate() // Refresh chart
        }
    }

    // ================== UTILS ==================

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
}