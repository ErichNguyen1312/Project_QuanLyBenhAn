package com.example.projectqlbenhan.dao.doctor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectqlbenhan.entity.doctor.Doctor
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {

    //detail bác sĩ
    @Query("SELECT * FROM doctors WHERE doctorId = :id LIMIT 1")
    suspend fun getDoctorById(id: Long): Doctor?

    //lấy tên bác sĩ theo id
    @Query("SELECT fullName FROM doctors WHERE doctorId = :id LIMIT 1")
    suspend fun getDoctorNameById(id: Long): String?


    // login
    @Query("""
        SELECT d.* FROM doctors d
        INNER JOIN accounts a ON d.account_id = a.accountId
        WHERE a.username = :username AND a.passwordHash = :passwordHash
        LIMIT 1
    """)
    suspend fun login(username: String, passwordHash: String): Doctor?

    // Lấy thông tin bác sĩ theo accountId (Sau khi login thành công bên AccountDao)
    @Query("SELECT * FROM doctors WHERE account_id = :accountId LIMIT 1")
    suspend fun getDoctorByAccountId(accountId: Long): Doctor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doctor: Doctor)

    @Update
    suspend fun update(doctor: Doctor)

    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun countDoctors(): Int

    // Load list cho bệnh nhân đặt lịch
    @Query("SELECT * FROM doctors")
    fun getAllDoctorsFlow(): Flow<List<Doctor>>

    //Lấy danh sách bác sĩ - Trí
    @Query("SELECT * FROM doctors")
    suspend fun getAll(): List<Doctor>
}