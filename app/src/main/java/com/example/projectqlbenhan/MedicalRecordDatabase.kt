package com.example.projectqlbenhan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectqlbenhan.dao.accountDao.AccountDao
import com.example.projectqlbenhan.dao.appointment.AppointmentDao
import com.example.projectqlbenhan.dao.doctor.DoctorDao
import com.example.projectqlbenhan.dao.medicalRecord.MedicalRecordDao
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao
import com.example.projectqlbenhan.dao.reviewDao.ReviewDao
import com.example.projectqlbenhan.entity.account.Account
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.entity.review.Review
import com.example.projectqlbenhan.utils.Doctor

// Lưu ý package name cho đúng



@Database(
    entities = [
        Account::class,          // 1. Tài khoản
        Doctor::class,           // 2. Bác sĩ
        Patient::class,          // 3. Bệnh nhân
        Appointment::class,      // 4. Lịch hẹn
        MedicalRecord::class,    // 5. Bệnh án
        PrescriptionItem::class, // 6. Thuốc
        Review::class            // 7. Đánh giá
    ],
    version = 5,
    exportSchema = false
)
abstract class MedicalRecordDatabase : RoomDatabase() {

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

        fun getDatabase(context: Context): MedicalRecordDatabase {
            // Singleton Pattern chuẩn
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalRecordDatabase::class.java,
                    "qlbenhan_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}