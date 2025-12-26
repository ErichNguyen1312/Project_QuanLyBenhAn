package com.example.projectqlbenhan.ui.DanhGia

import android.os.Bundle
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.review.ReviewDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ScreenDanhGia_Detail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_danh_gia_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val db = MedicalRecordDatabase.getDatabase(this@ScreenDanhGia_Detail)
            val reviewId = intent.getLongExtra("reviewId", 0L)
            val item = db.reviewDao().getReviewById(reviewId)

            if (item != null) {
                setupData(item)
            }
        }
    }

    private fun setupData(item: ReviewDetail) {
        val tvDoctor = findViewById<TextView>(R.id.tvDoctorName)
        val tvPatient = findViewById<TextView>(R.id.tvPatientName)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvComment = findViewById<TextView>(R.id.tvComment)

        val rbDoc = findViewById<RatingBar>(R.id.ratingDoctor)
        val rbDiag = findViewById<RatingBar>(R.id.ratingDiagnosis)
        val rbMed = findViewById<RatingBar>(R.id.ratingMedication)

        tvDoctor.text = "Bác sĩ: ${item.doctorName}"
        tvPatient.text = "Bệnh nhân: ${item.patientName}"
        tvComment.text = item.comment ?: "Không có nội dung bình luận."

        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        tvDate.text = sdf.format(item.createdAt)

        rbDoc.rating = item.ratingDoctor.toFloat()
        rbDiag.rating = item.ratingDiagnosis.toFloat()
        rbMed.rating = item.ratingMedication.toFloat()
    }
}