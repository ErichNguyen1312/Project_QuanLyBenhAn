package com.example.projectqlbenhan.ui.medicalRecord

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.review.Review // Entity Review chuẩn của bro
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem // Entity thuốc chuẩn
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientMedicalRecordDetail : AppCompatActivity() {

    // --- UI Components ---
    private lateinit var tvDiagnosis: TextView
    private lateinit var layoutSymptoms: LinearLayout
    private lateinit var layoutPrescriptions: LinearLayout
    private lateinit var tvType: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDoctor: TextView
    private lateinit var tvNotes: TextView
    private lateinit var layoutNotesContainer: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var btnRateDoctor: Button

    // --- Data Variables ---
    private var recordId: Long = -1
    private var patientId: Long = -1
    private var doctorId: Long = -1
    private var patientName: String? = null

    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }
    private val recordDao by lazy { db.medicalRecordDao() }
    private val doctorDao by lazy { db.doctorDao() }
    private val patientDao by lazy { db.patientDao() }
    // 👇 SỬA: Dùng đúng DAO trong dự án của bro
    private val prescriptionItemDao by lazy { db.prescriptionItemDao() }
    private val reviewDao by lazy { db.reviewDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_medical_record_detail)

        getIntentData()
        setControl()
        setEvent()
        loadRecordDetail()
    }

    private fun setControl() {
        tvDiagnosis = findViewById(R.id.tvDiagnosis)
        layoutSymptoms = findViewById(R.id.layoutSymptoms)
        layoutPrescriptions = findViewById(R.id.layoutPrescriptions)
        tvType = findViewById(R.id.tvType)
        tvDate = findViewById(R.id.tvDate)
        tvDoctor = findViewById(R.id.tvDoctor)
        tvNotes = findViewById(R.id.tvNotes)
        layoutNotesContainer = findViewById(R.id.layoutNotesContainer)
        btnBack = findViewById(R.id.btnBack)
        btnRateDoctor = findViewById(R.id.btnRateDoctor)
    }

    private fun setEvent() {
        btnBack.setOnClickListener { finish() }

    }

    private fun getIntentData() {
        recordId = intent.getLongExtra("record_id", -1)
    }

    private fun loadRecordDetail() {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = recordDao.getRecordById(recordId)

            if (record == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PatientMedicalRecordDetail, "Không tìm thấy bệnh án!", Toast.LENGTH_LONG).show()
                    finish()
                }
                return@launch
            }

            doctorId = record.doctorId ?: -1L
            patientId = record.patientId

            val doctorName = if (doctorId != -1L) doctorDao.getDoctorNameById(doctorId) else "Chưa phân công"
            val patient = patientDao.getPatientById(patientId)
            patientName = patient?.fullName

            val listThuoc = prescriptionItemDao.getItemsByRecordId(recordId)

            checkIfCanReview()

            withContext(Dispatchers.Main) {
                // UI Binding
                tvDiagnosis.text = record.diagnosis
                tvType.text = record.diseaseType
                tvDate.text = formatDate(record.examinationDate)
                tvDoctor.text = doctorName ?: "Không rõ bác sĩ"

                // Ẩn hiện ghi chú
                val advice = record.doctorNotes
                if (advice.isNullOrBlank()) {
                    layoutNotesContainer.visibility = View.GONE
                } else {
                    layoutNotesContainer.visibility = View.VISIBLE
                    tvNotes.text = advice
                }

                // Render Lists
                loadSymptoms(record.symptoms)
                loadPrescriptionList(listThuoc)
            }
        }
    }

    private fun loadPrescriptionList(items: List<PrescriptionItem>) {
        layoutPrescriptions.removeAllViews()
        if (items.isEmpty()) return

        val inflater = LayoutInflater.from(this)
        for (item in items) {
            val view = inflater.inflate(R.layout.item_medicine_row, layoutPrescriptions, false)

            val tvName = view.findViewById<TextView>(R.id.tvDrugName)
            val tvQty = view.findViewById<TextView>(R.id.tvQuantity)
            val tvUse = view.findViewById<TextView>(R.id.tvDosage)

            // 👇 Map đúng trường trong PrescriptionItem
            tvName.text = item.medicineName
            tvQty.text = "${item.quantity} ${item.unit}"
            tvUse.text = item.dosage

            layoutPrescriptions.addView(view)
        }
    }

    private fun loadSymptoms(symptomString: String?) {
        layoutSymptoms.removeAllViews()
        if (symptomString.isNullOrBlank()) return
        val inflater = LayoutInflater.from(this)
        val symptoms = symptomString.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
        for (s in symptoms) {
            val view = inflater.inflate(R.layout.item_symptom, layoutSymptoms, false)
            val text = view.findViewById<TextView>(R.id.tvSymptom)
            text.text = "${s.uppercase()}"
            layoutSymptoms.addView(view)
        }
    }

    private fun checkIfCanReview() {
        lifecycleScope.launch(Dispatchers.IO) {
            val existingReview = reviewDao.getReviewByRecord(recordId)

            withContext(Dispatchers.Main) {
                if (existingReview == null) {
                    btnRateDoctor.visibility = View.VISIBLE
                    btnRateDoctor.text = "Đánh giá bác sĩ"
                    btnRateDoctor.isEnabled = true
                    btnRateDoctor.setOnClickListener { showAdvancedRatingDialog() }
                } else {
                    btnRateDoctor.visibility = View.VISIBLE
                    btnRateDoctor.text = "Đã đánh giá"
                    btnRateDoctor.isEnabled = false // Không cho bấm nữa
                }
            }
        }
    }

    private fun showAdvancedRatingDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rating_advanced)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val rbDoctor = dialog.findViewById<RatingBar>(R.id.rbDoctor)
        val rbDiagnosis = dialog.findViewById<RatingBar>(R.id.rbDiagnosis)
        val rbMedication = dialog.findViewById<RatingBar>(R.id.rbMedication)
        val edtComment = dialog.findViewById<EditText>(R.id.edtComment)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmitRating)

        btnSubmit.setOnClickListener {
            val rDoc = rbDoctor.rating.toInt()
            val rDiag = rbDiagnosis.rating.toInt()
            val rMed = rbMedication.rating.toInt()
            val comment = edtComment.text.toString()

            if (rDoc == 0 || rDiag == 0 || rMed == 0) {
                Toast.makeText(this, "Vui lòng chấm điểm đủ 3 mục!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveReview(rDoc, rDiag, rMed, comment)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun saveReview(rDoc: Int, rDiag: Int, rMed: Int, comment: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val review = Review(
                recordId = recordId,
                ratingDoctor = rDoc,
                ratingDiagnosis = rDiag,
                ratingMedication = rMed,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )
            reviewDao.insertReview(review)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@PatientMedicalRecordDetail, "Cảm ơn đánh giá của bạn!", Toast.LENGTH_SHORT).show()
                btnRateDoctor.text = "Đã đánh giá"
                btnRateDoctor.isEnabled = false
            }
        }
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}