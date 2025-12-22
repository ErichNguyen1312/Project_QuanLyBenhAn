package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(
        context: Context,
        appointmentId: Long,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, Receiver_ThongBaoTaiKham::class.java).apply {
            // Truyền timestamp để Receiver tự format giờ hiển thị
            putExtra("appointmentId", appointmentId)
            putExtra("timestamp", triggerAtMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Hủy alarm cũ nếu có (để tránh trùng lặp khi update lịch)
        alarmManager.cancel(pendingIntent)

        // Đặt alarm mới
        if (triggerAtMillis > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    // Hàm hủy báo thức (dùng khi xóa lịch hẹn)
    fun cancel(context: Context, appointmentId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, Receiver_ThongBaoTaiKham::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}