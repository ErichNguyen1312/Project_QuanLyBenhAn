package com.example.projectqlbenhan.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    // --- 1. SESSION ĐĂNG NHẬP (USER SESSION) ---
    private const val PREF_USER_SESSION = "UserSession"

    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_ACCOUNT_ID = "account_id"
    private const val KEY_ROLE = "role"
    private const val KEY_SPECIFIC_ID = "specific_id"
    private const val KEY_FULL_NAME = "full_name"

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
    // A. QUẢN LÝ ĐĂNG NHẬP (LOGIN SESSION)
    // ========================================================================

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

    fun logout(context: Context) {
        getUserPrefs(context).edit().clear().apply()
        getContextPrefs(context).edit().clear().apply()
    }

    // ========================================================================
    // B. QUẢN LÝ TÁC VỤ (WORKING CONTEXT)
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

    // ========================================================================
    // ⭐ PHẦN BỔ SUNG: DÀNH RIÊNG CHO ĐƠN THUỐC (KHÔNG ẢNH HƯỞNG PHẦN KHÁC)
    // Các hàm này gọi lại các hàm Context Session ở trên để khớp với code của bạn
    // ========================================================================

    /**
     * Đồng bộ với getCurrentPatientId trong file SuaDonThuoc/ThemDonThuoc
     */
    fun getCurrentPatientId(context: Context): Long {
        return getSelectedPatientId(context)
    }

    /**
     * Đồng bộ với getCurrentPatientName trong file SuaDonThuoc/ThemDonThuoc
     */
    fun getCurrentPatientName(context: Context): String? {
        return getContextPrefs(context).getString(KEY_SELECTED_PATIENT_NAME, null)
    }

    /**
     * Đồng bộ với clearCurrentPatientInfo trong file SuaDonThuoc/ThemDonThuoc
     */
    fun clearCurrentPatientInfo(context: Context) {
        clearSelectedPatient(context)
    }
}