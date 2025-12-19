package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projectqlbenhan.MedicalRecordDatabase
import com.example.projectqlbenhan.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Worker_ThongBaoTaiKham(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val db = MedicalRecordDatabase.getDatabase(applicationContext)
        val appointmentDao = db.appointmentDao()

        val startToday = DateTimeUtils.getStartOfDay()
        val endToday = DateTimeUtils.getEndOfDay()

        val nowTime = SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(Date())

        val todayAppointments =
            appointmentDao.getTodayUpcomingAppointments(
                startToday,
                endToday,
                nowTime.toString()
            )

        Helper_ThongBaoTaiKham.notifyIfNeeded(
            applicationContext,
            todayAppointments
        )

        return Result.success()
    }
}
