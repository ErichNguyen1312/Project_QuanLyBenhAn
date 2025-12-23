package com.example.projectqlbenhan

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
        PrescriptionItem::class,
        Account::class // 1. Nhớ thêm Account vào đây
    ],
    version = 6, // 2. Tăng version lên (nếu cũ là 5)
    exportSchema = false
)
abstract class MedicalRecordDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun prescriptionItemDao(): PrescriptionItemDao
    abstract fun accountDao(): AccountDao // Thêm DAO Account

    private class MedicalRecordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // 3. Gọi các hàm Seed dữ liệu mẫu ở đây
                    AccountSeeder.seed(database.accountDao()) // <-- Gọi Account Seeder
                }
            }
        }

        // Nếu bro muốn nó chạy check mỗi lần mở app (để chắc chắn có data) thì dùng onOpen
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
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
                    // Nhớ thêm dòng này để kích hoạt Callback
                    .addCallback(MedicalRecordDatabaseCallback(CoroutineScope(Dispatchers.IO)))
                    .fallbackToDestructiveMigration() // Xóa dữ liệu cũ nếu đổi version (Cẩn thận khi dùng)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}