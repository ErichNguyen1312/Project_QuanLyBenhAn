package com.example.projectqlbenhan.utils

import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.entity.Prescription
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.random.Random

object DatabaseSeeder {

    suspend fun seedIfNeeded(db: MedicalRecordDatabase) {
        withContext(Dispatchers.IO) {

            if (db.patientDao().countPatients() > 0) return@withContext

            // =====================
            // 1. PATIENT (3)
            // =====================
            val p1 = Patient(
                fullName = "Nguyễn Văn An",
                gender = "Nam",
                dateOfBirth = getDob(1998),
                phoneNumber = "0900000001",
                address = "TP.HCM",
                medicalRecordNumber = "HS001"
            )

            val p2 = Patient(
                fullName = "Trần Thị Bình",
                gender = "Nữ",
                dateOfBirth = getDob(1996),
                phoneNumber = "0900000002",
                address = "TP.HCM",
                medicalRecordNumber = "HS002"
            )

            val p3 = Patient(
                fullName = "Lê Văn Cường",
                gender = "Nam",
                dateOfBirth = getDob(1994),
                phoneNumber = "0900000003",
                address = "TP.HCM",
                medicalRecordNumber = "HS003"
            )

            db.patientDao().insertPatient(p1)
            db.patientDao().insertPatient(p2)
            db.patientDao().insertPatient(p3)

            val patients = db.patientDao().getAll()

            // =====================
            // 2. MEDICAL RECORD (3)
            // =====================
            val r1 = MedicalRecord(
                patientId = patients[0].patientId,
                diagnosis = "Cảm cúm",
                symptoms = "Sốt nhẹ",
                diseaseType = "Nội khoa",
                examinationDate = getDateOffset(-2),
                doctorId = 1L,
                notes = "Theo dõi tại nhà"
            )

            val r2 = MedicalRecord(
                patientId = patients[1].patientId,
                diagnosis = "Đau dạ dày",
                symptoms = "Đau thượng vị",
                diseaseType = "Tiêu hóa",
                examinationDate = getDateOffset(-1),
                doctorId = 1L,
                notes = "Ăn uống điều độ"
            )

            val r3 = MedicalRecord(
                patientId = patients[2].patientId,
                diagnosis = "Viêm họng",
                symptoms = "Đau rát họng",
                diseaseType = "Tai mũi họng",
                examinationDate = getDateOffset(0),
                doctorId = 1L,
                notes = "Uống đủ nước"
            )

            db.medicalRecordDao().insert(r1)
            db.medicalRecordDao().insert(r2)
            db.medicalRecordDao().insert(r3)

            val records = db.medicalRecordDao().getAll()

            // =====================
            // 3. APPOINTMENT (3 ngày khác nhau)
            // =====================
            db.appointmentDao().insert(
                Appointment(
                    recordId = records[0].recordId,
                    patientId = records[0].patientId,
                    doctorId = 1L,
                    appointmentDate = getDateOffset(1),
                    appointmentTime = "9:00 AM",
                    location = "Phòng khám A",
                    notes = "Tái khám cảm cúm",
                    reminderEnabled = true
                )
            )

            db.appointmentDao().insert(
                Appointment(
                    recordId = records[1].recordId,
                    patientId = records[1].patientId,
                    doctorId = 1L,
                    appointmentDate = getDateOffset(2),
                    appointmentTime = "10:00 AM",
                    location = "Phòng khám B",
                    notes = "Tái khám dạ dày",
                    reminderEnabled = true
                )
            )

            db.appointmentDao().insert(
                Appointment(
                    recordId = records[2].recordId,
                    patientId = records[2].patientId,
                    doctorId = 1L,
                    appointmentDate = getDateOffset(3),
                    appointmentTime = "2:00 PM",
                    location = "Bệnh viện Quận 1",
                    notes = "Tái khám họng",
                    reminderEnabled = true
                )
            )

            // =====================
            // 4. PRESCRIPTION (3 KHÁC NHAU)
            // =====================
            db.prescriptionDao().insert(
                Prescription(
                    recordId = records[0].recordId,
                    medicineName = "Paracetamol",
                    dosage = "1 viên",
                    frequency = "3 lần/ngày",
                    durationDays = 5,
                    instructions = "Uống sau ăn"
                )
            )

            db.prescriptionDao().insert(
                Prescription(
                    recordId = records[1].recordId,
                    medicineName = "Omeprazole",
                    dosage = "1 viên",
                    frequency = "1 lần/ngày",
                    durationDays = 14,
                    instructions = "Uống trước ăn sáng"
                )
            )

            db.prescriptionDao().insert(
                Prescription(
                    recordId = records[2].recordId,
                    medicineName = "Amoxicillin",
                    dosage = "2 viên",
                    frequency = "2 lần/ngày",
                    durationDays = 7,
                    instructions = "Uống đủ liều"
                )
            )
        }
    }

    // =====================
    // UTIL
    // =====================
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

//
//object DatabaseSeeder {
//
//    suspend fun seedIfNeeded(db: MedicalRecordDatabase) {
//        withContext(Dispatchers.IO) {
//            if (db.patientDao().countPatients() > 0) return@withContext
//
//            val patients = seedPatients(db)
//            val records = seedMedicalRecords(db, patients)
//            seedPrescriptions(db, records)
//            seedAppointments(db, records)
//        }
//    }
//
//    // =========================
//    // 1. PATIENT (10)
//    // =========================
//    private suspend fun seedPatients(db: MedicalRecordDatabase): List<Patient> {
//
//        val names = listOf(
//            "Nguyễn Phúc  An", "Trần Bình Trọng ", "Lê Văn Cường",
//            "Phạm Thị Dung", "Hoàng Văn Đức", "Võ Thị Hạnh",
//            "Đặng Văn Khoa", "Bùi Thị Lan", "Phan Văn Minh", "Ngô Thị Nga"
//        )
//
//        names.forEachIndexed { index, name ->
//            db.patientDao().insertPatient(
//                Patient(
//                    fullName = name,
//                    gender = if (index % 2 == 0) "Nam" else "Nữ",
//                    dateOfBirth = randomDob(20, 70),
//                    phoneNumber = "09${(10000000..99999999).random()}",
//                    address = "TP.HCM",
//                    medicalRecordNumber = "HS-${System.currentTimeMillis()}-$index"
//                )
//            )
//        }
//
//        return db.patientDao().getAll()
//    }
//
//    // =========================
//    // 2. MEDICAL RECORD (1–2 / patient)
//    // =========================
//    private suspend fun seedMedicalRecords(
//        db: MedicalRecordDatabase,
//        patients: List<Patient>
//    ): List<MedicalRecord> {
//
//        val diagnoses = listOf(
//            "Cảm cúm", "Viêm họng", "Đau dạ dày",
//            "Tăng huyết áp", "Viêm xoang",
//            "Mất ngủ", "Rối loạn tiêu hóa"
//        )
//
//        patients.forEach { patient ->
//            repeat(Random.nextInt(1, 3)) {
//                db.medicalRecordDao().insert(
//                    MedicalRecord(
//                        patientId = patient.patientId,
//                        diagnosis = diagnoses.random(),
//                        symptoms = "Mệt mỏi, đau đầu",
//                        diseaseType = "Nội khoa",
//                        examinationDate = randomPastDate(15),
//                        doctorId = 1L,
//                        notes = "Theo dõi thêm"
//                    )
//                )
//            }
//        }
//
//        return db.medicalRecordDao().getAll()
//    }
//
//    // =========================
//    // 3. PRESCRIPTION (2–5 / record)
//    // =========================
//    private suspend fun seedPrescriptions(
//        db: MedicalRecordDatabase,
//        records: List<MedicalRecord>
//    ) {
//        val medicines = listOf(
//            "Paracetamol", "Amoxicillin", "Vitamin C",
//            "Ibuprofen", "Omeprazole", "Alpha Choay",
//            "Panadol", "Becozyme"
//        )
//
//        records.forEach { record ->
//            repeat(Random.nextInt(2, 6)) {
//                db.prescriptionDao().insert(
//                    Prescription(
//                        recordId = record.recordId,
//                        medicineName = medicines.random(),
//                        dosage = "1–2 viên/lần",
//                        frequency = "${Random.nextInt(2,4)} lần/ngày",
//                        durationDays = Random.nextInt(3, 10),
//                        instructions = "Uống sau ăn"
//                    )
//                )
//            }
//        }
//    }
//
//    // =========================
//    // 4. APPOINTMENT (1–3 / record)
//    // =========================
//    private suspend fun seedAppointments(
//        db: MedicalRecordDatabase,
//        records: List<MedicalRecord>
//    ) {
//
//        val locations = listOf(
//            "Phòng khám A", "Phòng khám B",
//            "Bệnh viện Quận 1", "Bệnh viện Quận 7"
//        )
//
//        records.forEach { record ->
//            repeat(Random.nextInt(1, 4)) {
//                db.appointmentDao().insert(
//                    Appointment(
//                        recordId = record.recordId,
//                        patientId = record.patientId,
//                        doctorId = 1L,
//                        appointmentDate = randomFutureDate(30),
//                        appointmentTime = "${Random.nextInt(8, 17)}:00",
//                        location = locations.random(),
//                        notes = "Tái khám theo chỉ định",
//                        reminderEnabled = true
//                    )
//                )
//            }
//        }
//    }
//
//    // =========================
//    // UTIL DATE
//    // =========================
//    private fun randomDob(minAge: Int, maxAge: Int): Long {
//        val cal = Calendar.getInstance()
//        cal.add(Calendar.YEAR, -Random.nextInt(minAge, maxAge))
//        return cal.timeInMillis
//    }
//
//    private fun randomPastDate(days: Int): Long {
//        val cal = Calendar.getInstance()
//        cal.add(Calendar.DAY_OF_YEAR, -Random.nextInt(1, days))
//        return cal.timeInMillis
//    }
//
//    private fun randomFutureDate(days: Int): Long {
//        val cal = Calendar.getInstance()
//        cal.add(Calendar.DAY_OF_YEAR, Random.nextInt(1, days))
//        return cal.timeInMillis
//    }
//}
