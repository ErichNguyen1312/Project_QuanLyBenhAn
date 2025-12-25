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
import com.example.projectqlbenhan.utils.DateTimeUtils

// Model đơn giản cho slot giờ
data class TimeSlot(
    val time: String,
    var isAvailable: Boolean = true,
    var isSelected: Boolean = false
)

class TimeSlotAdapter(
    private var slots: List<TimeSlot>,
    private var selectedDateMillis: Long,
    private val onTimeSelected: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeViewHolder>() {

    fun updateSelectedDate(newDateMillis: Long) {
        this.selectedDateMillis = newDateMillis
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(slots[position])
    }

    override fun getItemCount() = slots.size

    inner class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTimeSlot) // Đảm bảo ID này đúng trong XML

        fun bind(slot: TimeSlot) {
            tvTime.text = slot.time

            val isToday = DateTimeUtils.isToday(selectedDateMillis)
            val isPassed = DateTimeUtils.isPastTime(slot.time)

            if (isToday && isPassed) {
                setupStateDisabled(isStrikeThrough = false, isDimmed = true) // Mờ đi
            }
            else if (!slot.isAvailable) {
                setupStateDisabled(isStrikeThrough = true, isDimmed = false) // Gạch ngang
            }
            else {
                itemView.isEnabled = true
                itemView.alpha = 1.0f
                tvTime.paintFlags = tvTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() // Bỏ gạch

                if (slot.isSelected) {
                    tvTime.setBackgroundResource(R.drawable.bg_blue_button)
                    tvTime.setTextColor(Color.WHITE)
                    tvTime.setTypeface(null, Typeface.BOLD)
                } else {
                    tvTime.setBackgroundResource(R.drawable.bg_input_rounded)
                    tvTime.setTextColor(Color.parseColor("#111827"))
                    tvTime.setTypeface(null, Typeface.NORMAL)
                }
            }

            // Xử lý Click
            itemView.setOnClickListener {
                val canClick = !(isToday && isPassed) && slot.isAvailable

                if (canClick) {
                    slots.forEach { it.isSelected = false }
                    slot.isSelected = true
                    notifyDataSetChanged()
                    onTimeSelected(slot)
                }
            }
        }

        // Hàm phụ để set giao diện bị vô hiệu hóa
        private fun setupStateDisabled(isStrikeThrough: Boolean, isDimmed: Boolean) {
            itemView.isEnabled = false // Không cho click
            tvTime.setBackgroundResource(R.drawable.bg_input_rounded)
            tvTime.setTextColor(Color.GRAY)
            tvTime.setTypeface(null, Typeface.NORMAL)

            if (isDimmed) {
                itemView.alpha = 0.5f // Làm mờ (cho giờ đã qua)
            } else {
                itemView.alpha = 1.0f
            }

            if (isStrikeThrough) {
                tvTime.paintFlags = tvTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTime.paintFlags = tvTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }
}