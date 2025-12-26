package com.example.projectqlbenhan.utils

import com.example.projectqlbenhan.MedicalRecordDatabase
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
            try {
                // Kiểm tra nếu đã có dữ liệu Account thì không seed lại
                if (db.accountDao().countAccounts() > 0) return@withContext
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val defaultPassHash = hashPassword("123456")

            // ==========================================
            // 1. TẠO TÀI KHOẢN ADMIN
            // ==========================================
            db.accountDao().insertAccount(
                Account(username = "admin", passwordHash = defaultPassHash, role = "ADMIN")
            )

            // ==========================================
            // 2. TẠO BÁC SĨ (5 Bác sĩ: 1 cũ + 4 mới)
            // ==========================================

            // --- Bác sĩ 1 (Cũ) ---
            val docAccId1 = db.accountDao().insertAccount(
                Account(username = "doctor01", passwordHash = defaultPassHash, role = "DOCTOR")
            )
            val doctor1Id = db.doctorDao().insert(
                Doctor(
                    accountId = docAccId1,
                    fullName = "BS. Nguyễn Văn Đức",
                    specialization = "Nội Khoa",
                    description = "Chuyên khoa tiêu hóa, gan mật"
                )
            )

            // --- Bác sĩ 2 (Mới - Nhi Khoa) ---
            val docAccId2 = db.accountDao().insertAccount(
                Account(username = "doctor02", passwordHash = defaultPassHash, role = "DOCTOR")
            )
            db.doctorDao().insert(
                Doctor(
                    accountId = docAccId2,
                    fullName = "BS. Trần Thị Mai",
                    specialization = "Nhi Khoa",
                    description = "Chuyên điều trị bệnh lý ở trẻ em, hô hấp nhi"
                )
            )

            // --- Bác sĩ 3 (Mới - Tim Mạch) ---
            val docAccId3 = db.accountDao().insertAccount(
                Account(username = "doctor03", passwordHash = defaultPassHash, role = "DOCTOR")
            )
            db.doctorDao().insert(
                Doctor(
                    accountId = docAccId3,
                    fullName = "BS. Lê Quốc Tuấn",
                    specialization = "Tim Mạch",
                    description = "Chuyên gia về huyết áp và bệnh lý mạch vành"
                )
            )

            // --- Bác sĩ 4 (Mới - Da Liễu) ---
            val docAccId4 = db.accountDao().insertAccount(
                Account(username = "doctor04", passwordHash = defaultPassHash, role = "DOCTOR")
            )
            db.doctorDao().insert(
                Doctor(
                    accountId = docAccId4,
                    fullName = "BS. Phạm Thị Dung",
                    specialization = "Da Liễu",
                    description = "Điều trị mụn, dị ứng, thẩm mỹ da"
                )
            )

            // --- Bác sĩ 5 (Mới - Chấn Thương Chỉnh Hình) ---
            val docAccId5 = db.accountDao().insertAccount(
                Account(username = "doctor05", passwordHash = defaultPassHash, role = "DOCTOR")
            )
            db.doctorDao().insert(
                Doctor(
                    accountId = docAccId5,
                    fullName = "BS. Hoàng Văn Nam",
                    specialization = "Chấn Thương Chỉnh Hình",
                    description = "Phẫu thuật xương khớp, chấn thương thể thao"
                )
            )


            // ==========================================
            // 3. TẠO BỆNH NHÂN (6 BN: 2 cũ + 4 mới)
            // ==========================================

            // --- BN 1 (Cũ - Có tài khoản) ---
            val patAccId1 = db.accountDao().insertAccount(
                Account(username = "0912345678", passwordHash = defaultPassHash, role = "PATIENT")
            )
            val p1Id = db.patientDao().insertPatient(
                Patient(
                    accountId = patAccId1,
                    fullName = "Trần Thị Lan",
                    medicalRecordNumber = "BN-001",
                    dateOfBirth = getDob(1995),
                    gender = "Nữ",
                    phoneNumber = "0912345678",
                    address = "Quận 1, TP.HCM"
                )
            )

            // --- BN 2 (Cũ - Vãng lai) ---
            val p2Id = db.patientDao().insertPatient(
                Patient(
                    accountId = null,
                    fullName = "Lê Văn Tèo (Vãng lai)",
                    medicalRecordNumber = "BN-002",
                    dateOfBirth = getDob(2000),
                    gender = "Nam",
                    phoneNumber = "0987654321",
                    address = "Quận 5, TP.HCM"
                )
            )

            // --- BN 3 (Mới - Có tài khoản - 0909000111) ---
            val patAccId3 = db.accountDao().insertAccount(
                Account(username = "0909000111", passwordHash = defaultPassHash, role = "PATIENT")
            )
            db.patientDao().insertPatient(
                Patient(
                    accountId = patAccId3,
                    fullName = "Nguyễn Văn An",
                    medicalRecordNumber = "BN-003",
                    dateOfBirth = getDob(1988),
                    gender = "Nam",
                    phoneNumber = "0909000111",
                    address = "Thủ Đức, TP.HCM"
                )
            )

            // --- BN 4 (Mới - Có tài khoản - 0909000222) ---
            val patAccId4 = db.accountDao().insertAccount(
                Account(username = "0909000222", passwordHash = defaultPassHash, role = "PATIENT")
            )
            db.patientDao().insertPatient(
                Patient(
                    accountId = patAccId4,
                    fullName = "Phạm Thị Bích",
                    medicalRecordNumber = "BN-004",
                    dateOfBirth = getDob(1992),
                    gender = "Nữ",
                    phoneNumber = "0909000222",
                    address = "Bình Thạnh, TP.HCM"
                )
            )

            // --- BN 5 (Mới - Vãng lai/Cấp cứu) ---
            db.patientDao().insertPatient(
                Patient(
                    accountId = null,
                    fullName = "Trần Văn Cường (Vãng lai)",
                    medicalRecordNumber = "BN-005",
                    dateOfBirth = getDob(1975),
                    gender = "Nam",
                    phoneNumber = "0918111222",
                    address = "Quận 3, TP.HCM"
                )
            )

            // --- BN 6 (Mới - Vãng lai/Người già không dùng app) ---
            db.patientDao().insertPatient(
                Patient(
                    accountId = null,
                    fullName = "Lê Thị Dần (Vãng lai)",
                    medicalRecordNumber = "BN-006",
                    dateOfBirth = getDob(1950),
                    gender = "Nữ",
                    phoneNumber = "0918333444",
                    address = "Quận 10, TP.HCM"
                )
            )


            // ==========================================
            // 4. DATA MẪU: LỊCH HẸN & BỆNH ÁN (Giữ nguyên mẫu cũ + update nếu cần)
            // ==========================================

            // Lịch hẹn 1: Đã hoàn thành (của Lan - BN001) với Bác sĩ 1
            val appt1Id = db.appointmentDao().insert(
                Appointment(
                    patientId = p1Id,
                    doctorId = doctor1Id,
                    appointmentDate = getDateOffset(-2),
                    status = "COMPLETED",
                    reason = "Đau bụng kéo dài"
                )
            )

            // Lịch hẹn 2: Sắp tới (của Tèo - BN002) với Bác sĩ 1
            db.appointmentDao().insert(
                Appointment(
                    patientId = p2Id,
                    doctorId = doctor1Id,
                    appointmentDate = getDateOffset(1),
                    status = "SCHEDULED",
                    reason = "Tái khám định kỳ"
                )
            )

            // Bệnh án 1 (Khớp với Lịch hẹn 1)
            val record1 = MedicalRecord(
                patientId = p1Id,
                doctorId = doctor1Id,
                appointmentId = appt1Id,
                diagnosis = "Viêm dạ dày cấp",
                symptoms = "Đau thượng vị, buồn nôn, chán ăn",
                diseaseType = "Nội khoa",
                doctorNotes = "Hạn chế đồ chua cay, không thức khuya",
                doctorAdvice = "Uống thuốc đúng giờ, tái khám sau 1 tuần",
                examinationDate = getDateOffset(-2)
            )
            val record1Id = db.medicalRecordDao().insert(record1)

            // Bệnh án 2 (Cấp cứu/Vãng lai - Không có lịch hẹn)
            val record2 = MedicalRecord(
                patientId = p2Id,
                doctorId = doctor1Id,
                appointmentId = null,
                diagnosis = "Dị ứng thực phẩm",
                symptoms = "Nổi mề đay, ngứa toàn thân",
                diseaseType = "Da liễu",
                doctorNotes = "Đã tiêm thuốc chống dị ứng tại chỗ",
                doctorAdvice = "Kiêng hải sản, thịt bò trong 3 ngày",
                examinationDate = getDateOffset(-5)
            )
            val record2Id = db.medicalRecordDao().insert(record2)


            // ==========================================
            // 5. TẠO ĐƠN THUỐC
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


    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getDob(year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, 0, 1)
        return cal.timeInMillis
    }

    private fun getDateOffset(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }
}