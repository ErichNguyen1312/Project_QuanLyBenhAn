package com.example.projectqlbenhan.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.AppointmentWithPatient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppointmentAdapter(
    private var data: List<AppointmentWithPatient>,
    private val onItemClick: (AppointmentWithPatient) -> Unit,
    private val onItemLongClick: (AppointmentWithPatient) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvReason: TextView = itemView.findViewById(R.id.tvAppointmentType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.tvTime.text = formatTimestampToTime(item.appointment.appointmentDate)

        holder.tvPatientName.text = item.patient.fullName
        holder.tvReason.text = item.appointment.reason ?: "Tái khám"

        when (item.appointment.status) {
            "MISSED" -> {
                holder.tvTime.setTextColor(Color.RED)
                holder.tvReason.text = "Quá hạn / Vắng mặt"
                holder.tvReason.setTextColor(Color.RED)
            }

            "CANCELLED" -> {
                holder.tvTime.setTextColor(Color.GRAY)
                holder.tvReason.text = "Đã hủy"
                holder.tvReason.setTextColor(Color.GRAY)
            }

            "COMPLETED" -> holder.tvTime.setTextColor(Color.parseColor("#4CAF50"))
            else -> {
                holder.tvTime.setTextColor(Color.parseColor("#007BFF"))
                holder.tvReason.setTextColor(Color.parseColor("#6C757D"))
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item)
        }
        holder.itemView.setOnLongClickListener {
            if (item.appointment.status == "SCHEDULED") {
                onItemLongClick(item)
                true
            } else false
        }
    }

    fun submitList(newData: List<AppointmentWithPatient>) {
        data = newData
        notifyDataSetChanged()
    }

    // Hàm helper format giờ
    private fun formatTimestampToTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "--:--"
        }
    }
}