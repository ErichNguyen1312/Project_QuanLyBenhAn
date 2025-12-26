package com.example.projectqlbenhan.utils

import com.example.projectqlbenhan.database.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.account.Account
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Calendar

object DatabaseSeeder {

    suspend fun seedIfNeeded(db: MedicalRecordDatabase) {
        withContext(Dispatchers.IO) {
            // 1. Kiểm tra: Nếu đã có tài khoản thì coi như đã seed, dừng lại
            try {
                if (db.accountDao().countAccounts() > 0) return@withContext
            } catch (e: Exception) {
                // Nếu chưa có hàm countAccounts trong DAO, bỏ qua lỗi để chạy tiếp
            }

            val defaultPassHash = hashPassword("123456")

            // ==========================================
            // 2. TẠO ACCOUNT & PROFILE
            // ==========================================

            // --- A. ADMIN ---
            db.accountDao().insertAccount(
                Account(username = "admin", passwordHash = defaultPassHash, role = "ADMIN")
            )

            // --- B. DOCTOR (Bác sĩ) ---
            val docAccId = db.accountDao().insertAccount(
                Account(username = "bacsi1", passwordHash = defaultPassHash, role = "DOCTOR")
            )
            // Tạo Profile Bác sĩ liên kết với Account
            val doctor = Doctor(
                accountId = docAccId,
                fullName = "BS. Nguyễn Văn Đức",
                specialization = "Nội Khoa",
                description = "Chuyên khoa tiêu hóa, gan mật"
            )
            val savedDoctorId = db.doctorDao().insert(doctor)


            // --- C. PATIENT 1 (Bệnh nhân có tài khoản) ---
            val patAccId1 = db.accountDao().insertAccount(
                Account(username = "benhnhan1", passwordHash = defaultPassHash, role = "PATIENT")
            )
            val p1 = Patient(
                accountId = patAccId1,
                fullName = "Trần Thị Lan",
                medicalRecordNumber = "BN-001", // Mã hồ sơ bắt buộc
                dateOfBirth = getDob(1995),
                gender = "Nữ",
                phoneNumber = "0912345678",
                address = "Quận 1, TP.HCM"
            )
            val p1Id = db.patientDao().insertPatient(p1)

            // --- D. PATIENT 2 (Khách vãng lai - Không có Account) ---
            val p2 = Patient(
                accountId = null, // Vãng lai -> null
                fullName = "Lê Văn Tèo (Vãng lai)",
                medicalRecordNumber = "BN-002",
                dateOfBirth = getDob(2000),
                gender = "Nam",
                phoneNumber = "0987654321",
                address = "Quận 5, TP.HCM"
            )
            val p2Id = db.patientDao().insertPatient(p2)


            // ==========================================
            // 3. TẠO LỊCH HẸN (APPOINTMENT)
            // ==========================================

            // Lịch hẹn 1: Đã hoàn thành (của Lan - BN001)
            val appt1Id = db.appointmentDao().insert(
                Appointment(
                    patientId = p1Id,
                    doctorId = savedDoctorId,
                    appointmentDate = getDateOffset(-2), // 2 ngày trước
                    status = "COMPLETED",
                    reason = "Đau bụng kéo dài"
                )
            )

            // Lịch hẹn 2: Sắp tới (của Tèo - BN002)
            db.appointmentDao().insert(
                Appointment(
                    patientId = p2Id,
                    doctorId = savedDoctorId,
                    appointmentDate = getDateOffset(1), // Ngày mai
                    status = "SCHEDULED",
                    reason = "Tái khám định kỳ"
                )
            )

            // ==========================================
            // 4. TẠO BỆNH ÁN (MEDICAL RECORD)
            // ==========================================

            // Bệnh án 1 (Khớp với Lịch hẹn 1)
            val record1 = MedicalRecord(
                patientId = p1Id,
                doctorId = savedDoctorId,
                appointmentId = appt1Id, // Link với lịch hẹn
                diagnosis = "Viêm dạ dày cấp",
                symptoms = "Đau thượng vị, buồn nôn, chán ăn",
                diseaseType = "Nội khoa", // Khớp với trường mới
                doctorNotes = "Hạn chế đồ chua cay, không thức khuya",
                doctorAdvice = "Uống thuốc đúng giờ, tái khám sau 1 tuần",
                examinationDate = getDateOffset(-2)
            )
            val record1Id = db.medicalRecordDao().insert(record1)

            // Bệnh án 2 (Cấp cứu/Vãng lai - Không có lịch hẹn)
            val record2 = MedicalRecord(
                patientId = p2Id,
                doctorId = savedDoctorId,
                appointmentId = null, // Không có lịch trước
                diagnosis = "Dị ứng thực phẩm",
                symptoms = "Nổi mề đay, ngứa toàn thân",
                diseaseType = "Da liễu",
                doctorNotes = "Đã tiêm thuốc chống dị ứng tại chỗ",
                doctorAdvice = "Kiêng hải sản, thịt bò trong 3 ngày",
                examinationDate = getDateOffset(-5) // 5 ngày trước
            )
            val record2Id = db.medicalRecordDao().insert(record2)


            // ==========================================
            // 5. TẠO ĐƠN THUỐC (PRESCRIPTION ITEMS)
            // ==========================================

            // Thuốc cho Record 1
            val items1 = listOf(
                PrescriptionItem(
                    recordId = record1Id,
                    medicineName = "Omeprazole 20mg",
                    quantity = 14,
                    unit = "Viên",
                    dosage = "Sáng 1 viên trước ăn 30p",
                    instruction = "Uống khi đói" // ⭐ Bổ sung tham số để fix lỗi
                ),
                PrescriptionItem(
                    recordId = record1Id,
                    medicineName = "Phosphalugel",
                    quantity = 10,
                    unit = "Gói",
                    dosage = "Sáng 1, Chiều 1",
                    instruction = "Uống sau khi ăn" // ⭐ Bổ sung tham số để fix lỗi
                )
            )
            db.prescriptionItemDao().insertPrescriptionItems(items1)

            // Thuốc cho Record 2
            val items2 = listOf(
                PrescriptionItem(
                    recordId = record2Id,
                    medicineName = "Loratadin 10mg",
                    quantity = 5,
                    unit = "Viên",
                    dosage = "Sáng 1 viên sau ăn",
                    instruction = "Tránh dùng thuốc khi vận hành máy móc" // ⭐ Bổ sung tham số để fix lỗi
                )
            )
            db.prescriptionItemDao().insertPrescriptionItems(items2)
        }
    }

    // =====================
    // UTILS HELPERS
    // =====================

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Lấy timestamp từ năm sinh (VD: 1995 -> timestamp)
    private fun getDob(year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, 0, 1)
        return cal.timeInMillis
    }

    // Lấy timestamp +/- số ngày so với hiện tại
    private fun getDateOffset(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }
}