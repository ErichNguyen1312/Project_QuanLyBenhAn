package com.example.projectqlbenhan.utils

import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.dao.doctor.DoctorDao

object Doctor {

    suspend fun seed(dao: DoctorDao) {
        // nếu đã có bác sĩ → không seed nữa
        val count = dao.countDoctors()
        if (count > 0) return

        val doctors = listOf(
            Doctor(
                username = "doctor01",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Nguyễn Văn An",
                specialization = "Nội tổng quát"
            ),
            Doctor(
                username = "doctor02",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Trần Thị Bình",
                specialization = "Ngoại tổng quát"
            ),
            Doctor(
                username = "doctor03",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Lê Hoàng Cường",
                specialization = "Nhi khoa"
            ),
            Doctor(
                username = "doctor04",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Phạm Thu Dung",
                specialization = "Tim mạch"
            ),
            Doctor(
                username = "doctor05",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Võ Minh Đức",
                specialization = "Thần kinh"
            ),
            Doctor(
                username = "doctor06",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Đặng Thị Hạnh",
                specialization = "Da liễu"
            ),
            Doctor(
                username = "doctor07",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Nguyễn Quốc Hùng",
                specialization = "Tai Mũi Họng"
            ),
            Doctor(
                username = "doctor08",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Lý Thanh Lan",
                specialization = "Mắt"
            ),
            Doctor(
                username = "doctor09",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Trần Văn Minh",
                specialization = "Sản phụ khoa"
            ),
            Doctor(
                username = "doctor10",
                passwordHash = PasswordUtils.hash("123456"),
                fullName = "BS. Hoàng Anh Tuấn",
                specialization = "Chấn thương chỉnh hình"
            )
        )

        doctors.forEach {
            dao.insert(it)
        }
    }
}