package com.example.projectqlbenhan.ui.medicalRecord

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.BaseActivity

class ProfilePatientMedicalRecord : BaseActivity() {

    // Quy định Layout cho BaseActivity
    override fun getLayoutResId() = R.layout.activity_profile_patient_medical_record

    // Khai báo các Views
    private lateinit var tvHeader: TextView
    private lateinit var btnBack: ImageView
    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientId: TextView
    // Thêm các view khác nếu layout của bạn có (VD: tvDob, tvGender...)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lưu ý: BaseActivity của bạn thường đã gọi setContentView trong onCreate của nó

        // ⭐ BƯỚC 1: Ánh xạ View từ XML vào Kotlin trước
        setControl()

        // ⭐ BƯỚC 2: Sau khi các biến lateinit đã có giá trị, mới xử lý dữ liệu
        getIntentData()

        // ⭐ BƯỚC 3: Thiết lập các sự kiện nút bấm
        setEvent()
    }

    private fun setControl() {
        // Ánh xạ ID chính xác từ file activity_profile_patient_medical_record.xml
        tvHeader = findViewById(R.id.tvHeader)
        btnBack = findViewById(R.id.btnBack)

        // Giả định các ID này có trong layout của bạn, nếu chưa có hãy bổ sung vào XML
        // tvPatientName = findViewById(R.id.tvPatientName)
        // tvPatientId = findViewById(R.id.tvPatientId)
    }

    private fun getIntentData() {
        // Lấy dữ liệu từ Intent truyền sang
        val patientName = intent.getStringExtra("patient_name") ?: "Thông tin bệnh nhân"
        val patientId = intent.getLongExtra("patient_id", -1L)

        // Gán dữ liệu vào View (Lúc này tvHeader đã được khởi tạo nên KHÔNG bị crash)
        tvHeader.text = patientName

        // Nếu có các view profile khác:
        // tvPatientName.text = "Tên: $patientName"
        // if (patientId != -1L) tvPatientId.text = "Mã BN: $patientId"
    }

    private fun setEvent() {
        // Sự kiện quay lại
        btnBack.setOnClickListener {
            finish()
        }
    }
}