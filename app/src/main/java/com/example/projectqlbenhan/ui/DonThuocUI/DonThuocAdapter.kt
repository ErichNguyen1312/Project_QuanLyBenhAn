package com.example.projectqlbenhan.ui.DonThuocUI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.prescriptionItem.PrescriptionItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter sử dụng ListAdapter kết hợp DiffUtil để tự động nhận diện thay đổi dữ liệu.
 */
class DonThuocAdapter(
    private val onClick: (PrescriptionItem) -> Unit,
    private val onDelete: (PrescriptionItem) -> Unit
) : ListAdapter<PrescriptionItem, DonThuocAdapter.ViewHolder>(DonThuocDiffCallback()) {

    /**
     * ViewHolder ánh xạ các View từ file item_don_thuoc.xml
     */
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTen: TextView = v.findViewById(R.id.tvTenThuoc)
        private val tvLieu: TextView = v.findViewById(R.id.tvLieuDung)
        private val tvNgay: TextView = v.findViewById(R.id.tvNgayKe)
        private val tvSL: TextView = v.findViewById(R.id.tvSoLuong)

        // Sử dụng Safe Call (?) để tránh crash NullPointerException nếu XML chưa có ID btnXoa
        private val btnDel: ImageView? = v.findViewById(R.id.btnXoa)

        fun bind(item: PrescriptionItem) {
            // Hiển thị tên thuốc và thông tin cơ bản
            tvTen.text = item.medicineName
            tvLieu.text = "Liều dùng: ${item.dosage}"
            tvSL.text = "${item.quantity} ${item.unit}"

            // Định dạng ngày tháng kê thuốc
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(item.createdAt))
            tvNgay.text = "Ngày kê: $dateStr"

            // Sự kiện click vào toàn bộ item để mở màn hình chỉnh sửa (SuaDonThuoc)
            itemView.setOnClickListener {
                onClick(item)
            }

            // Sự kiện click vào icon xóa (Thùng rác)
            btnDel?.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Nạp giao diện item_don_thuoc.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_don_thuoc, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Lấy dữ liệu và thực hiện hiển thị thông qua hàm bind
        val item = getItem(position)
        holder.bind(item)
    }

    /**
     * Thành phần quan trọng nhất để fix lỗi KHÔNG UPDATE tên thuốc:
     * DiffUtil so sánh dữ liệu cũ và mới để ép RecyclerView vẽ lại đúng dòng bị sửa.
     */
    class DonThuocDiffCallback : DiffUtil.ItemCallback<PrescriptionItem>() {

        // Kiểm tra xem có phải cùng 1 loại thuốc không (dựa trên ID)
        override fun areItemsTheSame(oldItem: PrescriptionItem, newItem: PrescriptionItem): Boolean {
            return oldItem.itemId == newItem.itemId
        }

        // So sánh nội dung bên trong (Tên thuốc, liều dùng...)
        // Nếu tên thuốc thay đổi, hàm này trả về FALSE -> RecyclerView sẽ cập nhật lại chữ trên màn hình.
        override fun areContentsTheSame(oldItem: PrescriptionItem, newItem: PrescriptionItem): Boolean {
            // Yêu cầu: PrescriptionItem phải là một 'data class'
            return oldItem == newItem
        }
    }
}