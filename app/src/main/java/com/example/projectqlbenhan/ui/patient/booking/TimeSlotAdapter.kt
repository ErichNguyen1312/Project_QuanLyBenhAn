package com.example.projectqlbenhan.ui.home

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R

// Model đơn giản cho slot giờ
data class TimeSlot(val time: String, var isAvailable: Boolean = true, var isSelected: Boolean = false)

class TimeSlotAdapter(
    private var slots: List<TimeSlot>,
    private val onTimeSelected: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeViewHolder>() {

    inner class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(slot: TimeSlot) {
            tvTime.text = slot.time

            if (!slot.isAvailable) {
                // DISABLED (Đã có người đặt)
                tvTime.setBackgroundResource(R.drawable.bg_input_rounded)
                tvTime.setTextColor(Color.parseColor("#9CA3AF")) // Gray mờ
                tvTime.paintFlags = tvTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Gạch ngang
                itemView.isEnabled = false
            } else {
                itemView.isEnabled = true
                tvTime.paintFlags = tvTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() // Bỏ gạch ngang

                if (slot.isSelected) {
                    // SELECTED
                    tvTime.setBackgroundResource(R.drawable.bg_blue_button)
                    tvTime.setTextColor(Color.WHITE)
                    tvTime.setTypeface(null, Typeface.BOLD)
                } else {
                    // AVAILABLE
                    tvTime.setBackgroundResource(R.drawable.bg_input_rounded)
                    tvTime.setTextColor(Color.parseColor("#111827"))
                    tvTime.setTypeface(null, Typeface.NORMAL)
                }
            }

            itemView.setOnClickListener {
                if (slot.isAvailable) {
                    // Reset các slot khác
                    slots.forEach { it.isSelected = false }
                    slot.isSelected = true
                    notifyDataSetChanged()
                    onTimeSelected(slot)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(slots[position])
    }

    override fun getItemCount() = slots.size
}