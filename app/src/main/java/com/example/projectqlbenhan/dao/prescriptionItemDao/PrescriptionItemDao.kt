package com.example.projectqlbenhan.dao.prescriptionItemDao

import androidx.room.*
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem

@Dao
interface PrescriptionItemDao {

    @Query("SELECT * FROM prescription_items WHERE record_id = :recordId")
    suspend fun getItemsByRecordId(recordId: Long): List<PrescriptionItem>

    // ⭐ Cần bổ sung hàm này để fix lỗi Unresolved reference 'getItemById'
    @Query("SELECT * FROM prescription_items WHERE itemId = :id LIMIT 1")
    suspend fun getItemById(id: Long): PrescriptionItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptionItems(items: List<PrescriptionItem>)

    @Update
    suspend fun updateItem(item: PrescriptionItem)

    @Delete
    suspend fun deleteSingleItem(item: PrescriptionItem)

    @Query("DELETE FROM prescription_items WHERE record_id = :recordId")
    suspend fun deleteItemsByRecordId(recordId: Long)
}