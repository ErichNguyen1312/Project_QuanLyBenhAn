package com.example.projectqlbenhan.entity.MedicineEntity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

// Định nghĩa tên bảng là "medicines" để khớp với câu lệnh Query trong DAO
@Entity(tableName = "medicines")
public class Medicine implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id; // ID tự động tăng

    private int medicalRecordId; // Khóa ngoại liên kết với Bệnh án
    private String name;         // Tên thuốc (edtTenThuoc)
    private String unit;         // Đơn vị/Dạng thuốc (edtDangThuoc)
    private String dosage;       // Liều dùng (edtLieuDung)
    private int quantity;        // Số lượng/Số lần dùng (edtSoLanDung)
    private String note;         // Ghi chú (edtGhiChu)

    // Constructor
    public Medicine(int medicalRecordId, String name, String unit, String dosage, int quantity, String note) {
        this.medicalRecordId = medicalRecordId;
        this.name = name;
        this.unit = unit;
        this.dosage = dosage;
        this.quantity = quantity;
        this.note = note;
    }

    // --- Getter và Setter (Bắt buộc phải có để Room Database hoạt động) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(int medicalRecordId) { this.medicalRecordId = medicalRecordId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}