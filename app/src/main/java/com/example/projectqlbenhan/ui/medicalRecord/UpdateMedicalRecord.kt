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
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.ui.DonThuocUI.ChiTietDonThuoc
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

    // Danh sách thuốc tạm thời
    private var medicineList = mutableListOf<PrescriptionItem>()
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

    private fun getIntentData() {
        appointmentId = intent.getLongExtra("appointment_id", -1)
        patientId = intent.getLongExtra("patient_id", -1)
        recordId = intent.getLongExtra("record_id", -1)
    }

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

        // --- SỬA LẠI ĐOẠN KHỞI TẠO ADAPTER ---
        prescriptionAdapter = PrescriptionItemAdapter(
            list = medicineList,

            // Tham số 2: onItemClick (Xử lý khi bấm vào dòng thuốc)
            onItemClick = { item ->
                val intent = Intent(this, ChiTietDonThuoc::class.java)
                intent.putExtra("ITEM_ID", item.itemId)
                startActivity(intent)
            },

            // Tham số 3: onDeleteClick (Xử lý khi bấm nút thùng rác)
            onDeleteClick = { position ->
                if (position >= 0 && position < medicineList.size) {
                    medicineList.removeAt(position)
                    prescriptionAdapter.notifyItemRemoved(position)
                    // Cập nhật lại vị trí các item bên dưới để tránh lỗi IndexOutOfBounds khi xóa tiếp
                    prescriptionAdapter.notifyItemRangeChanged(position, medicineList.size)
                }
            }
        )

        rcPrescription.layoutManager = LinearLayoutManager(this)
        rcPrescription.adapter = prescriptionAdapter
    }

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

    override fun onResume() {
        loadData()
        super.onResume()
    }

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

        // Đảm bảo list thuốc rỗng khi tạo mới
        medicineList.clear()
        prescriptionAdapter.notifyDataSetChanged()
    }

    private fun setupForUpdateMode() {
        tvHeader.text = "Cập nhật bệnh án"
        btnSave.text = "CẬP NHẬT"
        btnDelete.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val record = medicalRecordDao.getRecordById(recordId)
            // Chỉ load thuốc của Bệnh án này
            val items = prescriptionItemDao.getItemsByRecordId(recordId)

            withContext(Dispatchers.Main) {
                if (record != null) {
                    edtDiagnosis.setText(record.diagnosis)
                    edtSymptoms.setText(record.symptoms)
                    edtType.setText(record.diseaseType)
                    edtNotes.setText(record.doctorNotes)

                    if (patientId == -1L) patientId = record.patientId
                }

                // Cập nhật list thuốc lên giao diện
                medicineList.clear()
                medicineList.addAll(items)
                prescriptionAdapter.notifyDataSetChanged()
            }
        }
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
            val qtyStr = edtQty.text.toString()
            val unit = edtUnit.text.toString()
            val dosage = edtDosage.text.toString()

            if (name.isNotEmpty() && qtyStr.isNotEmpty()) {
                val item = PrescriptionItem(
                    itemId = 0, // ID tạm là 0
                    recordId = if (recordId == -1L) 0 else recordId,
                    medicineName = name,
                    quantity = qtyStr.toIntOrNull() ?: 0,
                    unit = unit,
                    dosage = dosage,
                    createdAt = System.currentTimeMillis() // Thêm thời gian tạo
                )
                medicineList.add(item)
                prescriptionAdapter.notifyItemInserted(medicineList.size - 1)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Vui lòng nhập tên và số lượng", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    // --- Lưu Bệnh Án Mới ---
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

            // Lưu danh sách thuốc kèm theo ID bệnh án mới tạo
            if (medicineList.isNotEmpty()) {
                val prescriptions = medicineList.map {
                    it.copy(recordId = newRecordId)
                }
                prescriptionItemDao.insertPrescriptionItems(prescriptions)
            }

            if (appointmentId != -1L) {
                appointmentDao.updateStatus(appointmentId, "COMPLETED")
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateMedicalRecord, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    // --- Cập nhật Bệnh Án ---
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

                // Cập nhật thuốc: Xóa hết thuốc cũ của record này -> Thêm danh sách mới hiện tại
                // Đây là cách đơn giản nhất để đồng bộ
                prescriptionItemDao.deleteItemsByRecordId(recordId)

                if (medicineList.isNotEmpty()) {
                    val newMedicines = medicineList.map {
                        it.copy(itemId = 0, recordId = recordId) // Reset ID về 0 để insert mới
                    }
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
                    // Thuốc sẽ tự xóa nếu bạn cấu hình ForeignKey CASCADE trong Entity,
                    // nếu không thì phải gọi dòng dưới:
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