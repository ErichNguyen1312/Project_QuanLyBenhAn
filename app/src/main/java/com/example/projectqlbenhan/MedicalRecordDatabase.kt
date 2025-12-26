package com.example.projectqlbenhan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase 
import com.example.projectqlbenhan.dao.accountDao.AccountDao
import com.example.projectqlbenhan.dao.appointment.AppointmentDao
import com.example.projectqlbenhan.dao.doctor.DoctorDao
import com.example.projectqlbenhan.dao.medicalRecord.MedicalRecordDao
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.dao.prescriptionItemDao.PrescriptionItemDao
import com.example.projectqlbenhan.entity.account.Account
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import com.example.projectqlbenhan.utils.AccountSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Doctor::class,
        Patient::class,
        MedicalRecord::class,
        Appointment::class,
        PrescriptionItem::class, // Thực thể liên quan đến phần đơn thuốc của bạn
        Account::class
    ],
    version = 7, // Tăng lên version 7 để Room cập nhật các thay đổi mới nhất về Schema
    exportSchema = false
)
abstract class MedicalRecordDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun prescriptionItemDao(): PrescriptionItemDao // DAO điều khiển phần đơn thuốc
    abstract fun accountDao(): AccountDao

    private class MedicalRecordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    AccountSeeder.seed(database.accountDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // Đảm bảo dữ liệu tài khoản luôn sẵn sàng khi mở app
                    AccountSeeder.seed(database.accountDao())
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MedicalRecordDatabase? = null

        fun getDatabase(context: Context): MedicalRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalRecordDatabase::class.java,
                    "medical_record_database"
                )
                    .addCallback(MedicalRecordDatabaseCallback(CoroutineScope(Dispatchers.IO)))
                    // Sử dụng destructive migration để tự động cập nhật bảng PrescriptionItem khi bạn thay đổi Entity
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}