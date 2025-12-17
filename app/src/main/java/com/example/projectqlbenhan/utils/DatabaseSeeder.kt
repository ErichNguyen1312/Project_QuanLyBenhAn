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

            val patients = seedPatients(db)
            val records = seedMedicalRecords(db, patients)
            seedPrescriptions(db, records)
            seedAppointments(db, records)
        }
    }

    // =========================
    // 1. PATIENT (10)
    // =========================
    private suspend fun seedPatients(db: MedicalRecordDatabase): List<Patient> {

        val names = listOf(
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Văn Cường",
            "Phạm Thị Dung", "Hoàng Văn Đức", "Võ Thị Hạnh",
            "Đặng Văn Khoa", "Bùi Thị Lan", "Phan Văn Minh", "Ngô Thị Nga"
        )

        names.forEachIndexed { index, name ->
            db.patientDao().insertPatient(
                Patient(
                    fullName = name,
                    gender = if (index % 2 == 0) "Nam" else "Nữ",
                    dateOfBirth = randomDob(20, 70),
                    phoneNumber = "09${(10000000..99999999).random()}",
                    address = "TP.HCM",
                    medicalRecordNumber = "HS-${System.currentTimeMillis()}-$index"
                )
            )
        }

        return db.patientDao().getAll()
    }

    // =========================
    // 2. MEDICAL RECORD (1–2 / patient)
    // =========================
    private suspend fun seedMedicalRecords(
        db: MedicalRecordDatabase,
        patients: List<Patient>
    ): List<MedicalRecord> {

        val diagnoses = listOf(
            "Cảm cúm", "Viêm họng", "Đau dạ dày",
            "Tăng huyết áp", "Viêm xoang",
            "Mất ngủ", "Rối loạn tiêu hóa"
        )

        patients.forEach { patient ->
            repeat(Random.nextInt(1, 3)) {
                db.medicalRecordDao().insert(
                    MedicalRecord(
                        patientId = patient.patientId,
                        diagnosis = diagnoses.random(),
                        symptoms = "Mệt mỏi, đau đầu",
                        diseaseType = "Nội khoa",
                        examinationDate = randomPastDate(15),
                        doctorId = 1L,
                        notes = "Theo dõi thêm"
                    )
                )
            }
        }

        return db.medicalRecordDao().getAll()
    }

    // =========================
    // 3. PRESCRIPTION (2–5 / record)
    // =========================
    private suspend fun seedPrescriptions(
        db: MedicalRecordDatabase,
        records: List<MedicalRecord>
    ) {
        val medicines = listOf(
            "Paracetamol", "Amoxicillin", "Vitamin C",
            "Ibuprofen", "Omeprazole", "Alpha Choay",
            "Panadol", "Becozyme"
        )

        records.forEach { record ->
            repeat(Random.nextInt(2, 6)) {
                db.prescriptionDao().insert(
                    Prescription(
                        recordId = record.recordId,
                        medicineName = medicines.random(),
                        dosage = "1–2 viên/lần",
                        frequency = "${Random.nextInt(2,4)} lần/ngày",
                        durationDays = Random.nextInt(3, 10),
                        instructions = "Uống sau ăn"
                    )
                )
            }
        }
    }

    // =========================
    // 4. APPOINTMENT (1–3 / record)
    // =========================
    private suspend fun seedAppointments(
        db: MedicalRecordDatabase,
        records: List<MedicalRecord>
    ) {

        val locations = listOf(
            "Phòng khám A", "Phòng khám B",
            "Bệnh viện Quận 1", "Bệnh viện Quận 7"
        )

        records.forEach { record ->
            repeat(Random.nextInt(1, 4)) {
                db.appointmentDao().insert(
                    Appointment(
                        recordId = record.recordId,
                        patientId = record.patientId,
                        doctorId = 1L,
                        appointmentDate = randomFutureDate(30),
                        appointmentTime = "${Random.nextInt(8, 17)}:00",
                        location = locations.random(),
                        notes = "Tái khám theo chỉ định",
                        reminderEnabled = true
                    )
                )
            }
        }
    }

    // =========================
    // UTIL DATE
    // =========================
    private fun randomDob(minAge: Int, maxAge: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -Random.nextInt(minAge, maxAge))
        return cal.timeInMillis
    }

    private fun randomPastDate(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -Random.nextInt(1, days))
        return cal.timeInMillis
    }

    private fun randomFutureDate(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, Random.nextInt(1, days))
        return cal.timeInMillis
    }
}
