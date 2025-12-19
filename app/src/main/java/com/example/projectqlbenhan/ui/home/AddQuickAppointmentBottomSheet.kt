package com.example.projectqlbenhan.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.appointment.AppointmentDao
import com.example.projectqlbenhan.dao.medicalRecord.MedicalRecordDao
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.ui.ThongBaoTaiKham.Helper_ThongBaoTaiKham
import com.example.projectqlbenhan.utils.MrnGenerator
import com.example.projectqlbenhan.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddQuickAppointmentBottomSheet(private val onAdded: () -> Unit) :
    BottomSheetDialogFragment() {
    private lateinit var patientDao: PatientDao
    private lateinit var appointmentDao: AppointmentDao
    private lateinit var medicalRecordDao: MedicalRecordDao

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtDate: EditText
    private lateinit var edtTime: EditText
    private lateinit var btnSave: Button

    private var selectedDateMillis: Long = 0L
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.bottomsheet_add_appointment,
            container,
            false
        )

        val db = MedicalRecordDatabase.getDatabase(requireContext())
        patientDao = db.patientDao()
        appointmentDao = db.appointmentDao()
        medicalRecordDao = db.medicalRecordDao()

        edtName = view.findViewById(R.id.edtName)
        edtPhone = view.findViewById(R.id.edtPhone)
        edtDate = view.findViewById(R.id.edtDate)
        edtTime = view.findViewById(R.id.edtTime)
        btnSave = view.findViewById(R.id.btnSave)

        edtDate.setOnClickListener { pickDate() }
        edtTime.setOnClickListener { pickTime() }
        btnSave.setOnClickListener { saveAppointment() }

        return view
    }


    private fun pickDate() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day, 0, 0, 0)
                selectedDateMillis = cal.timeInMillis
                edtDate.setText(
                    String.format("%02d/%02d/%d", day, month + 1, year)
                )
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    private fun pickTime() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                edtTime.setText(selectedTime)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }


    private fun saveAppointment() {
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()

        if (name.isEmpty() || selectedDateMillis == 0L || selectedTime.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            //kiem tra benh nhan cu
            var patienId: Long
            val existingPatient = patientDao.getPatientByPhone(phone)
            if (existingPatient != null) {
                patienId = existingPatient.patientId
                if (existingPatient.fullName != name) {
                    withContext(Dispatchers.Main) {
                        showOptionExistingPatient(existingPatient)
                    }

                }
            } else {
                if (name.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập tên bệnh nhân", Toast.LENGTH_SHORT)
                        .show()
                    return@launch
                }
                val mrn = MrnGenerator.generateUnique(patientDao)
                val newPatientId = withContext(Dispatchers.IO) {
                    patientDao.insertPatient(
                        Patient(
                            fullName = name,
                            phoneNumber = phone,
                            medicalRecordNumber = mrn,
                            dateOfBirth = 0L,
                            gender = "ĐẶT LỊCH",
                            address = null
                        )
                    )
                }
                createAppointment(newPatientId, name)
            }


        }
    }

    private fun showOptionExistingPatient(existPatient: Patient) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Số điện thoại đã tồn tại")
            .setMessage("Tìm thấy bệnh nhân: ${existPatient.fullName}\nBạn có muốn dùng hồ sơ cũ này để đặt lịch không?")
            .setPositiveButton("Dùng hồ sơ cũ") { _, _ ->
                edtName.setText(existPatient.fullName)
                edtName.isEnabled = false


            }
            .setNegativeButton("Kiểm tra lại SĐT") { dialog, _ ->
                dialog.dismiss()
                edtPhone.requestFocus()
            }
            .show()

    }

    private fun createAppointment(patientId: Long, patientName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val recordId = medicalRecordDao.insert(
                MedicalRecord(
                    patientId = patientId,
                    diagnosis = "ĐẶT LỊCH",
                    symptoms = "Chưa khám",
                    diseaseType = "Chưa khám",
                    examinationDate = selectedDateMillis,
                    doctorId = SessionManager.getDoctorId(requireContext()),
                    notes = "Hồ sơ tạo khi đặt lịch"
                )
            )

            appointmentDao.insert(
                Appointment(
                    recordId = recordId,
                    patientId = patientId,
                    appointmentDate = selectedDateMillis,
                    appointmentTime = selectedTime,
                    location = "Phòng khám",
                    doctorId = SessionManager.getDoctorId(requireContext()),
                    notes = "Đặt lịch trước",
                    status = "SCHEDULED"
                )
            )


            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Đã thêm lịch hẹn cho $patientName",
                    Toast.LENGTH_SHORT
                ).show()
                onAdded.invoke()
                dismiss()
            }
        }
    }
}