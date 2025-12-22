package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.ui.home.HomeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helper_ThongBaoTaiKham {

    fun notifyIfNeeded(context: Context, appointments: List<Appointment>) {
        if (appointments.isEmpty()) return

        ThongBaoTaiKham.createChannel(context)

        // Check permission Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Duyệt qua danh sách và thông báo
        for (app in appointments) {
            showNotification(context, app)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, ap: Appointment) {
        val intent = Intent(context, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            ap.appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Format giờ từ Long
        val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ap.appointmentDate))

        val notification = NotificationCompat.Builder(context, ThongBaoTaiKham.CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)
            .setContentTitle("Lịch tái khám hôm nay")
            .setContentText("Bạn có lịch hẹn lúc $timeString")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(ap.appointmentId.toInt(), notification)
    }
}