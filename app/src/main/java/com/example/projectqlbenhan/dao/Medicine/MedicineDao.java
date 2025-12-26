package com.example.projectqlbenhan.dao.Medicine;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projectqlbenhan.entity.MedicineEntity.Medicine;

import java.util.List;

@Dao
public interface MedicineDao {

    // 1. XEM: Đổi medicines_table thành medicines để khớp với Entity
    @Query("SELECT * FROM medicines WHERE medicalRecordId = :recordId ORDER BY id DESC")
    LiveData<List<Medicine>> getMedicinesByRecordId(int recordId);

    // 2. XEM CHI TIẾT
    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    Medicine getMedicineById(int medicineId);

    // 3. THÊM: Trả về long để biết ID vừa chèn
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Medicine medicine);

    // 4. SỬA: Trả về int để kiểm tra số dòng cập nhật thành công
    @Update
    int update(Medicine medicine);

    // 5. XÓA: Trả về int để kiểm tra thao tác xóa
    @Delete
    int delete(Medicine medicine);

    // Xóa tất cả thuốc thuộc một bệnh án
    @Query("DELETE FROM medicines WHERE medicalRecordId = :recordId")
    void deleteMedicinesByRecord(int recordId);
}