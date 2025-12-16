package com.example.projectqlbenhan.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    // -------------------------------------------------------------------------
    // KHÓA CHO SESSION BÁC SĨ (Đăng nhập)
    // -------------------------------------------------------------------------
    private const val PREF_DOCTOR_SESSION_NAME = "doctor_session"
    private const val KEY_DOCTOR_ID = "doctor_id"
    private const val KEY_DOCTOR_NAME = "doctor_name"

    // -------------------------------------------------------------------------
    // KHÓA CHO SESSION BỆNH NHÂN ĐANG THAO TÁC (Dùng cho Thêm Đơn Thuốc)
    // -------------------------------------------------------------------------
    // Sử dụng tên session khác hoặc chung, ở đây dùng chung file SharedPreferences
    private const val PREF_APP_SESSION_NAME = "AppSession"
    private const val KEY_CURRENT_PATIENT_ID = "current_patient_id"
    private const val KEY_CURRENT_PATIENT_NAME = "current_patient_name"

    // Hàm tiện ích để lấy SharedPreferences (dùng cho Doctor session)
    private fun getDoctorSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_DOCTOR_SESSION_NAME, Context.MODE_PRIVATE)
    }

    // Hàm tiện ích để lấy SharedPreferences (dùng cho Patient session)
    private fun getPatientSharedPrefs(context: Context): SharedPreferences {
        // Sử dụng một tên khác để lưu Patient session, tránh xung đột logic
        return context.getSharedPreferences(PREF_APP_SESSION_NAME, Context.MODE_PRIVATE)
    }


    // -------------------------------------------------------------------------
    // LOGIC SESSION BÁC SĨ (Giữ nguyên)
    // -------------------------------------------------------------------------

    //    luu session dang nhap
    fun saveDoctorSession(
        context: Context,
        doctorId: Long,
        doctorName: String
    ) {
        getDoctorSharedPrefs(context).edit()
            .putLong(KEY_DOCTOR_ID, doctorId)
            .putString(KEY_DOCTOR_NAME, doctorName)
            .apply()
    }

    //    lay doctor dang nhap
    fun getDoctorId(context: Context): Long {
        return getDoctorSharedPrefs(context).getLong(KEY_DOCTOR_ID, -1L)
    }

    fun getDoctorName(context: Context): String? {
        return getDoctorSharedPrefs(context).getString(KEY_DOCTOR_NAME, null)
    }

    //    check login
    fun isLoggedIn(context: Context): Boolean {
        return getDoctorId(context) != -1L
    }

    //    logout, xoa session
    fun logout(context: Context) {
        // Xóa session Bác sĩ
        getDoctorSharedPrefs(context).edit().clear().apply()
        // Xóa luôn session Bệnh nhân đang thao tác để dọn dẹp
        clearCurrentPatientInfo(context)
    }

    //    clear khi logout (hàm này tương đương logout, có thể dùng logout thay thế)
    fun clear(context: Context) {
        getDoctorSharedPrefs(context).edit().clear().apply()
        clearCurrentPatientInfo(context)
    }


    // -------------------------------------------------------------------------
    // LOGIC SESSION BỆNH NHÂN ĐANG THAO TÁC (Đã thêm)
    // -------------------------------------------------------------------------

    /**
     * Lưu ID và Tên Bệnh nhân đang được xem/thao tác vào session.
     */
    fun saveCurrentPatientInfo(context: Context, patientId: Long, patientName: String) {
        getPatientSharedPrefs(context).edit()
            .putLong(KEY_CURRENT_PATIENT_ID, patientId)
            .putString(KEY_CURRENT_PATIENT_NAME, patientName)
            .apply()
    }

    /**
     * Lấy ID Bệnh nhân hiện tại từ session. Trả về -1L nếu không có.
     */
    fun getCurrentPatientId(context: Context): Long {
        return getPatientSharedPrefs(context).getLong(KEY_CURRENT_PATIENT_ID, -1L)
    }

    /**
     * Lấy Tên Bệnh nhân hiện tại từ session. Trả về null nếu không có.
     */
    fun getCurrentPatientName(context: Context): String? {
        return getPatientSharedPrefs(context).getString(KEY_CURRENT_PATIENT_NAME, null)
    }

    /**
     * XÓA (dọn dẹp) thông tin Bệnh nhân hiện tại trong session.
     */
    fun clearCurrentPatientInfo(context: Context) {
        getPatientSharedPrefs(context).edit().clear().apply()
    }
}