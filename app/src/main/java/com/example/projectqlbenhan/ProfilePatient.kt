package com.example.projectqlbenhan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfilePatient : AppCompatActivity() {
    lateinit var tvHeaderName: TextView
    lateinit var tvName: TextView
    lateinit var tvInfo: TextView
    lateinit var btnBack: ImageView
    lateinit var btnUpdate: Button

    lateinit var btnDelete: Button
    private var recordId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_patient)

        setControl()
        setData()
        setEvent()
    }

    private fun setEvent() {
        btnBack.setOnClickListener {
            finish()
        }
        btnUpdate.setOnClickListener {
            putDataToUpdateActivity()
        }
        btnDelete.setOnClickListener {
            showDeleteConfirm()

        }
    }

    private fun setData() {
        val name = intent.getStringExtra("name") ?: "Không rõ"
        val age = intent.getIntExtra("age", 0)
        val gender = intent.getStringExtra("gender") ?: "Không rõ"
        recordId = intent.getStringExtra("recordId") ?: "N/A"

        tvHeaderName.text = name
        tvName.text = "Tên: $name"
        tvInfo.text = "Tuổi: $age | Giới tính: $gender | Mã HS: $recordId"
    }

    private fun setControl() {
        tvHeaderName = findViewById(R.id.tvHeaderName)
        tvName = findViewById(R.id.tvName)
        tvInfo = findViewById(R.id.tvInfo)
        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
    }

    //cac ham xu ly
    //get data
    private fun putDataToUpdateActivity() {
        val name = intent.getStringExtra("name") ?: "Không rõ"
        val age = intent.getIntExtra("age", 0)
        val gender = intent.getStringExtra("gender") ?: "Không rõ"
        val recordId = intent.getStringExtra("recordId") ?: "N/A"
        val address = intent.getStringExtra("address") ?: "N/A"
        val phone = intent.getStringExtra("phone") ?: "N/A"


        var intent = Intent(this, UpdatePatient::class.java)
        intent.putExtra("name", name)
        intent.putExtra("age", age)
        intent.putExtra("gender", gender)
        intent.putExtra("recordId", recordId)
        intent.putExtra("address", address)
        intent.putExtra("phone", phone)
        startActivity(intent)
    }

    //show dialog form comfirm
    private fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xoá")
            .setMessage("Bạn có chắc muốn xoá bệnh nhân này không?")
            .setPositiveButton("Đồng ý") { _, _ ->
                returnDeleteResult()

            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // put record id  to list patient

    private fun returnDeleteResult() {
        val intent = Intent().apply {
            putExtra("delete_recordId", recordId)
        }
        setResult(RESULT_OK, intent)
        finish()
    }


}