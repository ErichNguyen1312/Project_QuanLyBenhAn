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

    private lateinit var btnSave: Button // Nút "LƯU BỆNH ÁN"
    private lateinit var btnDelete: TextView
    private lateinit var btnAddMedicine: Button
    private lateinit var rcPrescription: RecyclerView

    // Data & Logic
    private var appointmentId: Long = -1
    private var patientId: Long = -1
    private var recordId: Long = -1 // -1: Tạo mới, >0: Cập nhật

    // Quản lý thuốc
    private val medicineList = mutableListOf<PrescriptionItem>()
    private lateinit var prescriptionAdapter: PrescriptionItemAdapter

    // Database & DAOs
    private val db by lazy { MedicalRecordDatabase.getDatabase(this) }
    private val medicalRecordDao by lazy { db.medicalRecordDao() }
    private val appointmentDao by lazy { db.appointmentDao() }
    private val prescriptionItemDao by lazy { db.prescriptionItemDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_medical_record)

        getIntentData()
        initViews()
        setupRecyclerView()

        // Phân biệt chế độ: Tạo mới hay Cập nhật
        if (recordId != -1L) {
            setupForUpdateMode()
        } else {
            setupForCreateMode()
        }

        setEvents()
    }

    private fun getIntentData() {
        appointmentId = intent.getLongExtra("appointment_id", -1)
        patientId = intent.getLongExtra("patient_id", -1)
        recordId = intent.getLongExtra("record_id", -1)
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
    }

    private fun setupRecyclerView() {
        // Adapter thuốc với sự kiện xóa
        prescriptionAdapter = PrescriptionItemAdapter(medicineList) { position ->
            medicineList.removeAt(position)
            prescriptionAdapter.notifyItemRemoved(position)
        }
        rcPrescription.layoutManager = LinearLayoutManager(this)
        rcPrescription.adapter = prescriptionAdapter
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

        loadExistingData()
    }

    private fun loadExistingData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = medicalRecordDao.getRecordById(recordId)
            val items = prescriptionItemDao.getItemsByRecordId(recordId) // Load thuốc cũ

            withContext(Dispatchers.Main) {
                if (record != null) {
                    edtDiagnosis.setText(record.diagnosis)
                    edtSymptoms.setText(record.symptoms)
                    edtType.setText(record.diseaseType)
                    edtNotes.setText(record.doctorNotes) // Sửa lại field cho khớp entity

                    // Nếu vào từ List hồ sơ thì patientId có thể chưa set, lấy từ record
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

        btnAddMedicine.setOnClickListener { showAddMedicineDialog() }

        btnSave.setOnClickListener {
            if (recordId == -1L) saveNewRecord() else updateRecord()
        }

        btnDelete.setOnClickListener { showDeleteConfirm() }
    }

    // --- Dialog Thêm Thuốc ---
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
                // Tạo item thuốc tạm
                val item = PrescriptionItem(
                    itemId = 0,
                    recordId = if(recordId == -1L) 0 else recordId,
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

    // --- CASE 1: TẠO MỚI ---
    private fun saveNewRecord() {
        val diagnosis = edtDiagnosis.text.toString().trim()
        val symptoms = edtSymptoms.text.toString().trim()
        val type = edtType.text.toString().trim()
        val notes = edtNotes.text.toString().trim()
        val doctorId = SessionManager.getSpecificId(this) // Lấy ID bác sĩ từ session

        if (diagnosis.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Insert Medical Record
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

            // 2. Insert Prescriptions (Update recordId cho list thuốc)
            val prescriptions = medicineList.map { it.copy(recordId = newRecordId) }
            if (prescriptions.isNotEmpty()) {
                prescriptionItemDao.insertPrescriptionItems(prescriptions)
            }

            // 3. Update Appointment Status (Nếu khám từ lịch hẹn)
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

    // --- CASE 2: CẬP NHẬT ---
    private fun updateRecord() {
        val diagnosis = edtDiagnosis.text.toString().trim()
        val symptoms = edtSymptoms.text.toString().trim()
        val type = edtType.text.toString().trim()
        val notes = edtNotes.text.toString().trim()

        lifecycleScope.launch(Dispatchers.IO) {
            val oldRecord = medicalRecordDao.getRecordById(recordId)
            if (oldRecord != null) {
                // Giữ nguyên các trường không sửa (ngày tạo, patientId, doctorId...)
                val updatedRecord = oldRecord.copy(
                    diagnosis = diagnosis,
                    symptoms = symptoms,
                    diseaseType = type,
                    doctorNotes = notes
                )
                medicalRecordDao.update(updatedRecord)

                // Cập nhật thuốc: Cách đơn giản nhất là Xóa hết cũ -> Insert lại mới
                prescriptionItemDao.deleteItemsByRecordId(recordId)
                val newMedicines = medicineList.map { it.copy(itemId = 0, recordId = recordId) }
                if (newMedicines.isNotEmpty()) {
                    prescriptionItemDao.insertPrescriptionItems(newMedicines)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UpdateMedicalRecord, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    // --- CASE 3: XÓA ---
    private fun showDeleteConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa bệnh án này không? Dữ liệu thuốc cũng sẽ bị xóa.")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    medicalRecordDao.deleteRecord(recordId)
                    // Thuốc sẽ tự xóa nếu database thiết lập Cascade, hoặc xóa thủ công:
                    prescriptionItemDao.deleteItemsByRecordId(recordId)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateMedicalRecord, "Đã xóa bệnh án", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}