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
    private var data: List<MedicalRecord>
) : ArrayAdapter<MedicalRecord>(context, 0, data) {

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

        val tvDiagnosis = view.findViewById<TextView>(R.id.tvDiagnosis)
        val tvDiseaseType = view.findViewById<TextView>(R.id.tvDiseaseType)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val imgIcon = view.findViewById<ImageView>(R.id.img_icon)

        tvDiagnosis.text = record.diagnosis

        val shortSymptoms = if (record.symptoms.length > 30) {
            record.symptoms.substring(0, 30) + "..."
        } else {
            record.symptoms
        }
        tvDiseaseType.text = "Triệu chứng: $shortSymptoms"

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(record.examinationDate))
        tvDate.text = "Ngày khám: $dateStr"

        imgIcon.setImageResource(R.drawable.outline_article_24)

        return view
    }
}