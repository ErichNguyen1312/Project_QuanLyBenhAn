package com.example.projectqlbenhan.ui.TaiKham

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class screenTaiKham_Main : AppCompatActivity() {
    lateinit var adapter: Adapter_TaiKham

    lateinit var ct_btnBack: ImageButton
    lateinit var ct_tvTieuDe: TextView
    lateinit var listTaiKham: ListView
    lateinit var btnDatLichTK: Button
    private lateinit var db: MedicalRecordDatabase

    private var maBenhNhan : Long = -1

    override fun onResume() {
        super.onResume()
        refreshList()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_tai_kham_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setControl()
        setEvent()
        refreshList()
    }
    private fun refreshList() {
        lifecycleScope.launch {
            val newData = withContext(Dispatchers.IO) {
                db.appointmentDao().getAppointmentsByPatient(maBenhNhan).toMutableList()
            }
            adapter.updateData(newData)
        }
    }

    private fun setControl() {
        ct_btnBack = findViewById(R.id.ct_btnBack)
        ct_tvTieuDe = findViewById(R.id.ct_tvTieuDe)
        listTaiKham = findViewById(R.id.listTaiKham)
        btnDatLichTK = findViewById(R.id.btnDatLichTK)

        db = MedicalRecordDatabase.getDatabase(this)
        ct_tvTieuDe.text = "Lịch tái khám"

        adapter = Adapter_TaiKham(
            this,
            mutableListOf()
        ) { appointment ->
            val intent = Intent(
                this@screenTaiKham_Main,
                screenTaiKham_Edit::class.java
            )

            intent.putExtra("maTaiKham", appointment.appointmentId)
            intent.putExtra("maBenhNhan", appointment.patientId)
            intent.putExtra("maBenhAn", appointment.recordId)
            intent.putExtra("ngayTaiKham", appointment.appointmentDate)
            intent.putExtra("gioTaiKham", appointment.appointmentTime)
            intent.putExtra("maBacSi", appointment.doctorId)
            intent.putExtra("ghiChu", appointment.notes)

            editLauncher.launch(intent)
        }

        listTaiKham.adapter = adapter

        maBenhNhan = intent.getLongExtra("patient_id", -1)
    }


    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshList() // refresh ListView khi quay về
        }
    }
    private fun setEvent() {
        val maBenhNhan = intent.getLongExtra("patient_id", -1)
        ct_btnBack.setOnClickListener { finish() }

        btnDatLichTK.setOnClickListener {

            intent = Intent(this, screenTaiKham_Create::class.java)
            intent.putExtra("patient_id", maBenhNhan)
            startActivity(intent)
        }
    }
}