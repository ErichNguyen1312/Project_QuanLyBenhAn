package com.example.projectqlbenhan.dao.prescriptionItemDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem

@Dao
interface PrescriptionItemDao {
    // Lấy danh sách thuốc của 1 bệnh án
    @Query("SELECT * FROM prescription_items WHERE record_id = :recordId")
    suspend fun getItemsByRecordId(recordId: Long): List<PrescriptionItem>

    // Thêm list thuốc 1 lần
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptionItems(items: List<PrescriptionItem>)

    @Query("DELETE FROM prescription_items WHERE record_id = :recordId")
    suspend fun deleteItemsByRecordId(recordId: Long)
    // lay so luong don thuoc theo benh an la ra tong don thuoc
    @Query("SELECT COUNT(DISTINCT record_id) FROM prescription_items")
    fun countTotalPrescriptions(): Int
}