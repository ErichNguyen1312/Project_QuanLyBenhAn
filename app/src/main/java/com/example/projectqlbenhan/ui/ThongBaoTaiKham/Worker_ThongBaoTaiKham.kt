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

            // Tính toán khoảng thời gian "Hôm nay" (00:00:00 -> 23:59:59)
            val calendar = Calendar.getInstance()

            // Start of day
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startToday = calendar.timeInMillis

            // End of day
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endToday = calendar.timeInMillis

            // Gọi DAO (Lưu ý: Đảm bảo AppointmentDao có hàm getTodayUpcomingAppointments như đã thiết kế)
            val todayAppointments = db.appointmentDao().getTodayUpcomingAppointments(
                startToday,
                endToday
            )

            // Gửi thông báo
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