package com.example.projectqlbenhan

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectqlbenhan.dao.PatientDao
import com.example.projectqlbenhan.entity.MedicalRecord
import com.example.projectqlbenhan.entity.Patient

@Database(
    entities = [
        Patient::class,
        MedicalRecord::class
//        Prescription::class,
//        Appointment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MedicalRecordDatabase: RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun medicalRecordDao(): MedicalRecord
    companion object {
        private var INSTANCE: MedicalRecordDatabase? = null

        fun getDatabase(context: Context): MedicalRecordDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalRecordDatabase::class.java,
                    "simple_db"
                ).allowMainThreadQueries()   // ⚠️ Run trên main thread cho đơn giản
                    .build()
            }
            return INSTANCE!!
        }
    }
}