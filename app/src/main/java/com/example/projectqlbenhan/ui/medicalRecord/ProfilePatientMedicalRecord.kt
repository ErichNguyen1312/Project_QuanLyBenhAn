package com.example.projectqlbenhan.ui.medicalRecord

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePatientMedicalRecord : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvHeader: TextView
    private lateinit var rcRecyclerMedicalRecord: RecyclerView

    private var patientId: Long = -1
    private lateinit var layoutEmpty: View

    private lateinit var btnAddMedicalRecord: Button

    private var medicalRecordList: MutableList<MedicalRecord> = mutableListOf()
    private lateinit var adapter: MedicalRecordAdapter
    private val dao by lazy {
        MedicalRecordDatabase.getDatabase(this).medicalRecordDao()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile_patient_medical_record)

        setControl()
        getIntentData()
        setEvent()
        loadMedicalRecords()

    }


    private fun setEvent() {

        btnAddMedicalRecord.setOnClickListener {
            val intent = Intent(this, CreateMedicalRecord::class.java)
            intent.putExtra("patient_id", patientId)
            Log.d("patient_id_fromProfile", "patient_id: demo")
            createLauncher.launch(intent)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setControl() {
        btnBack = findViewById(R.id.btnBack)
        tvHeader = findViewById(R.id.tvHeader) // sửa lại ID theo header của bạn
        rcRecyclerMedicalRecord = findViewById(R.id.rcRecyclerMedicalRecord)
        rcRecyclerMedicalRecord.layoutManager = LinearLayoutManager(this)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnAddMedicalRecord = findViewById(R.id.btnAddMedicalRecord)

        //patient medical record detail
        adapter = MedicalRecordAdapter(medicalRecordList) { record ->
            val intent = Intent(this, PatientMedicalRecordDetail::class.java)
            intent.putExtra("record_id", record.recordId)
            detailLauncher.launch(intent)
        }
        rcRecyclerMedicalRecord.adapter = adapter
    }


    //cac ham xu ly
    private fun getIntentData() {
        patientId = intent.getLongExtra("patient_id", -1)
        val name = intent.getStringExtra("patient_name") ?: "Bệnh nhân"

        tvHeader.text = "Bệnh án của $name"
    }


    private fun loadMedicalRecords() {

        CoroutineScope(Dispatchers.IO).launch {
            val list = dao.getRecordsByPatient(patientId)
            Log.d("DEBUG", "Load record after create size = ${list.size}")

            withContext(Dispatchers.Main) {
                medicalRecordList.clear()
                medicalRecordList.addAll(list)
                adapter.notifyDataSetChanged()

                val isEmpty = medicalRecordList.isEmpty()
                layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rcRecyclerMedicalRecord.visibility = if (!isEmpty) View.VISIBLE else View.GONE
            }
        }

    }

    private val createLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadMedicalRecords()
            }
        }

    private val detailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadMedicalRecords()
            }
        }

}