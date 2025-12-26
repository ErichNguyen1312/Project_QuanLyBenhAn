package com.example.projectqlbenhan.ui.ThongKe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.home.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class screenThongKe_BenhAn : AppCompatActivity() {

    private lateinit var spDiseaseType: Spinner
    private lateinit var ct_btnBack: ImageButton
    private lateinit var ct_tvTieuDe: TextView
    private lateinit var lvMedicalRecord: ListView
    private lateinit var tvTotalCount: TextView

    private lateinit var adapter: Adapter_ThongKeBenhAn
    private lateinit var db: MedicalRecordDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_thong_ke_benh_an)


        db = MedicalRecordDatabase.getDatabase(this)


        setControl()
        initView()
        setupSpinnerData()
        setEvent()
    }

    private fun setControl() {
        spDiseaseType = findViewById(R.id.spDiseaseType)
        ct_btnBack = findViewById(R.id.ct_btnBack)
        ct_tvTieuDe = findViewById(R.id.ct_tvTieuDe)
        lvMedicalRecord = findViewById(R.id.lvMedicalRecord)
        tvTotalCount = findViewById(R.id.tvTotalCount)
    }

    private fun initView() {
        spDiseaseType = findViewById(R.id.spDiseaseType)
        lvMedicalRecord = findViewById(R.id.lvMedicalRecord)
        tvTotalCount = findViewById(R.id.tvTotalCount)


        ct_tvTieuDe.text = "Thống kê Bệnh án - Trần Thiện Trí"


        adapter = Adapter_ThongKeBenhAn(this, emptyList())
        lvMedicalRecord.adapter = adapter
    }

    private fun setupSpinnerData() {
        lifecycleScope.launch {

            val distinctTypes = withContext(Dispatchers.IO) {

                db.medicalRecordDao().getAllDiseaseTypes()
            }


            val spinnerItems = mutableListOf("Tất cả")
            spinnerItems.addAll(distinctTypes)

            val spinnerAdapter = ArrayAdapter(
                this@screenThongKe_BenhAn,
                android.R.layout.simple_spinner_item,
                spinnerItems
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spDiseaseType.adapter = spinnerAdapter
        }
    }

    private fun setEvent() {

        ct_btnBack.setOnClickListener {
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }


        spDiseaseType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedType = parent?.getItemAtPosition(position).toString()
                filterData(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun filterData(type: String) {
        lifecycleScope.launch {
            val listRecord = withContext(Dispatchers.IO) {
                if (type == "Tất cả") {
                    db.medicalRecordDao().getAll()
                } else {
                    db.medicalRecordDao().getByDiseaseType(type)
                }
            }

            adapter.updateData(listRecord)
            tvTotalCount.text = "Tìm thấy: ${listRecord.size} hồ sơ"
        }
    }
}