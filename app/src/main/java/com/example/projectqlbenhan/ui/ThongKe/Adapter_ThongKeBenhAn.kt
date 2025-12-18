package com.example.projectqlbenhan.ui.ThongKe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import java.text.SimpleDateFormat
import java.util.*

class Adapter_ThongKeBenhAn(
    context: Context,
    private val data: MutableList<MedicalRecord>
) : ArrayAdapter<MedicalRecord>(context, 0, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_thong_ke_benh_an, parent, false)

        val record = data[position]

        val tvDiseaseType = view.findViewById<TextView>(R.id.tvDiseaseType)
        val tvDiagnosis = view.findViewById<TextView>(R.id.tvDiagnosis)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)

        tvDiseaseType.text = record.diseaseType
        tvDiagnosis.text = "Chuẩn đoán: ${record.diagnosis}"

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvDate.text = "Ngày khám: ${sdf.format(Date(record.examinationDate))}"

        return view
    }

    fun updateData(newData: List<MedicalRecord>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}
