package com.example.projectqlbenhan



import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectqlbenhan.dao.doctor.DoctorDao
import com.example.projectqlbenhan.dao.medicalRecord.MedicalRecordDao
import com.example.projectqlbenhan.dao.patient.PatientDao
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import com.example.projectqlbenhan.entity.patient.Patient

@Database(
    entities = [
        Patient::class,
        MedicalRecord::class,
        Doctor::class

//        Prescription::class,
//        Appointment::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MedicalRecordDatabase: RoomDatabase() {

    //khai bao cac dao
    abstract fun patientDao(): PatientDao
    abstract fun medicalRecordDao(): MedicalRecordDao

    abstract fun doctorDao(): DoctorDao

//    abstract fun prescriptionDao(): PrescriptionDao
//    abstract fun appointmentDao(): AppointmentDao

    companion object {
        private var INSTANCE: MedicalRecordDatabase? = null

        fun getDatabase(context: Context): MedicalRecordDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalRecordDatabase::class.java,
                    "simple_db"
                )

                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE!!
        }
    }
}