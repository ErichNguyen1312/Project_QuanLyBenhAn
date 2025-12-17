package com.example.projectqlbenhan.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.loginService.Login
import com.example.projectqlbenhan.ui.patient.Patients
import com.example.projectqlbenhan.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//abstract class BaseActivity : AppCompatActivity() {
//
//    protected lateinit var drawerLayout: DrawerLayout
//    protected lateinit var navigationView: NavigationView
//    protected lateinit var toolbar: MaterialToolbar
//
//    abstract fun getLayoutResId(): Int
//    abstract fun getToolbarTitle(): String
//
//    private val doc by lazy {
//        MedicalRecordDatabase.getDatabase(this).doctorDao()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 1. Nạp layout khung sườn (activity_base)
//        setContentView(R.layout.activity_base)
//
//        // 2. Ánh xạ các view trong khung sườn
//        drawerLayout = findViewById(R.id.drawerLayout)
//        navigationView = findViewById(R.id.navigationView)
////        toolbar = findViewById(R.id.toolbar)
//        val contentFrame = findViewById<FrameLayout>(R.id.contentFrame)
//
//        // 3. Nạp layout con (Dashboard, Patients...) vào bên trong FrameLayout
//        if (getLayoutResId() != 0) {
//            layoutInflater.inflate(getLayoutResId(), contentFrame, true)
//        }
//
//        // 4. Setup Toolbar & Menu Icon
//        setSupportActionBar(toolbar)
//        supportActionBar?.title = getToolbarTitle()
//
//        // Đặt icon 3 gạch
//        toolbar.setNavigationIcon(R.drawable.ic_menu_24)
//        toolbar.setNavigationOnClickListener {
//            drawerLayout.openDrawer(GravityCompat.START)
//        }
//
//        // 5. [FIX LỖI THANH ĐEN] Ép màu thanh trạng thái bằng code
//        window.statusBarColor = Color.parseColor("#2196F3") // Màu xanh
//
//        // Chỉnh icon pin/sóng thành màu trắng cho nổi trên nền xanh
//        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
//        windowInsetsController.isAppearanceLightStatusBars = false
//
//        // 6. Setup logic khác
//        setupHeader()
//        setupDrawer()
//    }
//
//    // --- XÓA BỎ HÀM override fun setContentView Ở ĐÂY ĐỂ TRÁNH LỖI LẶP ---
//
//    // Các hàm xử lý logic giữ nguyên
//    private fun setupHeader() {
//        val header = navigationView.getHeaderView(0)
//        CoroutineScope(Dispatchers.IO).launch {
//            // Kiểm tra null để tránh crash nếu chưa đăng nhập
//            val doctorId = SessionManager.getDoctorId(this@BaseActivity)
//            if (doctorId != -1L ) {
//                val doctorInfo = doc.getDoctorById(doctorId)
//                withContext(Dispatchers.Main) {
//                    if (doctorInfo != null) {
//                        header.findViewById<TextView>(R.id.tvDoctorName).text = doctorInfo.fullName
//                        header.findViewById<TextView>(R.id.tvDoctorDept).text = doctorInfo.specialization
//                    }
//                }
//            }
//        }
//    }
//
//    private fun setupDrawer() {
//        navigationView.setNavigationItemSelectedListener {
//            when (it.itemId) {
//                R.id.menu_profile -> openPatientsScreen(Patients.MODE_MY)
//                R.id.menu_listPatient -> startActivity(Intent(this, Patients::class.java))
//                R.id.menu_logout -> showLogoutConfirm()
//            }
//            drawerLayout.closeDrawers()
//            true
//        }
//    }
//
//    private fun openPatientsScreen(mode: String) {
//        val intent = Intent(this, Patients::class.java)
//        intent.putExtra("MODE", mode)
//        startActivity(intent)
//        // Không finish() nếu muốn quay lại dashboard, tuỳ bạn
//    }
//
//    private fun showLogoutConfirm() {
//        AlertDialog.Builder(this)
//            .setTitle("Đăng xuất")
//            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
//            .setPositiveButton("Đăng xuất") { _, _ ->
//                SessionManager.clear(this)
//                startActivity(Intent(this, Login::class.java))
//                finishAffinity()
//            }
//            .setNegativeButton("Hủy", null)
//            .show()
//    }
//}


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
                    SessionManager.clear(this)
                    startActivity(Intent(this, Login::class.java))
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
            val doctorId = SessionManager.getDoctorId(this@BaseActivity)
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
