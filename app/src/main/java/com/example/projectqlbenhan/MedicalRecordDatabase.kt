package com.example.projectqlbenhan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Import các DAO - Cần khớp chính xác với cấu trúc folder trong image_59c9d8.png
import com.example.projectqlbenhan.dao.accountDao.AccountDao
import com.example.projectqlbenhan.dao.doctor.DoctorDao
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.dao.appointment.AppointmentDao
import com.example.projectqlbenhan.dao.medicalRecord.MedicalRecordDao
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao
import com.example.projectqlbenhan.dao.reviewDao.ReviewDao

// Import các Entity
import com.example.projectqlbenhan.entity.account.Account
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.entity.review.Review
import com.example.projectqlbenhan.entity.doctor.Doctor

@Database(
    entities = [
        Account::class,          // 1. Tài khoản đăng nhập
        Doctor::class,           // 2. Thông tin bác sĩ
        Patient::class,          // 3. Thông tin bệnh nhân
        Appointment::class,      // 4. Lịch hẹn khám
        MedicalRecord::class,    // 5. Hồ sơ bệnh án
        PrescriptionItem::class, // 6. Chi tiết đơn thuốc
        Review::class            // 7. Đánh giá từ người dùng
    ],
    version = 5,
    exportSchema = false
)
abstract class MedicalRecordDatabase : RoomDatabase() {

    // Khai báo các hàm abstract để truy cập vào DAO
    abstract fun accountDao(): AccountDao
    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun prescriptionItemDao(): PrescriptionItemDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalRecordDatabase? = null

        /**
         * Phương thức khởi tạo Database theo Singleton Pattern.
         */
        fun getDatabase(context: Context): MedicalRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalRecordDatabase::class.java,
                    "qlbenhan_db"
                )
                    .fallbackToDestructiveMigration() // Xóa dữ liệu cũ nếu đổi version (hữu ích khi debug)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}