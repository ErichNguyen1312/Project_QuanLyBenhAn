package com.example.projectqlbenhan.ui.BacSi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.doctor.Doctor
import java.util.Locale

class Adapter_BacSi(
    private var list: List<Doctor>,
    private val onClick: (Doctor) -> Unit
) : RecyclerView.Adapter<Adapter_BacSi.DoctorViewHolder>() {

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvInfo: TextView = itemView.findViewById(R.id.tvInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        // Trỏ đúng vào file layout item_bac_si vừa tạo
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bac_si, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = list[position]

        // 1. Hiển thị Tên
        holder.tvName.text = "${doctor.fullName}"

        // 2. Xử lý Avatar (Lấy chữ cái đầu tiên in hoa)
        if (doctor.fullName.isNotEmpty()) {
            val firstChar = doctor.fullName.trim().first().toString()
            holder.tvAvatar.text = firstChar.uppercase(Locale.getDefault())
        } else {
            holder.tvAvatar.text = "?"
        }

        // 3. Hiển thị thông tin phụ: "Chuyên khoa
        val infoText = "${doctor.specialization}"
        holder.tvInfo.text = infoText

        // Click item
        holder.itemView.setOnClickListener {
            onClick(doctor)
        }
    }

    override fun getItemCount(): Int = list.size

    // Hàm cập nhật data cho tìm kiếm
    fun updateData(newList: List<Doctor>) {
        list = newList
        notifyDataSetChanged()
    }
}