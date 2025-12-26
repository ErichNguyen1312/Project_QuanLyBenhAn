package com.example.projectqlbenhan.ui

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.BacSi.screenBacSi_Main
import com.example.projectqlbenhan.ui.DonThuocUI.DanhSachDonThuoc
import com.example.projectqlbenhan.ui.DonThuocUI.DonThuoc
import com.example.projectqlbenhan.ui.ThongKe.screenThongKe_BenhAn

import com.example.projectqlbenhan.ui.authService.Login
import com.example.projectqlbenhan.ui.patient.Patients
import com.example.projectqlbenhan.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    private val doc by lazy {
        MedicalRecordDatabase.getDatabase(this).doctorDao()
    }

    abstract fun getLayoutResId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        setControl()
        setEvent()
    }

    private fun setControl() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
    }

    private fun setEvent() {
        // inflate layout con
        val frame = findViewById<FrameLayout>(R.id.contentFrame)
        layoutInflater.inflate(getLayoutResId(), frame, true)
        setupDrawer()
        setupHeader()
    }

    protected fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_listPatient ->
                    startActivity(Intent(this, Patients::class.java))

                R.id.menu_logout -> {
                    SessionManager.logout(this)
                    startActivity(Intent(this, Login::class.java))
                    finishAffinity()
                }
                R.id.menu_ThongKeBenhAn ->{
                    startActivity(Intent(this, screenThongKe_BenhAn::class.java))
                    finishAffinity()
                }
                R.id.menu_quanLyDonThuoc ->{
                    startActivity(Intent(this, DanhSachDonThuoc::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupHeader() {
        val header = navigationView.getHeaderView(0)
        CoroutineScope(Dispatchers.IO).launch {
            // Kiểm tra null để tránh crash nếu chưa đăng nhập
            val doctorId = SessionManager.getSpecificId(this@BaseActivity)
            if (doctorId != -1L) {
                val doctorInfo = doc.getDoctorById(doctorId)
                withContext(Dispatchers.Main) {
                    if (doctorInfo != null) {
                        header.findViewById<TextView>(R.id.tvDoctorName).text = doctorInfo.fullName
                        header.findViewById<TextView>(R.id.tvDoctorDept).text =
                            doctorInfo.specialization
                    }
                }
            }
        }
    }




}
