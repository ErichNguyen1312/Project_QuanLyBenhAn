package com.example.projectqlbenhan.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R

// Model đơn giản cho ngày
data class BookingDate(val dayNumber: String, val dayOfWeek: String, val fullDate: Long)

class DateAdapter(
    private val dates: List<BookingDate>,
    private val onDateSelected: (BookingDate) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var selectedPosition = 0 // Mặc định chọn ngày đầu

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        val tvDayOfWeek: TextView = itemView.findViewById(R.id.tvDayOfWeek)
        val layoutDate: LinearLayout = itemView.findViewById(R.id.layoutDate)

        fun bind(date: BookingDate, position: Int) {
            tvDayNumber.text = date.dayNumber
            tvDayOfWeek.text = date.dayOfWeek

            if (selectedPosition == position) {
                // SELECTED
                layoutDate.setBackgroundResource(R.drawable.bg_blue_button) // Nền Xanh
                tvDayNumber.setTextColor(Color.WHITE)
                tvDayOfWeek.setTextColor(Color.parseColor("#E0E7FF"))
            } else {
                // UNSELECTED
                layoutDate.setBackgroundResource(R.drawable.bg_input_rounded) // Nền Xám
                tvDayNumber.setTextColor(Color.parseColor("#111827"))
                tvDayOfWeek.setTextColor(Color.parseColor("#6B7280"))
            }

            itemView.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = position
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onDateSelected(date)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dates[position], position)
    }

    override fun getItemCount() = dates.size
}