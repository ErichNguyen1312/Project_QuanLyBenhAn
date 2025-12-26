package com.example.projectqlbenhan.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.review.ReviewDetail

class Adapter_DanhGia(
    private var list: List<ReviewDetail>,
    private val onClickItem: (ReviewDetail) -> Unit
) : RecyclerView.Adapter<Adapter_DanhGia.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvRating: TextView = itemView.findViewById(R.id.tvRatingDoc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_danh_gia, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val item = list[position]

        // 1. Hiển thị Tên
        holder.tvName.text = item.doctorName

        // 2. Xử lý Avatar (Lấy chữ cái đầu)
        val nameToShow = item.doctorName
        if (nameToShow.isNotEmpty()) {
            val firstLetter = nameToShow.substring(0, 1).uppercase()
            holder.tvAvatar.text = firstLetter
        } else {
            holder.tvAvatar.text = "?"
        }

        // 3. Hiển thị Sao
        val stars = StringBuilder()
        // Đảm bảo rating không bị âm hoặc quá lớn để tránh lỗi vòng lặp
        val avg = (item.ratingDoctor + item.ratingDiagnosis + item.ratingMedication) / 3
        val safeRating = if (avg in 1..5) avg else 0

        repeat(safeRating) {
            stars.append("⭐")
        }
        holder.tvRating.text = stars.toString()

        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val date = java.util.Date(item.createdAt)
        holder.tvDate.text = sdf.format(date)

        holder.itemView.setOnClickListener {
            onClickItem(item)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(newList: List<ReviewDetail>) {
        list = newList
        notifyDataSetChanged()
    }
}