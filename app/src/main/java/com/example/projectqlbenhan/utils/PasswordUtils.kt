package com.example.projectqlbenhan.utils

import java.security.MessageDigest

object PasswordUtils {

    fun hash(raw: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}