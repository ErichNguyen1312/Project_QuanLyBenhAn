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
import com.example.projectqlbenhan.entity.patient.Patient
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

    // Biến Calendar duy nhất để lưu ngày giờ
    private val appointmentCalendar = Calendar.getInstance()

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

        // Reset giây
        appointmentCalendar.set(Calendar.SECOND, 0)
        appointmentCalendar.set(Calendar.MILLISECOND, 0)

        updateDateTimeUI()

        edtDate.setOnClickListener { pickDate() }
        edtTime.setOnClickListener { pickTime() }
        btnSave.setOnClickListener { saveAppointment() }

        return view
    }

    private fun updateDateTimeUI() {
        val day = appointmentCalendar.get(Calendar.DAY_OF_MONTH)
        val month = appointmentCalendar.get(Calendar.MONTH) + 1
        val year = appointmentCalendar.get(Calendar.YEAR)
        edtDate.setText(String.format("%02d/%02d/%d", day, month, year))

        val hour = appointmentCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = appointmentCalendar.get(Calendar.MINUTE)
        edtTime.setText(String.format("%02d:%02d", hour, minute))
    }

    private fun pickDate() {
        val currentYear = appointmentCalendar.get(Calendar.YEAR)
        val currentMonth = appointmentCalendar.get(Calendar.MONTH)
        val currentDay = appointmentCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                appointmentCalendar.set(Calendar.YEAR, year)
                appointmentCalendar.set(Calendar.MONTH, month)
                appointmentCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateTimeUI()
            },
            currentYear, currentMonth, currentDay
        ).show()
    }

    private fun pickTime() {
        val currentHour = appointmentCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = appointmentCalendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                appointmentCalendar.set(Calendar.HOUR_OF_DAY, hour)
                appointmentCalendar.set(Calendar.MINUTE, minute)
                updateDateTimeUI()
            },
            currentHour, currentMinute, true
        ).show()
    }

    private fun saveAppointment() {
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập tên bệnh nhân", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val existingPatient = patientDao.getPatientByPhone(phone)
            if (existingPatient != null) {
                if (existingPatient.fullName != name) {
                    withContext(Dispatchers.Main) {
                        showOptionExistingPatient(existingPatient)
                    }
                } else {
                    createAppointment(existingPatient.patientId, existingPatient.fullName)
                }
            } else {
                val mrn = MrnGenerator.generateUnique(patientDao)
                val newPatientId = withContext(Dispatchers.IO) {
                    patientDao.insertPatient(
                        Patient(
                            accountId = null,
                            fullName = name,
                            phoneNumber = phone,
                            medicalRecordNumber = mrn,
                            dateOfBirth = 0L,
                            gender = "Khác",
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
            .setMessage("Tìm thấy hồ sơ: ${existPatient.fullName}\nDùng hồ sơ này để đặt lịch?")
            .setPositiveButton("Dùng hồ sơ cũ") { _, _ ->
                edtName.setText(existPatient.fullName)
                edtName.isEnabled = false
                createAppointment(existPatient.patientId, existPatient.fullName)
            }
            .setNegativeButton("Nhập lại SĐT") { dialog, _ ->
                dialog.dismiss()
                edtPhone.requestFocus()
            }
            .show()
    }

    private fun createAppointment(patientId: Long, patientName: String) {
        val finalTimestamp = appointmentCalendar.timeInMillis

        // Lưu ý: Nếu bạn đã update SessionManager mới thì dùng getSpecificId
        // Nếu chưa thì dùng getDoctorId như cũ. Ở đây mình dùng getSpecificId cho chuẩn hệ thống mới.
        val doctorId = SessionManager.getSpecificId(requireContext())

        // Check lỗi ID
        if (doctorId == -1L) {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Lỗi phiên đăng nhập", Toast.LENGTH_SHORT).show()
            }
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // ⭐️ CHỈ CẦN TẠO MỖI APPOINTMENT THÔI
            appointmentDao.insert(
                Appointment(
                    patientId = patientId,
                    doctorId = doctorId,
                    appointmentDate = finalTimestamp,
                    reason = "Đặt lịch nhanh",
                    status = "SCHEDULED" // Trạng thái chờ khám
                )
            )

            // ❌ ĐÃ XÓA đoạn tạo MedicalRecord "treo" ở đây.
            // Bệnh án sẽ được tạo ở màn hình "UpdateMedicalRecord" khi bác sĩ bắt đầu khám.

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Đã đặt lịch thành công cho $patientName",
                    Toast.LENGTH_SHORT
                ).show()
                onAdded.invoke()
                dismiss()
            }
        }
    }
}