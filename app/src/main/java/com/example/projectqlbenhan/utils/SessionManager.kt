package com.example.projectqlbenhan.utils

import android.content.Context

object SessionManager {


    private const val PREF_NAME = "doctor_session"
    private const val KEY_DOCTOR_ID = "doctor_id"
    private const val KEY_DOCTOR_NAME = "doctor_name"

//    luu session dang nhap
    fun saveDoctorSession(
        context: Context,
        doctorId: Long,
        doctorName: String
    ) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit()
            .putLong(KEY_DOCTOR_ID, doctorId)
            .putString(KEY_DOCTOR_NAME, doctorName)
            .apply()
    }

//    lay doctor dang nhap
    fun getDoctorId(context: Context): Long {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getLong(KEY_DOCTOR_ID, -1)
    }

    fun getDoctorName(context: Context): String? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_DOCTOR_NAME, null)
    }

//    check login
    fun isLoggedIn(context: Context): Boolean {
        return getDoctorId(context) != -1L
    }

//    logout, xoa session
    fun logout(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().clear().apply()
    }

//    clear khi logout
fun clear(context: Context) {
    context.getSharedPreferences("doctor_session", Context.MODE_PRIVATE)
        .edit().clear().apply()
}
}