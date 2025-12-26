package com.example.projectqlbenhan.ui.patient

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

class RecentMedicalRecordAdapter(
    private val list: List<MedicalRecord>,
    private val onClick: (MedicalRecord) -> Unit
) : RecyclerView.Adapter<RecentMedicalRecordAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvDate = v.findViewById<TextView>(R.id.tvDate)
        val tvDiagnosis = v.findViewById<TextView>(R.id.tvDiagnosis)

        fun bind(item: MedicalRecord) {
            tvDate.text = formatDate(item.examinationDate)
            tvDiagnosis.text = item.diagnosis

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_record, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}