package com.example.projectqlbenhan.utils

import com.example.projectqlbenhan.dao.patient.PatientDao
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object MrnGenerator {

    private const val PREFIX = "HS-TDC"


    fun generateCandidate(): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date())

        val rand = Random.nextInt(100000, 999999)

        return "$PREFIX-$date-$rand"
    }

    suspend fun generateUnique(dao: PatientDao): String {
        while (true) {
            val mrn = generateCandidate()
            val exists = dao.countByMedicalRecordNumber(mrn)
            if (exists == 0) return mrn
        }
    }
}