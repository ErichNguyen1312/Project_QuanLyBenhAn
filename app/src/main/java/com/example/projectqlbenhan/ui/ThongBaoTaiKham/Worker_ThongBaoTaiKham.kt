package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projectqlbenhan.MedicalRecordDatabase
import java.util.Calendar

class Worker_ThongBaoTaiKham(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = MedicalRecordDatabase.getDatabase(applicationContext)

            val calendar = Calendar.getInstance()

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startToday = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endToday = calendar.timeInMillis

            val todayAppointments = db.appointmentDao().getTodayUpcomingAppointments(
                startToday,
                endToday
            )

            Helper_ThongBaoTaiKham.notifyIfNeeded(
                applicationContext,
                todayAppointments
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}