package com.example.projectqlbenhan.ui.home

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
import com.example.projectqlbenhan.ui.BaseActivity
import com.example.projectqlbenhan.utils.DatabaseSeeder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HomeActivity : BaseActivity() {

    override fun getLayoutResId() = R.layout.activity_main

    //    override fun getToolbarTitle() = "DashBoard"
    private lateinit var tvTotalPatients: TextView
    private lateinit var tvTotalRecords: TextView
    private lateinit var tvTodayAppointments: TextView
    private lateinit var tvTotalPrescriptions: TextView
    private lateinit var rcAppointments: RecyclerView
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var layoutEmptyAppointments: LinearLayout

    private lateinit var btnAdd: FloatingActionButton

    private lateinit var btnMenu: ImageView

    private val appointmentDao by lazy {
        MedicalRecordDatabase.getDatabase(this).appointmentDao()
    }
    private val patientDao by lazy {
        MedicalRecordDatabase.getDatabase(this).patientDao()
    }
    private val medicalRecordDao by lazy {
        MedicalRecordDatabase.getDatabase(this).medicalRecordDao()
    }

    private val prescriptionDao by lazy {
        MedicalRecordDatabase.getDatabase(this).prescriptionDao()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        setControl()
        setEvent()
        lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(
                MedicalRecordDatabase.getDatabase(this@HomeActivity)
            )
        }


        loadStatistics()
        loadAppointments()
    }

    private fun setControl() {
        btnAdd = findViewById(R.id.btnAdd)
        tvTotalPatients = findViewById(R.id.tvTotalPatients)
        tvTotalRecords = findViewById(R.id.tvTotalRecords)
        tvTodayAppointments = findViewById(R.id.tvTodayAppointments)
        tvTotalPrescriptions = findViewById(R.id.tvTotalPrescriptions)
        rcAppointments = findViewById(R.id.rcAppointments)
        layoutEmptyAppointments = findViewById(R.id.layoutEmptyAppointments)
        appointmentAdapter = AppointmentAdapter(emptyList())
        btnMenu = findViewById(R.id.btnMenu)
        rcAppointments.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = appointmentAdapter
        }


    }

    private fun setEvent() {
        // hiện tại dashboard chỉ hiển thị, chưa cần click

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            openDrawer()
        }

        //them lich hen
        btnAdd.setOnClickListener {
            AddQuickAppointmentBottomSheet {
                reloadDashboard()
            }.show(supportFragmentManager, "ADD_APPOINTMENT")
        }
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
        loadAppointments()
    }

    //cac ham xu ly du lieu
    private fun loadStatistics() {
        lifecycleScope.launch {
            val startToday = getStartOfToday()
            val endToday = getEndOfToday()

            val countPatient = withContext(Dispatchers.IO) {
                patientDao.countPatients()
            }
            val countRecord = withContext(Dispatchers.IO) {
                medicalRecordDao.countMedicalRecords()
            }
            val appointmentsToday = withContext(Dispatchers.IO) {
                appointmentDao.countTodayAppointments()
            }
            val countPrescription = withContext(Dispatchers.IO) {
                prescriptionDao.countPrescriptions()
            }

            tvTotalPatients.text = countPatient.toString()
            tvTotalRecords.text = countRecord.toString()
            tvTodayAppointments.text = appointmentsToday.toString()
            tvTotalPrescriptions.text =
                countPrescription.toString() ?: "Hiện đang chưa có đơn thuốc nào"
        }
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            val today = getStartOfToday()

            val data = withContext(Dispatchers.IO) {
                appointmentDao.getUpcomingAppointmentsWithPatient(today)
            }

            if (data.isEmpty()) {
                // 👉 HIỆN EMPTY STATE
                layoutEmptyAppointments.visibility = View.VISIBLE
                rcAppointments.visibility = View.GONE
            } else {
                // 👉 HIỆN LIST
                layoutEmptyAppointments.visibility = View.GONE
                rcAppointments.visibility = View.VISIBLE
                appointmentAdapter.submitList(data)
            }
        }
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }


    private fun getEndOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private fun reloadDashboard() {
        loadStatistics()
        loadAppointments()
    }


}