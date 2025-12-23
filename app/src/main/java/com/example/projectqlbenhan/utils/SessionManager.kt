package com.example.projectqlbenhan.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    // --- 1. SESSION ĐĂNG NHẬP (USER SESSION) ---
    private const val PREF_USER_SESSION = "UserSession"

    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_ACCOUNT_ID = "account_id"       // ID tài khoản (quan trọng để đổi pass)
    private const val KEY_ROLE = "role"                   // "DOCTOR", "PATIENT", "ADMIN"
    private const val KEY_SPECIFIC_ID = "specific_id"     // doctorId hoặc patientId
    private const val KEY_FULL_NAME = "full_name"         // Tên hiển thị

    // --- 2. SESSION TÁC VỤ (CONTEXT SESSION) ---
    // Dùng khi Bác sĩ đang thao tác trên hồ sơ 1 bệnh nhân cụ thể
    private const val PREF_CONTEXT_SESSION = "ContextSession"
    private const val KEY_SELECTED_PATIENT_ID = "selected_patient_id"
    private const val KEY_SELECTED_PATIENT_NAME = "selected_patient_name"


    private fun getUserPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE)
    }

    private fun getContextPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_CONTEXT_SESSION, Context.MODE_PRIVATE)
    }

    // ========================================================================
    // A. QUẢN LÝ ĐĂNG NHẬP (LOGIN SESSION)
    // ========================================================================

    /**
     * Lưu phiên đăng nhập cho bất kỳ User nào (Bác sĩ, Bệnh nhân, Admin)
     */
    fun saveUserSession(
        context: Context,
        accountId: Long,
        role: String,
        specificId: Long, // Là doctorId nếu là BS, patientId nếu là BN, -1 nếu là Admin
        fullName: String
    ) {
        getUserPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_ACCOUNT_ID, accountId)
            putString(KEY_ROLE, role)
            putLong(KEY_SPECIFIC_ID, specificId)
            putString(KEY_FULL_NAME, fullName)
            apply()
        }
    }

    // Kiểm tra đã đăng nhập chưa
    fun isLoggedIn(context: Context): Boolean {
        return getUserPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Lấy Role hiện tại (để phân quyền UI)
    fun getRole(context: Context): String? {
        return getUserPrefs(context).getString(KEY_ROLE, null)
    }

    // Lấy ID cụ thể (DoctorId hoặc PatientId) để query dữ liệu cá nhân
    fun getSpecificId(context: Context): Long {
        return getUserPrefs(context).getLong(KEY_SPECIFIC_ID, -1L)
    }

    // Lấy Account ID (để đổi mật khẩu)
    fun getAccountId(context: Context): Long {
        return getUserPrefs(context).getLong(KEY_ACCOUNT_ID, -1L)
    }

    // Lấy tên hiển thị xin chào
    fun getFullName(context: Context): String {
        return getUserPrefs(context).getString(KEY_FULL_NAME, "Người dùng") ?: "Người dùng"
    }

    // Đăng xuất: Xóa sạch mọi thứ
    fun logout(context: Context) {
        getUserPrefs(context).edit().clear().apply()
        getContextPrefs(context).edit().clear().apply() // Xóa luôn context đang làm việc
    }

    // ========================================================================
    // B. QUẢN LÝ TÁC VỤ (WORKING CONTEXT)
    // (Dành cho Bác sĩ khi chọn 1 bệnh nhân để khám/xem hồ sơ)
    // ========================================================================

    fun saveSelectedPatient(context: Context, patientId: Long, patientName: String) {
        getContextPrefs(context).edit().apply {
            putLong(KEY_SELECTED_PATIENT_ID, patientId)
            putString(KEY_SELECTED_PATIENT_NAME, patientName)
            apply()
        }
    }

    fun getSelectedPatientId(context: Context): Long {
        return getContextPrefs(context).getLong(KEY_SELECTED_PATIENT_ID, -1L)
    }

    fun clearSelectedPatient(context: Context) {
        getContextPrefs(context).edit().clear().apply()
    }
}