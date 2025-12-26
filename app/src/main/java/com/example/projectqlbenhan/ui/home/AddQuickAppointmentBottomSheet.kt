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
import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.dao.accountDao.AccountDao
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

    // DAOs
    private lateinit var patientDao: PatientDao
    private lateinit var appointmentDao: AppointmentDao
    private lateinit var medicalRecordDao: MedicalRecordDao
    private lateinit var accountDao: AccountDao

    // Views
    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtDate: EditText
    private lateinit var edtTime: EditText
    private lateinit var btnSave: Button

    private val appointmentCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottomsheet_add_appointment, container, false)

        setControl(view)
        setEvent()

        return view
    }

    private fun setControl(view: View) {
        val db = MedicalRecordDatabase.getDatabase(requireContext())
        patientDao = db.patientDao()
        appointmentDao = db.appointmentDao()
        medicalRecordDao = db.medicalRecordDao()
        accountDao = db.accountDao()

        edtName = view.findViewById(R.id.edtName)
        edtPhone = view.findViewById(R.id.edtPhone)
        edtDate = view.findViewById(R.id.edtDate)
        edtTime = view.findViewById(R.id.edtTime)
        btnSave = view.findViewById(R.id.btnSave)

      // lấy mốc giờ để hiển thị thời gian hiện tại
        appointmentCalendar.set(Calendar.SECOND, 0)
        appointmentCalendar.set(Calendar.MILLISECOND, 0)

        updateDateTimeUI()
    }

    private fun setEvent() {
        edtDate.setOnClickListener { pickDate() }
        edtTime.setOnClickListener { pickTime() }
        btnSave.setOnClickListener { saveAppointment() }
    }

    // các hàm xử lý

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

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập Tên và SĐT", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val existingPatient = patientDao.getPatientByPhone(phone)

            if (existingPatient != null) {
                // Nếu tên khác với SĐT cũ -> Hỏi người dùng
                if (existingPatient.fullName != name) {
                    withContext(Dispatchers.Main) { showOptionExistingPatient(existingPatient) }
                } else {
                    createAppointment(existingPatient.patientId, existingPatient.fullName)
                }
            } else {
                // ⭐️ KHÁCH MỚI -> TẠO ACCOUNT + PATIENT
                withContext(Dispatchers.IO) {
                    // check account đã tồn tại chưa
                    var accountId: Long? = null

                    // Check xem SĐT này đã có tài khoản chưa
                    val isAccountExist = accountDao.isUsernameExist(phone)

                    if (!isAccountExist) {
                        // Tạo mới pass mặc định
                        val defaultPass = hashPassword("123456")
                        val newAccount = com.example.projectqlbenhan.entity.account.Account(
                            username = phone,
                            passwordHash = defaultPass,
                            role = "PATIENT"
                        )
                        accountId = accountDao.insertAccount(newAccount)
                    } else {
                        // Đã có account thì lấy ID account cũ link vào
                        val acc = accountDao.login(phone, hashPassword("123456"))
                        accountId = acc?.accountId
                    }

                    // tạo mã số hồ sơ tự động
                    val mrn = MrnGenerator.generateUnique(patientDao)
                    val newPatient = Patient(
                        accountId = accountId,
                        fullName = name,
                        phoneNumber = phone,
                        medicalRecordNumber = mrn,
                        dateOfBirth = 0L,
                        gender = "Khác",
                        address = null
                    )
                    val newPatientId = patientDao.insertPatient(newPatient)

                    // tiến hành đặt lịch
                    createAppointment(newPatientId, name)
                }
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
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
        val doctorId = SessionManager.getSpecificId(requireContext())

        if (doctorId == -1L) {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Lỗi phiên đăng nhập", Toast.LENGTH_SHORT).show()
            }
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            appointmentDao.insert(
                Appointment(
                    patientId = patientId,
                    doctorId = doctorId,
                    appointmentDate = finalTimestamp,
                    reason = "Đặt lịch nhanh",
                    status = "SCHEDULED"
                )
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Đã đặt lịch cho $patientName", Toast.LENGTH_SHORT).show()
                onAdded.invoke()
                dismiss()
            }
        }
    }
}