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

            // 👉 Nếu đã có patient thì KHÔNG seed nữa
            if (db.patientDao().countPatients() > 0) return@withContext

            val patients = seedPatients(db)
            val records = seedMedicalRecords(db, patients)
            seedAppointments(db, records)
            seedPrescriptions(db, records)
        }
    }

    private suspend fun seedPatients(
        db: MedicalRecordDatabase
    ): List<Patient> {

        val names = listOf(
            "Nguyễn Văn An",
            "Trần Thị Bình",
            "Lê Văn Cường",
            "Phạm Thị Dung",
            "Hoàng Văn Đức"
        )

        names.forEachIndexed { index, name ->
            val patient = Patient(
                fullName = name,
                gender = if (index % 2 == 0) "Nam" else "Nữ",
                dateOfBirth = randomDob(25, 65),
                phoneNumber = "09${(10000000..99999999).random()}",
                address = "TP.HCM",
                medicalRecordNumber = "HS-${System.currentTimeMillis()}-$index"
            )
            db.patientDao().insertPatient(patient)
        }

        return db.patientDao().getAll()
    }

    private suspend fun seedMedicalRecords(
        db: MedicalRecordDatabase,
        patients: List<Patient>
    ): List<MedicalRecord> {

        val diagnoses = listOf(
            "Viêm họng cấp",
            "Đau dạ dày",
            "Cảm cúm",
            "Tăng huyết áp",
            "Viêm xoang"
        )

        patients.forEach { patient ->
            val record = MedicalRecord(
                patientId = patient.patientId,
                diagnosis = diagnoses.random(),
                symptoms = "Sốt nhẹ, mệt mỏi",
                diseaseType = "Nội khoa",
                examinationDate = System.currentTimeMillis(),
                doctorId = 1L, // 👈 gán tạm
                notes = "Theo dõi thêm"
            )
            db.medicalRecordDao().insert(record)
        }

        return db.medicalRecordDao().getAll()
    }

    private suspend fun seedAppointments(
        db: MedicalRecordDatabase,
        records: List<MedicalRecord>
    ) {
        val calendar = Calendar.getInstance()

        records.take(3).forEachIndexed { index, record ->
            val appointment = Appointment(
                recordId = record.recordId,
                patientId = 1L,
                doctorId = 1L,
                appointmentDate = calendar.timeInMillis,
                appointmentTime = "${9 + index}:00",
                location = "Phòng khám A",
                notes = "Tái khám",
                reminderEnabled = true
            )
            db.appointmentDao().insert(appointment)
        }
    }

    private suspend fun seedPrescriptions(
        db: MedicalRecordDatabase,
        records: List<MedicalRecord>
    ) {
        records.forEach { record ->
            val prescription = Prescription(
                recordId = record.recordId,

                medicineName = "Paracetamol",
                dosage = "2 viên/lần",
                frequency = "3 lần/ngày",
                durationDays = 5,
                instructions = "Uống sau ăn"
            )
            db.prescriptionDao().insert(prescription)
        }
    }

    private fun randomDob(minAge: Int, maxAge: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -Random.nextInt(minAge, maxAge))
        return cal.timeInMillis
    }
}


