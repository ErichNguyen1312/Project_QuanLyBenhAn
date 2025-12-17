package com.example.projectqlbenhan.ui.patient

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.ui.home.HomeActivity
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Patients : AppCompatActivity() {
    lateinit var btnBack: ImageView
    lateinit var btnAddHeader: TextView
    lateinit var etSearch: EditText
    lateinit var rvPatients: RecyclerView
    private val patients = mutableListOf<Patient>()

    private val allPatients = mutableListOf<Patient>()
    private val displayPatients = mutableListOf<Patient>()
    private lateinit var adapter: PatientAdapterRecycler

    companion object {
        const val MODE_ALL = "MODE_ALL"
        const val MODE_MY = "MODE_MY"
    }

    private var mode: String = MODE_ALL


    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).patientDao()
    }
    private val doc by lazy {
        MedicalRecordDatabase.getDatabase(this).doctorDao()
    }
//    private lateinit var drawerLayout: DrawerLayout
//    private lateinit var navigationView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients)
        mode = intent.getStringExtra("MODE") ?: MODE_ALL

        setControl()
        setupRecyclerView()
//        setupDrawer()
//        setupHeader()
        setEvent()

    }

    //override resume de load danh sach
    override fun onResume() {
        super.onResume()
        loadListPatient()   // Reload lại DB mỗi lần quay lại màn hình
    }


    private fun setEvent() {


        //goi ham them patient
        btnAddHeader.setOnClickListener {
            val intent = Intent(this, CreatePatient::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))

        }

        //tim kiem
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPatients(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun setControl() {

        rvPatients = findViewById(R.id.rvPatients)
        rvPatients.layoutManager = LinearLayoutManager(this)
        btnBack = findViewById(R.id.btnBack)
        btnAddHeader = findViewById(R.id.btnAdd)
        etSearch = findViewById(R.id.etSearch)
//        drawerLayout = findViewById(R.id.drawerLayout)
//        navigationView = findViewById(R.id.navigationView)


    }


    //cac ham xu ly du lieu

//    private fun setupDrawer() {
//        navigationView.setNavigationItemSelectedListener {
//            when (it.itemId) {
//
//                R.id.menu_profile -> {
////                    startActivity(Intent(this, DoctorProfile::class.java))
//                    openPatientsScreen(Patients.MODE_MY)
//                }
//
//                R.id.menu_logout -> {
//                    showLogoutConfirm()
//                }
//            }
//            drawerLayout.closeDrawers()
//            true
//        }
//    }

//    private fun openPatientsScreen(mode: String) {
//        val intent = Intent(this, Patients::class.java)
//        intent.putExtra("MODE", mode)
//        startActivity(intent)
//        finish()
//    }

//    private fun setupHeader() {
//        val header = navigationView.getHeaderView(0)
//        CoroutineScope(Dispatchers.IO).launch {
//            val doctorInfo = doc.getDoctorById(SessionManager.getDoctorId(this@Patients))
//
//
//            withContext(Dispatchers.Main) {
//                header.findViewById<TextView>(R.id.tvDoctorName).text = doctorInfo.fullName
//
//                header.findViewById<TextView>(R.id.tvDoctorDept).text = doctorInfo.specialization
//
//            }
//        }
//
//
//    }

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


    private fun filterPatients(keyword: String) {
        val key = keyword.trim().lowercase()

        displayPatients.clear()

        if (key.isEmpty()) {
            displayPatients.addAll(allPatients)
        } else {
            displayPatients.addAll(
                allPatients.filter {
                    it.fullName.lowercase().contains(key) ||
                            it.medicalRecordNumber.lowercase().contains(key)
                }
            )
        }

        adapter.notifyDataSetChanged()
    }

    private fun loadListPatient() {
        CoroutineScope(Dispatchers.IO).launch {

            val list = when (mode) {
                MODE_MY -> {
                    val doctorId = SessionManager.getDoctorId(this@Patients)
                    dao.getPatientsByDoctor(doctorId)
                }

                else -> {
                    dao.getAll()
                }
            }

            withContext(Dispatchers.Main) {
                allPatients.clear()
                allPatients.addAll(list)

                displayPatients.clear()
                displayPatients.addAll(list)

                adapter.notifyDataSetChanged()
            }
        }
    }


    private fun setupRecyclerView() {
        adapter = PatientAdapterRecycler(displayPatients) { patient ->
            val intent = Intent(this, ProfilePatient::class.java)
            intent.putExtra("patient_id", patient.patientId)
            startActivity(intent)
        }
        rvPatients.layoutManager = LinearLayoutManager(this)
        rvPatients.adapter = adapter
    }

}