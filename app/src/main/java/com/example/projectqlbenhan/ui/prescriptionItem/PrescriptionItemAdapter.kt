package com.example.projectqlbenhan.ui.medicalRecord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem

class PrescriptionItemAdapter(
    private val list: MutableList<PrescriptionItem>,
    private val onItemClick: (PrescriptionItem) -> Unit, // Callback để xem chi tiết/toa thuốc
    private val onDeleteClick: (Int) -> Unit             // Callback để xóa thuốc theo vị trí
) : RecyclerView.Adapter<PrescriptionItemAdapter.ViewHolder>() {

    /**
     * ViewHolder ánh xạ các View từ file item_prescription_item.xml
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMedicineName)
        val tvInfo: TextView = itemView.findViewById(R.id.tvMedicineInfo)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteMedicine)

        fun bind(item: PrescriptionItem, position: Int) {
            // Gán dữ liệu lên giao diện
            tvName.text = item.medicineName
            tvInfo.text = "${item.quantity} ${item.unit}"
            tvDosage.text = item.dosage

            // ⭐ Xử lý click vào TOÀN BỘ dòng thuốc để xem toa thuốc chi tiết
            itemView.setOnClickListener {
                onItemClick(item)
            }

            // ⭐ Xử lý click vào nút XÓA
            btnDelete.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Nạp layout item_prescription_item.xml mà bạn đã tạo
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prescription_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    /**
     * Cập nhật toàn bộ danh sách khi dữ liệu thay đổi
     */
    fun updateData(newList: List<PrescriptionItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}