package com.example.projectqlbenhan.ui.home

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.drawerlayout.widget.DrawerLayout
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.appointment.AppointmentDao
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.dao.medicalRecord.MedicalRecordDao
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao
import com.example.projectqlbenhan.entity.appointment.AppointmentWithPatient
import com.example.projectqlbenhan.ui.BaseActivity
import com.example.projectqlbenhan.ui.medicalRecord.UpdateMedicalRecord
import com.example.projectqlbenhan.ui.DonThuocUI.DonThuoc
import com.example.projectqlbenhan.ui.DonThuocUI.DanhSachDonThuoc
import com.example.projectqlbenhan.ui.patient.Patients
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

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

    private val db: MedicalRecordDatabase by lazy { MedicalRecordDatabase.getDatabase(this) }
    private val appointmentDao: AppointmentDao by lazy { db.appointmentDao() }
    private val patientDao: PatientDao by lazy { db.patientDao() }
    private val medicalRecordDao: MedicalRecordDao by lazy { db.medicalRecordDao() }
    private val prescriptionDao: PrescriptionItemDao by lazy { db.prescriptionItemDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setControl()
        setEvent()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    /**
     * ⭐ LUỒNG 2: Điều hướng từ Menu Drawer
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_listPatient -> {
                startActivity(Intent(this, Patients::class.java))
            }
            R.id.menu_quanLyDonThuoc -> {
                // Chuyển sang Dashboard Đơn Thuốc con (file XML don_thuoc.xml)
                startActivity(Intent(this, DonThuoc::class.java))
            }
            R.id.menu_logout -> finish()
        }
        findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawers()
        return true
    }

    override fun openDrawer() {
        findViewById<DrawerLayout>(R.id.drawer_layout)?.open()
    }

    override fun onResume() {
        super.onResume()
        reloadDashboard()
    }

    private fun setControl() {
        btnMenu = findViewById(R.id.btnMenu)
        btnAdd = findViewById(R.id.btnAdd)
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

        appointmentAdapter = AppointmentAdapter(emptyList(), onItemClick = { item ->
            val intent = Intent(this, UpdateMedicalRecord::class.java).apply {
                putExtra("patient_id", item.patient.patientId)
                putExtra("appointment_id", item.appointment.appointmentId)
            }
            startActivity(intent)
        }, onItemLongClick = { item -> showCancelDialog(item) })

        rcAppointments.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = appointmentAdapter
        }
    }

    private fun setEvent() {
        btnMenu.setOnClickListener { openDrawer() }

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView?.setNavigationItemSelectedListener(this)

        btnPickDate.setOnClickListener { showDatePicker() }
        chipGroupFilter.setOnCheckedStateChangeListener { _, _ -> loadAppointments() }

        /**
         * ⭐ LUỒNG 1: Click Card Đơn thuốc ngoài Dashboard chính
         * Chuyển thẳng sang Danh sách đơn thuốc
         */
        findViewById<View>(R.id.cardPrescriptions)?.setOnClickListener {
            startActivity(Intent(this, DanhSachDonThuoc::class.java))
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, Patients::class.java))
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch(Dispatchers.IO) {
            val countPatient = patientDao.countPatients()
            val countRecord = medicalRecordDao.countMedicalRecords()
            val allMedicines = prescriptionDao.getAllPrescriptionItems()

            withContext(Dispatchers.Main) {
                tvTotalPatients.text = countPatient.toString()
                tvTotalRecords.text = countRecord.toString()
                tvTotalPrescriptions.text = allMedicines.size.toString()
            }
        }
    }

    private fun loadAppointments() {
        lifecycleScope.launch(Dispatchers.IO) {
            val checkedId = chipGroupFilter.checkedChipId
            val startOfDay = selectedCalendar.timeInMillis
            val dataToShow = when (checkedId) {
                R.id.chipMissed -> appointmentDao.getAppointmentsByStatus("MISSED")
                R.id.chipCancelled -> appointmentDao.getAppointmentsByStatus("CANCELLED")
                else -> appointmentDao.getAppointmentsByDateRange(startOfDay, startOfDay + 86400000)
            }
            withContext(Dispatchers.Main) {
                appointmentAdapter.submitList(dataToShow)
                layoutEmptyAppointments.visibility = if (dataToShow.isEmpty()) View.VISIBLE else View.GONE
                tvTodayAppointments.text = dataToShow.size.toString()
            }
        }
    }

    private fun reloadDashboard() {
        loadStatistics()
        loadAppointments()
        loadDiseaseChart()
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            selectedCalendar.set(year, month, day)
            updateDateDisplay()
            reloadDashboard()
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvCurrentDate.text = sdf.format(selectedCalendar.time)
    }

    private fun loadDiseaseChart() {
        lifecycleScope.launch(Dispatchers.IO) {
            val stats = medicalRecordDao.getDiseaseStats()
            withContext(Dispatchers.Main) {
                if (stats.isNotEmpty()) {
                    val entries = stats.map { PieEntry(it.total.toFloat(), it.diseaseType) }
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                    }
                    pieChart.data = PieData(dataSet)
                    pieChart.animateY(1000)
                    pieChart.invalidate()
                }
            }
        }
    }

    private fun showCancelDialog(item: AppointmentWithPatient) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận")
            .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này?")
            .setPositiveButton("Hủy lịch") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    appointmentDao.updateStatus(item.appointment.appointmentId, "CANCELLED")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeActivity, "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show()
                        reloadDashboard()
                    }
                }
            }
            .setNegativeButton("Đóng", null)
            .show()
    }
}