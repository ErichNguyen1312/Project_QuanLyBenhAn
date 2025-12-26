package com.example.projectqlbenhan.ui.medicalRecord

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.ui.DonThuocUI.ChiTietDonThuoc
import com.example.projectqlbenhan.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateMedicalRecord : AppCompatActivity() {

    private lateinit var edtDiagnosis: EditText
    private lateinit var edtSymptoms: EditText
    private lateinit var edtType: EditText
    private lateinit var edtNotes: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: TextView
    private lateinit var btnAddMedicine: Button
    private lateinit var rcPrescription: RecyclerView
    private lateinit var tvHeader: TextView

    private var appointmentId: Long = -1
    private var patientId: Long = -1
    private var recordId: Long = -1
    private val medicineList = mutableListOf<PrescriptionItem>()
    private lateinit var prescriptionAdapter: PrescriptionItemAdapter

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

        if (recordId != -1L) setupForUpdateMode() else setupForCreateMode()
        setEvents()
    }

    /**
     * ⭐ FIX QUAN TRỌNG: Làm mới dữ liệu thuốc khi quay lại từ màn hình chỉnh sửa chi tiết
     */
    override fun onResume() {
        super.onResume()
        if (recordId != -1L) {
            loadPrescriptionOnly()
        }
    }

    private fun getIntentData() {
        appointmentId = intent.getLongExtra("appointment_id", -1)
        patientId = intent.getLongExtra("patient_id", -1)
        recordId = intent.getLongExtra("record_id", -1)
    }

    private fun initViews() {
        tvHeader = findViewById(R.id.tvHeader)
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
        prescriptionAdapter = PrescriptionItemAdapter(
            medicineList,
            onItemClick = { item ->
                if (item.itemId != 0L) {
                    val intent = Intent(this, ChiTietDonThuoc::class.java).apply {
                        putExtra("ITEM_ID", item.itemId)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Hãy 'Lưu bệnh án' trước khi xem chi tiết!", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { position ->
                medicineList.removeAt(position)
                prescriptionAdapter.notifyItemRemoved(position)
                prescriptionAdapter.notifyItemRangeChanged(position, medicineList.size)
            }
        )
        rcPrescription.layoutManager = LinearLayoutManager(this)
        rcPrescription.adapter = prescriptionAdapter
    }

    /**
     * Hàm chỉ load lại danh sách thuốc để cập nhật UI nhanh
     */
    private fun loadPrescriptionOnly() {
        lifecycleScope.launch(Dispatchers.IO) {
            val items = prescriptionItemDao.getItemsByRecordId(recordId)
            withContext(Dispatchers.Main) {
                medicineList.clear()
                medicineList.addAll(items)
                prescriptionAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddMedicineDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_medicine)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmAdd)
        btnConfirm.setOnClickListener {
            val name = dialog.findViewById<EditText>(R.id.edtMedName).text.toString()
            val qty = dialog.findViewById<EditText>(R.id.edtMedQty).text.toString().toIntOrNull() ?: 0
            val unit = dialog.findViewById<EditText>(R.id.edtMedUnit).text.toString()
            val dose = dialog.findViewById<EditText>(R.id.edtMedDosage).text.toString()
            val instruct = dialog.findViewById<EditText>(R.id.edtMedInstruction).text.toString()

            if (name.isNotEmpty() && qty > 0) {
                val item = PrescriptionItem(
                    recordId = if (recordId == -1L) 0 else recordId,
                    medicineName = name,
                    quantity = qty,
                    unit = unit,
                    dosage = dose,
                    instruction = instruct
                )
                medicineList.add(item)
                prescriptionAdapter.notifyItemInserted(medicineList.size - 1)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun saveNewRecord() {
        val diag = edtDiagnosis.text.toString().trim()
        if (diag.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val newRecord = MedicalRecord(
                patientId = patientId,
                doctorId = SessionManager.getSpecificId(this@UpdateMedicalRecord),
                appointmentId = if (appointmentId != -1L) appointmentId else null,
                diagnosis = diag,
                symptoms = edtSymptoms.text.toString(),
                diseaseType = edtType.text.toString(),
                doctorNotes = edtNotes.text.toString(),
                doctorAdvice = "",
                examinationDate = System.currentTimeMillis()
            )
            val newId = medicalRecordDao.insert(newRecord)

            val medicinesWithId = medicineList.map { it.copy(recordId = newId) }
            if (medicinesWithId.isNotEmpty()) {
                prescriptionItemDao.insertPrescriptionItems(medicinesWithId)
            }

            if (appointmentId != -1L) {
                appointmentDao.updateStatus(appointmentId, "COMPLETED")
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateMedicalRecord, "Lưu thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateRecord() {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = medicalRecordDao.getRecordById(recordId) ?: return@launch
            val updated = record.copy(
                diagnosis = edtDiagnosis.text.toString(),
                symptoms = edtSymptoms.text.toString(),
                diseaseType = edtType.text.toString(),
                doctorNotes = edtNotes.text.toString()
            )
            medicalRecordDao.update(updated)

            // Lưu ý: Đoạn này sẽ ghi đè lại thuốc. 
            // Nếu bạn muốn giữ lại itemId để không bị mất hiệu ứng, hãy cân nhắc logic update lẻ.
            prescriptionItemDao.deleteItemsByRecordId(recordId)
            val newMedicines = medicineList.map { it.copy(recordId = recordId, itemId = 0L) }
            if (newMedicines.isNotEmpty()) {
                prescriptionItemDao.insertPrescriptionItems(newMedicines)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateMedicalRecord, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadExistingData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val record = medicalRecordDao.getRecordById(recordId)
            val items = prescriptionItemDao.getItemsByRecordId(recordId)
            withContext(Dispatchers.Main) {
                record?.let {
                    edtDiagnosis.setText(it.diagnosis)
                    edtSymptoms.setText(it.symptoms)
                    edtType.setText(it.diseaseType)
                    edtNotes.setText(it.doctorNotes)
                }
                medicineList.clear()
                medicineList.addAll(items)
                prescriptionAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setEvents() {
        findViewById<TextView>(R.id.tvCancel).setOnClickListener { finish() }
        btnAddMedicine.setOnClickListener { showAddMedicineDialog() }
        btnSave.setOnClickListener { if (recordId == -1L) saveNewRecord() else updateRecord() }
        btnDelete.setOnClickListener { showDeleteConfirm() }
    }

    private fun showDeleteConfirm() {
        AlertDialog.Builder(this).setTitle("Xác nhận").setMessage("Xóa bệnh án này?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    medicalRecordDao.deleteRecord(recordId)
                    prescriptionItemDao.deleteItemsByRecordId(recordId)
                    withContext(Dispatchers.Main) { finish() }
                }
            }.setNegativeButton("Hủy", null).show()
    }

    private fun setupForCreateMode() {
        tvHeader.text = "Khám bệnh & Kê đơn"
        btnDelete.visibility = View.GONE
    }

    private fun setupForUpdateMode() {
        tvHeader.text = "Cập nhật bệnh án"
        btnDelete.visibility = View.VISIBLE
        loadExistingData()
    }
}