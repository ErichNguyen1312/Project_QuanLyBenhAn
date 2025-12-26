package com.example.projectqlbenhan.ui.medicalRecord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.medicalRecord.MedicalRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicalRecordAdapter(
    private val list: List<MedicalRecord>,
    private val onClick: (MedicalRecord) -> Unit
) : RecyclerView.Adapter<MedicalRecordAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDiagnosis = itemView.findViewById<TextView>(R.id.tvDiagnosis)
        val tvDiseaseType = itemView.findViewById<TextView>(R.id.tvDiseaseType)
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val tvSymptoms = itemView.findViewById<TextView>(R.id.tvSymptoms)

        fun bind(item: MedicalRecord) {
            tvDiagnosis.text = item.diagnosis
            tvDiseaseType.text = "Loại bệnh: ${item.diseaseType}"
            tvDate.text = "Ngày khám: ${formatDate(item.examinationDate)}"
            tvSymptoms.text = item.symptoms
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_record, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(time))
    }
}