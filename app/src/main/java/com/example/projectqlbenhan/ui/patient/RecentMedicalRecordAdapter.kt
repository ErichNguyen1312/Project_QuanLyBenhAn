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
    private val list: List<MedicalRecord>
) : RecyclerView.Adapter<RecentMedicalRecordAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvDate = v.findViewById<TextView>(R.id.tvDate)
        val tvDiagnosis = v.findViewById<TextView>(R.id.tvDiagnosis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_record, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvDate.text = formatDate(item.examinationDate)
        holder.tvDiagnosis.text = item.diagnosis
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}