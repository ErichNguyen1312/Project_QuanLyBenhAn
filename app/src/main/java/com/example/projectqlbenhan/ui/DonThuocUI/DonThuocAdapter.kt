package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem


class DonThuocAdapter(
    private val context: Context,
    private val clickListener: (PrescriptionItem) -> Unit
) : ListAdapter<PrescriptionItem, DonThuocAdapter.DonThuocViewHolder>(DonThuocDiffCallback()) {

    class DonThuocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ánh xạ các ID từ layout item_don_thuoc.xml
        private val tvTenThuoc: TextView = itemView.findViewById(R.id.tvTenThuoc)
        private val tvLieuDung: TextView = itemView.findViewById(R.id.tvLieuDung)
        private val tvSoLuong: TextView = itemView.findViewById(R.id.tvSoLuong)

        fun bind(item: PrescriptionItem, clickListener: (PrescriptionItem) -> Unit) {
            // Hiển thị thông tin thuốc từ thực thể PrescriptionItem
            tvTenThuoc.text = item.medicineName
            tvLieuDung.text = "Liều dùng: ${item.dosage}"
            tvSoLuong.text = "Số lượng: ${item.quantity} ${item.unit}"

            itemView.setOnClickListener {
                clickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonThuocViewHolder {
        // Sử dụng layout item_don_thuoc.xml thay vì layout mặc định của Android
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_don_thuoc, parent, false)
        return DonThuocViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonThuocViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    // Lớp hỗ trợ so sánh dữ liệu để cập nhật RecyclerView mượt mà
    class DonThuocDiffCallback : DiffUtil.ItemCallback<PrescriptionItem>() {
        override fun areItemsTheSame(oldItem: PrescriptionItem, newItem: PrescriptionItem): Boolean {
            // Sử dụng itemId chính xác từ thực thể PrescriptionItem
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: PrescriptionItem, newItem: PrescriptionItem): Boolean {
            return oldItem == newItem
        }
    }
}