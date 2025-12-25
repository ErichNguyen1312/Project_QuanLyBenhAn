package com.example.projectqlbenhan.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateTimeUtils {

    fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun isPastTime(timeSlot: String): Boolean {
        try {
            val now = Calendar.getInstance()

            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Định dạng 12h có AM/PM
            val dateSlot = sdf.parse(timeSlot) ?: return false

            val slotCalendar = Calendar.getInstance()
            slotCalendar.time = dateSlot

            slotCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
            slotCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
            slotCalendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

            return slotCalendar.before(now)

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun isToday(selectedDateMillis: Long): Boolean {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

        return today.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR)
    }
}
