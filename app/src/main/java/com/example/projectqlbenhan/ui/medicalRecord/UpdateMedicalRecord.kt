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

    // --- Views ---
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

    // --- Data Variables ---
    private var appointmentId: Long = -1
    private var patientId: Long = -1
    private var recordId: Long = -1

    private val medicineList = mutableListOf<PrescriptionItem>()
    private lateinit var prescriptionAdapter: PrescriptionItemAdapter

    // --- Database ---
    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }
    private val medicalRecordDao by lazy { db.medicalRecordDao() }
    private val appointmentDao by lazy { db.appointmentDao() }
    private val prescriptionItemDao by lazy { db.prescriptionItemDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_medical_record)

        getIntentData()
        setControl()
        setEvent()
        loadData()
    }

    // 1. Nhận dữ liệu từ Intent
    private fun getIntentData() {
        appointmentId = intent.getLongExtra("appointment_id", -1)
        patientId = intent.getLongExtra("patient_id", -1)
        recordId = intent.getLongExtra("record_id", -1)
    }

    // 2. Ánh xạ View & Cấu hình RecyclerView
    private fun setControl() {
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

        // Setup RecyclerView ngay tại đây
        prescriptionAdapter = PrescriptionItemAdapter(medicineList) { position ->
            // Bác sĩ xóa thuốc khỏi danh sách tạm
            medicineList.removeAt(position)
            prescriptionAdapter.notifyItemRemoved(position)
        }
        rcPrescription.layoutManager = LinearLayoutManager(this)
        rcPrescription.adapter = prescriptionAdapter
    }

    // 3. Xử lý sự kiện Click
    private fun setEvent() {
        tvCancel.setOnClickListener { finish() }

        btnAddMedicine.setOnClickListener {
            showAddMedicineDialog()
        }

        btnSave.setOnClickListener {
            if (recordId == -1L) saveNewRecord() else updateRecord()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirm()
        }
    }

    // 4. Logic Load dữ liệu ban đầu
    private fun loadData() {
        if (recordId != -1L) {
            setupForUpdateMode()
        } else {
            setupForCreateMode()
        }
    }

    private fun setupForCreateMode() {
        tvHeader.text = "Khám bệnh & Kê đơn"
        btnSave.text = "LƯU BỆNH ÁN"
        btnDelete.visibility = View.GONE
    }

    private fun setupForUpdateMode() {
        tvHeader.text = "Cập nhật bệnh án"
        btnSave.text = "CẬP NHẬT"
        btnDelete.visibility = View.VISIBLE

        // Load dữ liệu từ DB lên form
        lifecycleScope.launch(Dispatchers.IO) {
            val record = medicalRecordDao.getRecordById(recordId)
            val items = prescriptionItemDao.getItemsByRecordId(recordId)

            withContext(Dispatchers.Main) {
                if (record != null) {
                    edtDiagnosis.setText(record.diagnosis)
                    edtSymptoms.setText(record.symptoms)
                    edtType.setText(record.diseaseType)
                    edtNotes.setText(record.doctorNotes) // Load đúng vào doctorNotes

                    if (patientId == -1L) patientId = record.patientId
                }
                medicineList.clear()
                medicineList.addAll(items)
                prescriptionAdapter.notifyDataSetChanged()
            }
        }
    }

    // --- CÁC HÀM XỬ LÝ LOGIC NGHIỆP VỤ ---

    private fun showAddMedicineDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_medicine)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

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
        val diagnosis = edtDiagnosis.text.toString().trim()
        val symptoms = edtSymptoms.text.toString().trim()
        val type = edtType.text.toString().trim()
        val notes = edtNotes.text.toString().trim()
        val doctorId = SessionManager.getSpecificId(this)

        if (diagnosis.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán", Toast.LENGTH_SHORT).show()
            return
        }

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

            // Lưu danh sách thuốc
            val prescriptions = medicineList.map { it.copy(recordId = newRecordId) }
            if (prescriptions.isNotEmpty()) {
                prescriptionItemDao.insertPrescriptionItems(prescriptions)
            }

            // Cập nhật trạng thái lịch hẹn
            if (appointmentId != -1L) {
                appointmentDao.updateStatus(appointmentId, "COMPLETED")
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateMedicalRecord, "Lưu bệnh án thành công!", Toast.LENGTH_SHORT).show()
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

                // Cập nhật thuốc: Xóa cũ -> Thêm mới
                prescriptionItemDao.deleteItemsByRecordId(recordId)
                val newMedicines = medicineList.map { it.copy(itemId = 0, recordId = recordId) }
                if (newMedicines.isNotEmpty()) {
                    prescriptionItemDao.insertPrescriptionItems(newMedicines)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UpdateMedicalRecord, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa bệnh án này không?")
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
}