package com.example.projectqlbenhan.dao.doctor

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.doctor.DoctorWithRating
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

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(doctor: Doctor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doctor: Doctor): Long
    @Update
    suspend fun update(doctor: Doctor)

    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun countDoctors(): Int

    // Load list cho bệnh nhân đặt lịch
    @Query("SELECT * FROM doctors")
    fun getAllDoctorsFlow(): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors")
    suspend fun getAllDoctors(): List<Doctor>

    @Transaction
    @Query("""
        SELECT d.*, AVG(r.rating_doctor) as averageRating
        FROM doctors d
        LEFT JOIN medical_records m ON d.doctorId = m.doctor_id
        LEFT JOIN reviews r ON m.recordId = r.record_id
        GROUP BY d.doctorId
    """)
    suspend fun getDoctorsWithRating(): List<DoctorWithRating>


    //Lấy danh sách bác sĩ - Trí
    @Query("SELECT * FROM doctors")
    suspend fun getAll(): List<Doctor>

    @Update
    suspend fun Update(doctor: Doctor)

    @Delete
    suspend fun Delete(doctor: Doctor)



}