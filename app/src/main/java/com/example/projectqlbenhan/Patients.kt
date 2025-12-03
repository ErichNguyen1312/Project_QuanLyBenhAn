package com.example.projectqlbenhan


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView

import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity

class Patients : AppCompatActivity() {
    lateinit var btnBack: ImageView
    lateinit var btnAddHeader: TextView
    lateinit var etSearch: EditText

    lateinit var lvPatients: ListView

    private val patients = mutableListOf(
        Patient(
            name = "Nguyễn Văn A",
            age = 34,
            gender = "Nam",
            recordId = "BN001",
            address = "12 Nguyễn Thị Minh Khai, Q.1, TP.HCM",
            phone = "0901234567"
        ),
        Patient(
            name = "Trần Thị B",
            age = 28,
            gender = "Nữ",
            recordId = "BN002",
            address = "45 Trường Chinh, Tân Bình, TP.HCM",
            phone = "0912345678"
        ),
        Patient(
            name = "Lê Văn C",
            age = 45,
            gender = "Nam",
            recordId = "BN003",
            address = "89 Lý Thường Kiệt, Q.10, TP.HCM",
            phone = "0939876543"
        ),
        Patient(
            name = "Phạm Thị D",
            age = 52,
            gender = "Nữ",
            recordId = "BN004",
            address = "23 Hoàng Văn Thụ, Phú Nhuận, TP.HCM",
            phone = "0969988776"
        ),
        Patient(
            name = "Hoàng Văn E",
            age = 60,
            gender = "Nam",
            recordId = "BN005",
            address = "100 Điện Biên Phủ, Bình Thạnh, TP.HCM",
            phone = "0988123456"
        )
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients)
        setControl()
        loadListPatient()
        setEvent()

    }

    private fun setEvent() {

        lvPatients.setOnItemClickListener { _, _, position, _ ->
        //profile patient
            val p = patients[position]

            val intent = Intent(this, ProfilePatient::class.java)

            intent.putExtra("name", p.name)
            intent.putExtra("age", p.age)
            intent.putExtra("gender", p.gender)
            intent.putExtra("recordId", p.recordId)
            intent.putExtra("address", p.address)
            intent.putExtra("phone", p.phone)
            deletePatientLauncher.launch(intent)


        }

        //goi ham them patient
        btnAddHeader.setOnClickListener {
            val intent = Intent(this, CreatePatient::class.java)
            addPatientLauncher.launch(intent)
        }

        //goi ham xoa patient

    }

    private fun setControl() {
        lvPatients = findViewById(R.id.lvPatients)
        btnBack = findViewById(R.id.btnBack)
        btnAddHeader = findViewById(R.id.btnAdd)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun loadListPatient() {
        val adapter = PatientAdapter(this, patients)
        lvPatients.adapter = adapter
    }


    //cac ham xu ly data

    //Create Patient
    val addPatientLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult

                val p = Patient(
                    name = data.getStringExtra("newPatient_name") ?: "",
                    age = data.getIntExtra("newPatient_age", 0),
                    gender = data.getStringExtra("newPatient_gender") ?: "",
                    recordId = data.getStringExtra("newPatient_recordId") ?: ""
                )

                patients.add(p)
                (lvPatients.adapter as PatientAdapter).notifyDataSetChanged()
            }
        }
    //xu ly benh nhan sau khi delete
    val deletePatientLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data ?: return@registerForActivityResult
                val recordId = data.getStringExtra("delete_recordId") ?: return@registerForActivityResult

                // Xóa trong danh sách
                val index = patients.indexOfFirst { it.recordId == recordId }
                if (index != -1) {
                    patients.removeAt(index)
                    (lvPatients.adapter as PatientAdapter).notifyDataSetChanged()
                }
            }
        }

}

