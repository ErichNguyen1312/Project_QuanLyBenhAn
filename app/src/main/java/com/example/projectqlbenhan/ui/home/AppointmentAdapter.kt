package com.example.projectqlbenhan.ui.home

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


class AppointmentAdapter(private var data: List<AppointmentWithPatient>) :
    RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvAppointmentType: TextView = itemView.findViewById(R.id.tvAppointmentType)

//
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val formatAmPm = formatTime24hToAMPM(item.appointment.appointmentTime)
        holder.tvTime.text = formatAmPm
        holder.tvPatientName.text = item.patient.fullName
        holder.tvAppointmentType.text = item.appointment.notes ?: "Không có ghi chú"
    }

    fun submitList(newData: List<AppointmentWithPatient>) {
        data = newData
        notifyDataSetChanged()
    }

    private fun formatTime(time: String): String {
        return try {
            time
        } catch (e: Exception) {
            "--:--"
        }
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