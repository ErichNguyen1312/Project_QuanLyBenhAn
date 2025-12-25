package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.ui.home.HomeActivity // Hoặc Activity Main của bạn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Receiver_ThongBaoTaiKham : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Kiểm tra quyền trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Tạo channel nếu chưa có
        ThongBaoTaiKham.createChannel(context)

        // Lấy dữ liệu
        val timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis())
        val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        val appointmentId = intent.getLongExtra("appointmentId", 0)

        // Intent mở app khi bấm vào thông báo
        val openIntent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.toInt(), // Dùng ID lịch hẹn để tạo request code unique
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ThongBaoTaiKham.CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)
            .setContentTitle("Nhắc lịch tái khám")
            .setContentText("Bạn có lịch hẹn tái khám lúc $timeString hôm nay.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(appointmentId.toInt(), notification)
    }
}