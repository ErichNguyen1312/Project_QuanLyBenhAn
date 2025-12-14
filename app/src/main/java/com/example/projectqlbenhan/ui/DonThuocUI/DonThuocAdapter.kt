package com.example.projectqlbenhan.ui.DonThuocUI

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.entity.DonThuoc.ChiTietDonThuocEntity

// ------------------------------------

// Sử dụng ChiTietDonThuocEntity
class DonThuocAdapter(
    private val context: Context,
    private val clickListener: (ChiTietDonThuocEntity) -> Unit
) : ListAdapter<ChiTietDonThuocEntity, DonThuocAdapter.DonThuocViewHolder>(DonThuocDiffCallback()) {

    class DonThuocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Tạm thời dùng ID có sẵn (android.R.id.text1)
        private val tvPlaceholder: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(donThuoc: ChiTietDonThuocEntity, clickListener: (ChiTietDonThuocEntity) -> Unit) {
            tvPlaceholder.text = donThuoc.tenThuoc

            itemView.setOnClickListener {
                clickListener(donThuoc)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonThuocViewHolder {
        // Tạm dùng layout đơn giản của Android
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return DonThuocViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonThuocViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class DonThuocDiffCallback : DiffUtil.ItemCallback<ChiTietDonThuocEntity>() {
        override fun areItemsTheSame(oldItem: ChiTietDonThuocEntity, newItem: ChiTietDonThuocEntity): Boolean {
            return oldItem.donThuocId == newItem.donThuocId
        }

        override fun areContentsTheSame(oldItem: ChiTietDonThuocEntity, newItem: ChiTietDonThuocEntity): Boolean {
            return oldItem == newItem
        }
    }
}