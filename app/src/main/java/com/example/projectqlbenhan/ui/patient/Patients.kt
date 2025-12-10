package com.example.projectqlbenhan.ui.patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.PatientDao
import com.example.projectqlbenhan.entity.Patient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class Patients : AppCompatActivity() {
    lateinit var btnBack: ImageView
    lateinit var btnAddHeader: TextView
    lateinit var etSearch: EditText
    lateinit var lvPatients: ListView
    lateinit var dao: PatientDao
    private val patients = mutableListOf<com.example.projectqlbenhan.entity.Patient>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients)
        dao = MedicalRecordDatabase.getDatabase(this).patientDao()
        setControl()

        CoroutineScope(Dispatchers.IO).launch {
            seedPatientsIfEmpty(dao)
            loadListPatient()
        }

        setEvent()

    }

    //override resume de load danh sach
    override fun onResume() {
        super.onResume()
        loadListPatient()   // Reload lại DB mỗi lần quay lại màn hình
    }


    private fun setEvent() {

        lvPatients.setOnItemClickListener { _, _, position, _ ->
            val patientDeail = patients[position]
            val intent = Intent(this, ProfilePatient::class.java)
            intent.putExtra("patient_id", patientDeail.patientId)
            startActivity(intent)
            Log.d("patient_id_fromList", "patient_id: ${patientDeail.patientId}")
        }

        //goi ham them patient
        btnAddHeader.setOnClickListener {
            val intent = Intent(this, CreatePatient::class.java)
            startActivity(intent)
        }


    }

    private fun setControl() {
        lvPatients = findViewById(R.id.lvPatients)
        btnBack = findViewById(R.id.btnBack)
        btnAddHeader = findViewById(R.id.btnAdd)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun loadListPatient() {
        patients.clear()
        patients.addAll(dao.getAll())
        val adapter = PatientAdapter(this, patients)
        lvPatients.adapter = adapter
        lifecycleScope.launch(Dispatchers.IO) {
//            val list = dao.getAll()  // Lấy dữ liệu từ Room
//
//            withContext(Dispatchers.Main) {
//                patients.clear()
//                patients.addAll(list)
//                adapter.notifyDataSetChanged()
//            }
        }
    }


    suspend fun seedPatientsIfEmpty(dao: PatientDao) {

        val existing = dao.getAll()
        if (existing.isNotEmpty()) return   // Đã có dữ liệu → không seed nữa

        val now = System.currentTimeMillis()

        val samplePatients = listOf(
            Patient(
                fullName = "Nguyễn Văn A",
                dateOfBirth = getDate(1990, 1, 10),
                gender = "Nam",
                phoneNumber = "0901234567",
                address = "12 Nguyễn Thị Minh Khai, Q.1, TP.HCM",
                medicalRecordNumber = "BN001",
                createdAt = now
            ),
            Patient(
                fullName = "Trần Thị B",
                dateOfBirth = getDate(1996, 5, 22),
                gender = "Nữ",
                phoneNumber = "0912345678",
                address = "45 Trường Chinh, Tân Bình, TP.HCM",
                medicalRecordNumber = "BN002",
                createdAt = now
            ),
            Patient(
                fullName = "Lê Văn C",
                dateOfBirth = getDate(1979, 3, 5),
                gender = "Nam",
                phoneNumber = "0939876543",
                address = "89 Lý Thường Kiệt, Q.10, TP.HCM",
                medicalRecordNumber = "BN003",
                createdAt = now
            ),
            Patient(
                fullName = "Phạm Thị D",
                dateOfBirth = getDate(1972, 8, 18),
                gender = "Nữ",
                phoneNumber = "0969988776",
                address = "23 Hoàng Văn Thụ, Phú Nhuận, TP.HCM",
                medicalRecordNumber = "BN004",
                createdAt = now
            ),
            Patient(
                fullName = "Hoàng Văn E",
                dateOfBirth = getDate(1964, 11, 2),
                gender = "Nam",
                phoneNumber = "0988123456",
                address = "100 Điện Biên Phủ, Bình Thạnh, TP.HCM",
                medicalRecordNumber = "BN005",
                createdAt = now
            )
        )

        samplePatients.forEach { dao.insertPatient(it) }
    }
    fun getDate(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }


    //cac ham xu ly data

    fun calculateAge(dob: Long): Int {
        val dobCalendar = Calendar.getInstance().apply { timeInMillis = dob }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)

        // Nếu chưa tới sinh nhật năm nay thì -1 tuổi
        if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

}