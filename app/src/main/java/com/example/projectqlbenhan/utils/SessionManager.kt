package com.example.projectqlbenhan.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    // --- 1. SESSION ĐĂNG NHẬP (USER SESSION) ---
    private const val PREF_USER_SESSION = "UserSession"

    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_AUTH_TOKEN = "auth_token"       // Token (nếu có)
    private const val KEY_ACCOUNT_ID = "account_id"       // ID tài khoản
    private const val KEY_ROLE = "role"                   // "DOCTOR", "PATIENT", "ADMIN"
    private const val KEY_SPECIFIC_ID = "specific_id"     // doctorId hoặc patientId
    private const val KEY_FULL_NAME = "full_name"         // Tên hiển thị

    // --- 2. SESSION TÁC VỤ (CONTEXT SESSION) ---
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
    // A. CÁC HÀM SETTER (LƯU DỮ LIỆU) - ⭐️ MỚI THÊM ⭐️
    // ========================================================================

    fun saveAuthToken(context: Context, token: String) {
        getUserPrefs(context).edit().putString(KEY_AUTH_TOKEN, token).putBoolean(KEY_IS_LOGGED_IN, true).apply()
    }

    fun saveUserRole(context: Context, role: String) {
        getUserPrefs(context).edit().putString(KEY_ROLE, role).apply()
    }

    fun saveAccountId(context: Context, accountId: Long) {
        getUserPrefs(context).edit().putLong(KEY_ACCOUNT_ID, accountId).apply()
    }

    fun saveSpecificId(context: Context, id: Long) {
        getUserPrefs(context).edit().putLong(KEY_SPECIFIC_ID, id).apply()
    }

    fun saveFullName(context: Context, name: String) {
        getUserPrefs(context).edit().putString(KEY_FULL_NAME, name).apply()
    }

    /**
     * Hàm cũ: Lưu tất cả cùng lúc (Vẫn giữ để tương thích nếu chỗ nào dùng)
     */
    fun saveUserSession(
        context: Context,
        accountId: Long,
        role: String,
        specificId: Long,
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

    // ========================================================================
    // B. CÁC HÀM GETTER (LẤY DỮ LIỆU)
    // ========================================================================

    fun isLoggedIn(context: Context): Boolean {
        return getUserPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getRole(context: Context): String? {
        return getUserPrefs(context).getString(KEY_ROLE, null)
    }

    fun getSpecificId(context: Context): Long {
        return getUserPrefs(context).getLong(KEY_SPECIFIC_ID, -1L)
    }

    fun getAccountId(context: Context): Long {
        return getUserPrefs(context).getLong(KEY_ACCOUNT_ID, -1L)
    }

    fun getFullName(context: Context): String {
        return getUserPrefs(context).getString(KEY_FULL_NAME, "Người dùng") ?: "Người dùng"
    }

    // Đăng xuất
    fun logout(context: Context) {
        getUserPrefs(context).edit().clear().apply()
        getContextPrefs(context).edit().clear().apply()
    }

    // ========================================================================
    // C. QUẢN LÝ TÁC VỤ (CONTEXT SESSION)
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