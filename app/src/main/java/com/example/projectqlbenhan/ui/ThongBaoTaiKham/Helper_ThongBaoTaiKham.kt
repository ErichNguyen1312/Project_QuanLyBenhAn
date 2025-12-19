package com.example.projectqlbenhan.ui.ThongBaoTaiKham

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.ui.home.HomeActivity
import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.work.WorkManager
import com.example.projectqlbenhan.entity.appointment.AppointmentWithPatient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helper_ThongBaoTaiKham {

    fun notifyIfNeeded(context: Context, appointments: List<Appointment>) {
        if (appointments.isEmpty()) return

        ThongBaoTaiKham.createChannel(context)

        // Check permission Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val prefs = context.getSharedPreferences("notify_pref", Context.MODE_PRIVATE)
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date())
            .toLong()

        if (prefs.getLong("last_notify_today", 0L) == todayKey) return

        appointments.forEach { ap ->
            showNotification(context, ap)
        }

        prefs.edit()
            .putLong("last_notify_today", todayKey)
            .apply()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyNearestIfNeeded(
        context: Context,
        appointment: Appointment
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val prefs = context.getSharedPreferences("tb_taikham", Context.MODE_PRIVATE)

        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastId = prefs.getLong("last_notified_id", -1)
        val lastDate = prefs.getString("last_date", "")

        if (
            appointment.appointmentId == lastId &&
            today == lastDate
        ) return

        ThongBaoTaiKham.createChannel(context)

        val intent = Intent(context, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            ThongBaoTaiKham.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.outline_circle_notifications_24)
            .setContentTitle("Lịch tái khám hôm nay")
            .setContentText(
                "Sắp tới lúc: ${appointment.appointmentTime}"
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)

        prefs.edit()
            .putLong("last_notified_id", appointment.appointmentId)
            .putString("last_date", today)
            .apply()
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

        val notification = NotificationCompat.Builder(context, ThongBaoTaiKham.CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)
            .setContentTitle("Nhắc lịch tái khám hôm nay")
            .setContentText("Bệnh nhân có lịch tái khám lúc ${ap.appointmentTime}")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(ap.appointmentId.toInt(), notification)
    }
}


