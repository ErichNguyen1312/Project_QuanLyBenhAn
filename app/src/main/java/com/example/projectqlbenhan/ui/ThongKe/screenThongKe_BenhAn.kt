package com.example.projectqlbenhan.ui.ThongKe

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.home.HomeActivity
import com.example.projectqlbenhan.ui.patient.ProfilePatient
import kotlinx.coroutines.launch

class screenThongKe_BenhAn : AppCompatActivity() {

    private lateinit var spDiseaseType: Spinner
    private lateinit var ct_btnBack: ImageButton
    private lateinit var ct_tvTieuDe: TextView
    private lateinit var lvMedicalRecord: ListView
    private lateinit var adapter: Adapter_ThongKeBenhAn
    private lateinit var db: MedicalRecordDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_thong_ke_benh_an)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setControl()
        loadSpinner()
        loadAllRecords()
        setEvent()
    }

    private fun setControl() {
        spDiseaseType = findViewById(R.id.spDiseaseType)
        lvMedicalRecord = findViewById(R.id.lvMedicalRecord)
        ct_btnBack = findViewById(R.id.ct_btnBack)
        ct_tvTieuDe = findViewById(R.id.ct_tvTieuDe)

        ct_tvTieuDe.text = "Thống kê bệnh án"

        db = MedicalRecordDatabase.getDatabase(this)

        adapter = Adapter_ThongKeBenhAn(this, mutableListOf())
        lvMedicalRecord.adapter = adapter
    }

    private fun loadSpinner() {
        lifecycleScope.launch {
            val types = db.medicalRecordDao().getAllDiseaseTypes().toMutableList()
            types.add(0, "Tất cả")

            val spinnerAdapter = ArrayAdapter(
                this@screenThongKe_BenhAn,
                android.R.layout.simple_spinner_item,
                types
            )
            spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            spDiseaseType.adapter = spinnerAdapter
        }
    }

    private fun loadAllRecords() {
        lifecycleScope.launch {
            val data = db.medicalRecordDao().getAll()
            adapter.updateData(data)
        }
    }

    private fun setEvent() {
        ct_btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        spDiseaseType.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    val type = parent.getItemAtPosition(position).toString()

                    lifecycleScope.launch {
                        val list = if (type == "Tất cả") {
                            db.medicalRecordDao().getAll()
                        } else {
                            db.medicalRecordDao().getByDiseaseType(type)
                        }
                        adapter.updateData(list)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }
    }
}
