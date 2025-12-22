package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ThongBaoTaiKham {
    const val CHANNEL_ID = "TAI_KHAM_CHANNEL"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nhắc lịch tái khám"
            val descriptionText = "Thông báo khi đến giờ hẹn tái khám"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}