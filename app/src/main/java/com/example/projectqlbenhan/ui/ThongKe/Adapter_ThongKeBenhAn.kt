package com.example.projectqlbenhan.ui.ThongKe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Adapter_ThongKeBenhAn(
    context: Context,
    private var data: List<MedicalRecord> // Đổi thành List để dễ quản lý
) : ArrayAdapter<MedicalRecord>(context, 0, data) {

    // Hàm cập nhật dữ liệu mới từ Activity
    fun updateData(newData: List<MedicalRecord>) {
        this.data = newData
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): MedicalRecord? {
        return data[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_thong_ke_benh_an, parent, false)

        val record = data[position]

        // Ánh xạ View
        val tvDiagnosis = view.findViewById<TextView>(R.id.tvDiagnosis)
        val tvDiseaseType = view.findViewById<TextView>(R.id.tvDiseaseType)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val imgIcon = view.findViewById<ImageView>(R.id.img_icon)

        // Gán dữ liệu
        // 1. Chẩn đoán (Diagnosis)
        tvDiagnosis.text = record.diagnosis

        // 2. Dòng phụ: Hiển thị triệu chứng (Symptoms) vì Entity không có field diseaseType
        // Nếu symptoms quá dài thì cắt bớt
        val shortSymptoms = if (record.symptoms.length > 30) {
            record.symptoms.substring(0, 30) + "..."
        } else {
            record.symptoms
        }
        tvDiseaseType.text = "Triệu chứng: $shortSymptoms"

        // 3. Ngày khám
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(record.examinationDate)) // Sử dụng examinationDate từ Entity
        tvDate.text = "Ngày khám: $dateStr"

        // 4. Set icon (Có thể logic đổi icon theo bệnh nếu muốn, hiện tại để mặc định)
        imgIcon.setImageResource(R.drawable.outline_article_24) // Đảm bảo bạn có icon này

        return view
    }
}