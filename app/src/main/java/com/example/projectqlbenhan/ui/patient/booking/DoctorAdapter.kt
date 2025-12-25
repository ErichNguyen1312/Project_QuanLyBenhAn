package com.example.projectqlbenhan.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.doctor.Doctor
import com.example.projectqlbenhan.entity.doctor.DoctorWithRating // ✅ Nhớ import
import com.google.android.material.card.MaterialCardView

class DoctorAdapter(
    private val doctors: List<DoctorWithRating>, // ✅ Sửa thành DoctorWithRating
    private val onDoctorSelected: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    private var selectedPosition = 0 // ✅ Sửa thành 0 (Mặc định chọn người đầu tiên)

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvSpecialty: TextView = itemView.findViewById(R.id.tvSpecialty)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val cardDoctor: MaterialCardView = itemView.findViewById(R.id.cardDoctor)

        fun bind(item: DoctorWithRating, position: Int) {
            val doctor = item.doctor

            tvName.text = doctor.fullName
            tvSpecialty.text = doctor.specialization // Hoặc doctor.specialization tùy entity của bro

            // ✅ HIỂN THỊ ĐIỂM THẬT
            val rating = item.averageRating ?: 0.0
            if (rating > 0) {
                tvRating.text = String.format("★ %.1f", rating)
                tvRating.setTextColor(Color.parseColor("#FFD700")) // Vàng
            } else {
                tvRating.text = "Mới"
                tvRating.setTextColor(Color.GRAY)
            }

            // LOGIC ĐỔI MÀU
            if (selectedPosition == position) {
                cardDoctor.setCardBackgroundColor(Color.parseColor("#2563EB")) // Xanh
                cardDoctor.strokeWidth = 0
                tvName.setTextColor(Color.WHITE)
                tvSpecialty.setTextColor(Color.parseColor("#E0E7FF"))
            } else {
                cardDoctor.setCardBackgroundColor(Color.WHITE)
                cardDoctor.strokeColor = Color.parseColor("#E5E7EB")
                cardDoctor.strokeWidth = 2
                tvName.setTextColor(Color.parseColor("#111827"))
                tvSpecialty.setTextColor(Color.parseColor("#6B7280"))
            }

            itemView.setOnClickListener {
                val previousItem = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousItem)
                notifyItemChanged(selectedPosition)
                onDoctorSelected(doctor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_card, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position], position)
    }

    override fun getItemCount() = doctors.size
}