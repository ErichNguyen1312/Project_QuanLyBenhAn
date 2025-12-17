package com.example.projectqlbenhan


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectqlbenhan.dao.PrescriptionDao
import com.example.projectqlbenhan.dao.appointment.AppointmentDao
import com.example.projectqlbenhan.dao.doctor.DoctorDao
import com.example.projectqlbenhan.dao.medicalRecord.MedicalRecordDao
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.entity.Prescription
import com.example.projectqlbenhan.entity.appointment.Appointment
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient

@Database(
    entities = [
        Patient::class,
        MedicalRecord::class,
        Doctor::class,
        Appointment::class,
        Prescription::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MedicalRecordDatabase : RoomDatabase() {

    //khai bao cac dao
    abstract fun patientDao(): PatientDao
    abstract fun medicalRecordDao(): MedicalRecordDao

    abstract fun doctorDao(): DoctorDao

    abstract fun appointmentDao(): AppointmentDao

    abstract fun prescriptionDao() : PrescriptionDao

    companion object {
        private var INSTANCE: MedicalRecordDatabase? = null

        fun getDatabase(context: Context): MedicalRecordDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalRecordDatabase::class.java,
                    "qlbenhan_db"
                )

                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE!!
        }
    }
}