package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ThongBaoTaiKham {
    const val CHANNEL_ID = "TAI_KHAM"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc lịch tái khám",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo lịch tái khám trong ngày"
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
