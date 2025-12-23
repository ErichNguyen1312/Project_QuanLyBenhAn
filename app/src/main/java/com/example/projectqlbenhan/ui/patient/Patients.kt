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
import com.google.android.material.chip.ChipGroup
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
    private lateinit var chipGroupFilter: ChipGroup


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients)
        mode = intent.getStringExtra("MODE") ?: MODE_ALL

        setControl()
        setupRecyclerView()
        if (mode == MODE_MY) {
            chipGroupFilter.check(R.id.chipMyPatients)
        } else {
            chipGroupFilter.check(R.id.chipAll)
        }
        setEvent()

    }

    //override resume de load danh sach
    override fun onResume() {
        super.onResume()
        loadListPatient()
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
        //filter
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipAll -> {
                        mode = MODE_ALL
                        loadListPatient()
                    }
                    R.id.chipMyPatients -> {
                        mode = MODE_MY
                        loadListPatient()
                    }
                }
            }
        }
    }


    private fun setControl() {

        rvPatients = findViewById(R.id.rvPatients)
        rvPatients.layoutManager = LinearLayoutManager(this)
        btnBack = findViewById(R.id.btnBack)
        btnAddHeader = findViewById(R.id.btnAdd)
        etSearch = findViewById(R.id.etSearch)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)

    }


    //cac ham xu ly du lieu
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
                    val doctorId = SessionManager.getSpecificId(this@Patients)
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