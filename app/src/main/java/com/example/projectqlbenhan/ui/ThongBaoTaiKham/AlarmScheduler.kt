package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlarmScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(
        context: Context,
        appointmentId: Long,
        triggerAtMillis: Long,
        timeText: String
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, Receiver_ThongBaoTaiKham::class.java).apply {
            putExtra("time", timeText)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}