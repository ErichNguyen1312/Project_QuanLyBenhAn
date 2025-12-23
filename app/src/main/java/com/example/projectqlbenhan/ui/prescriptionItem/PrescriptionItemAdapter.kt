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
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<PrescriptionItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMedicineName)
        val tvInfo: TextView = itemView.findViewById(R.id.tvMedicineInfo) // SL + Đơn vị
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteMedicine)

        fun bind(item: PrescriptionItem, position: Int) {
            tvName.text = item.medicineName
            tvInfo.text = "${item.quantity} ${item.unit}"
            tvDosage.text = item.dosage

            btnDelete.setOnClickListener { onDelete(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prescription_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    fun addItem(item: PrescriptionItem) {
        list.add(item)
        notifyItemInserted(list.size - 1)
    }

    fun removeItem(position: Int) {
        if (position in list.indices) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getData(): List<PrescriptionItem> = list
}