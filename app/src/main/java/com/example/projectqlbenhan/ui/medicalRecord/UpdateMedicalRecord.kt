package com.example.projectqlbenhan.ui.medicalRecord

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateMedicalRecord : AppCompatActivity() {

    // Views
    private lateinit var tvHeader: TextView
    private lateinit var tvCancel: TextView
    private lateinit var edtDiagnosis: EditText
    private lateinit var edtSymptoms: EditText
    private lateinit var edtType: EditText
    private lateinit var edtNotes: EditText

    private lateinit var btnSave: Button
    private lateinit var btnDelete: TextView
    private lateinit var btnAddMedicine: Button
    private lateinit var rcPrescription: RecyclerView

    // Data
    private var appointmentId: Long = -1
    private var patientId: Long = -1
    private var recordId: Long = -1
    private var isViewOnly: Boolean = false // ⭐️ Biến cờ quan trọng

    private val medicineList = mutableListOf<PrescriptionItem>()
    private lateinit var prescriptionAdapter: PrescriptionItemAdapter

    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }
    private val medicalRecordDao by lazy { db.medicalRecordDao() }
    private val appointmentDao by lazy { db.appointmentDao() }
    private val prescriptionItemDao by lazy { db.prescriptionItemDao() }
    private val reviewDao by lazy { db.reviewDao() }
    private lateinit var btnRateDoctor: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_medical_record)

        getIntentData() // Lấy cờ is_view_only ở đây
        initViews()
        setupRecyclerView()

        if (recordId != -1L) {
            setupForUpdateMode()
        } else {
            setupForCreateMode()
        }

        setEvents()

        // ⭐️ LOGIC KHÓA GIAO DIỆN (Code cũ của bro đang thiếu đoạn này)
        if (isViewOnly) {
            setupReadOnlyMode()
        }
    }

    private fun getIntentData() {
        appointmentId = intent.getLongExtra("appointment_id", -1)
        patientId = intent.getLongExtra("patient_id", -1)
        recordId = intent.getLongExtra("record_id", -1)

        // Lấy cờ từ Intent
        isViewOnly = intent.getBooleanExtra("is_view_only", false)
    }

    private fun initViews() {
        tvHeader = findViewById(R.id.tvHeader)
        tvCancel = findViewById(R.id.tvCancel)
        edtDiagnosis = findViewById(R.id.edtDiagnosis)
        edtSymptoms = findViewById(R.id.edtSymptoms)
        edtType = findViewById(R.id.edtDiseaseType)
        edtNotes = findViewById(R.id.edtNotes)

        btnSave = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnAddMedicine = findViewById(R.id.btnAddMedicine)
        rcPrescription = findViewById(R.id.rcPrescription)
        btnRateDoctor = findViewById(R.id.btnRateDoctor)
    }

    private fun setupRecyclerView() {
        prescriptionAdapter = PrescriptionItemAdapter(medicineList) { position ->
            // Nếu chỉ xem thì KHÔNG cho xóa thuốc
            if (!isViewOnly) {
                medicineList.removeAt(position)
                prescriptionAdapter.notifyItemRemoved(position)
            }
        }
        rcPrescription.layoutManager = LinearLayoutManager(this)
        rcPrescription.adapter = prescriptionAdapter
    }

    // --- CÁC HÀM SETUP MODE ---

    private fun setupForCreateMode() {
        tvHeader.text = "Khám bệnh & Kê đơn"
        btnSave.text = "LƯU BỆNH ÁN"
        btnDelete.visibility = View.GONE
    }

    private fun setupForUpdateMode() {
        tvHeader.text = "Cập nhật bệnh án"
        btnSave.text = "CẬP NHẬT"
        btnDelete.visibility = View.VISIBLE
        loadExistingData()
    }

    // ⭐️ HÀM KHÓA GIAO DIỆN (Ẩn nút, khóa nhập liệu)
    private fun setupReadOnlyMode() {
        // 1. Ẩn nút
        btnSave.visibility = View.GONE
        btnDelete.visibility = View.GONE
        btnAddMedicine.visibility = View.GONE

        // 2. Đổi tiêu đề
        tvHeader.text = "Chi tiết bệnh án"

        // 3. Khóa nhập liệu
        disableEditText(edtDiagnosis)
        disableEditText(edtSymptoms)
        disableEditText(edtType)
        disableEditText(edtNotes)
        checkIfCanReview()
    }

    private fun disableEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.inputType = android.text.InputType.TYPE_NULL
        editText.setTextColor(resources.getColor(android.R.color.black, null))
        editText.background = null
    }

    private fun loadExistingData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = medicalRecordDao.getRecordById(recordId)
            val items = prescriptionItemDao.getItemsByRecordId(recordId)

            withContext(Dispatchers.Main) {
                if (record != null) {
                    edtDiagnosis.setText(record.diagnosis)
                    edtSymptoms.setText(record.symptoms)
                    edtType.setText(record.diseaseType)
                    edtNotes.setText(record.doctorNotes)
                    if (patientId == -1L) patientId = record.patientId
                }
                medicineList.clear()
                medicineList.addAll(items)
                prescriptionAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setEvents() {
        tvCancel.setOnClickListener { finish() }

        // Chặn sự kiện click nếu đang xem
        btnAddMedicine.setOnClickListener {
            if (!isViewOnly) showAddMedicineDialog()
        }

        btnSave.setOnClickListener {
            if (!isViewOnly) {
                if (recordId == -1L) saveNewRecord() else updateRecord()
            }
        }

        btnDelete.setOnClickListener {
            if (!isViewOnly) showDeleteConfirm()
        }
    }

    private fun showAddMedicineDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_medicine)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val edtName = dialog.findViewById<EditText>(R.id.edtMedName)
        val edtQty = dialog.findViewById<EditText>(R.id.edtMedQty)
        val edtUnit = dialog.findViewById<EditText>(R.id.edtMedUnit)
        val edtDosage = dialog.findViewById<EditText>(R.id.edtMedDosage)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmAdd)

        btnConfirm.setOnClickListener {
            val name = edtName.text.toString()
            val qty = edtQty.text.toString().toIntOrNull() ?: 0
            val unit = edtUnit.text.toString()
            val dosage = edtDosage.text.toString()

            if (name.isNotEmpty() && qty > 0) {
                val item = PrescriptionItem(
                    itemId = 0,
                    recordId = if (recordId == -1L) 0 else recordId,
                    medicineName = name,
                    quantity = qty,
                    unit = unit,
                    dosage = dosage
                )
                medicineList.add(item)
                prescriptionAdapter.notifyItemInserted(medicineList.size - 1)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Vui lòng nhập tên và số lượng > 0", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun saveNewRecord() {
        // ... (Giữ nguyên logic lưu)
        val diagnosis = edtDiagnosis.text.toString().trim()
        val symptoms = edtSymptoms.text.toString().trim()
        val type = edtType.text.toString().trim()
        val notes = edtNotes.text.toString().trim()
        val doctorId = SessionManager.getSpecificId(this)

        if (diagnosis.isEmpty()) return

        lifecycleScope.launch(Dispatchers.IO) {
            val newRecord = MedicalRecord(
                patientId = patientId,
                doctorId = doctorId,
                appointmentId = if (appointmentId != -1L) appointmentId else null,
                diagnosis = diagnosis,
                symptoms = symptoms,
                diseaseType = type,
                doctorNotes = notes,
                doctorAdvice = "",
                examinationDate = System.currentTimeMillis()
            )
            val newRecordId = medicalRecordDao.insert(newRecord)

            val prescriptions = medicineList.map { it.copy(recordId = newRecordId) }
            if (prescriptions.isNotEmpty()) {
                prescriptionItemDao.insertPrescriptionItems(prescriptions)
            }

            if (appointmentId != -1L) {
                appointmentDao.updateStatus(appointmentId, "COMPLETED")
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateMedicalRecord, "Lưu thành công!", Toast.LENGTH_SHORT)
                    .show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun updateRecord() {
        val diagnosis = edtDiagnosis.text.toString().trim()
        val symptoms = edtSymptoms.text.toString().trim()
        val type = edtType.text.toString().trim()
        val notes = edtNotes.text.toString().trim()

        lifecycleScope.launch(Dispatchers.IO) {
            val oldRecord = medicalRecordDao.getRecordById(recordId)
            if (oldRecord != null) {
                val updatedRecord = oldRecord.copy(
                    diagnosis = diagnosis,
                    symptoms = symptoms,
                    diseaseType = type,
                    doctorNotes = notes
                )
                medicalRecordDao.update(updatedRecord)

                prescriptionItemDao.deleteItemsByRecordId(recordId)
                val newMedicines = medicineList.map { it.copy(itemId = 0, recordId = recordId) }
                if (newMedicines.isNotEmpty()) {
                    prescriptionItemDao.insertPrescriptionItems(newMedicines)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UpdateMedicalRecord, "Đã cập nhật!", Toast.LENGTH_SHORT)
                        .show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    medicalRecordDao.deleteRecord(recordId)
                    prescriptionItemDao.deleteItemsByRecordId(recordId)
                    withContext(Dispatchers.Main) {
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun checkIfCanReview() {
        lifecycleScope.launch(Dispatchers.IO) {
            val existingReview = reviewDao.getReviewByRecord(recordId)

            withContext(Dispatchers.Main) {
                if (existingReview == null) {

                    btnRateDoctor.visibility = View.VISIBLE
                    btnRateDoctor.setOnClickListener { showAdvancedRatingDialog() }
                } else {

                    btnRateDoctor.visibility = View.GONE
                }
            }
        }
    }

    private fun showAdvancedRatingDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rating_advanced)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

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
            val review = com.example.projectqlbenhan.entity.review.Review(
                recordId = recordId, // Neo vào bệnh án hiện tại
                ratingDoctor = rDoc,
                ratingDiagnosis = rDiag,
                ratingMedication = rMed,
                comment = comment
            )
            reviewDao.insertReview(review)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateMedicalRecord, "Cảm ơn đánh giá của bạn!", Toast.LENGTH_SHORT).show()
                btnRateDoctor.visibility = View.GONE // Ẩn nút ngay lập tức
            }
        }
    }
}