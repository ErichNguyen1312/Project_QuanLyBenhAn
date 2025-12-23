package com.example.projectqlbenhan.utils

import com.example.projectqlbenhan.dao.accountDao.AccountDao
import com.example.projectqlbenhan.entity.account.Account

object AccountSeeder {

    suspend fun seed(dao: AccountDao) {
        // Kiểm tra nếu đã có tài khoản thì không seed nữa
        val count = dao.countAccounts()
        if (count > 0) return

        val accounts = listOf(
            // 1. Tài khoản Admin
            Account(
                username = "admin",
                passwordHash = PasswordUtils.hash("123456"), // Mật khẩu mặc định
                role = "ADMIN"
            ),

            // 2. Tài khoản Bác sĩ (Khớp username với DoctorSeeder)
            Account(
                username = "doctor01",
                passwordHash = PasswordUtils.hash("123456"),
                role = "DOCTOR"
            ),
            Account(
                username = "doctor02",
                passwordHash = PasswordUtils.hash("123456"),
                role = "DOCTOR"
            ),
            Account(
                username = "doctor03",
                passwordHash = PasswordUtils.hash("123456"),
                role = "DOCTOR"
            ),
            Account(
                username = "doctor04",
                passwordHash = PasswordUtils.hash("123456"),
                role = "DOCTOR"
            )
            // Bro có thể thêm user "PATIENT" test nếu cần
        )

        accounts.forEach {
            dao.insertAccount(it)
        }
    }
}