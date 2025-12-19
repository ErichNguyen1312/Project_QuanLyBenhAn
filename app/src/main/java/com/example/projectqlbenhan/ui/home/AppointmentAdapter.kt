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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppointmentAdapter(
    private var data: List<AppointmentWithPatient>,
    private val onItemClick: (AppointmentWithPatient) -> Unit,
    private val onItemLongClick: (AppointmentWithPatient) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvAppointmentType: TextView = itemView.findViewById(R.id.tvAppointmentType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val status = item.appointment.status
        holder.tvTime.text = formatTime24hToAMPM(item.appointment.appointmentTime)
        holder.tvPatientName.text = item.patient.fullName
        holder.tvAppointmentType.text = item.appointment.notes ?: "Không có ghi chú"

        when (status) {
            "MISSED" -> {
                holder.tvTime.setTextColor(Color.RED)
                holder.tvAppointmentType.text = "Quá hạn / Không đến"
                holder.tvAppointmentType.setTextColor(Color.RED)
            }
            "CANCELLED" -> {
                holder.tvTime.setTextColor(Color.GRAY)
                holder.tvAppointmentType.text = "Đã hủy hẹn"
            }
            "COMPLETED" -> {
                holder.tvTime.setTextColor(Color.parseColor("#4CAF50"))
            }
            else -> {
                holder.tvTime.setTextColor(Color.parseColor("#007BFF"))
                holder.tvAppointmentType.setTextColor(Color.parseColor("#6C757D"))
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }

        holder.itemView.setOnLongClickListener {
            if (status == "SCHEDULED") {
                onItemLongClick(item)
                true
            } else {
                false
            }
        }
    }

    fun submitList(newData: List<AppointmentWithPatient>) {
        data = newData
        notifyDataSetChanged()
    }

    fun formatTime24hToAMPM(time24h: String): String {
        return try {
            val input = SimpleDateFormat("HH:mm", Locale.US)
            val output = SimpleDateFormat("h:mm a", Locale.US)
            val date = input.parse(time24h)
            output.format(date!!)
        } catch (e: Exception) {
            "--:--"
        }
    }
}