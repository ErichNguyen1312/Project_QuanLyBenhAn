package com.example.projectqlbenhan.ui.TaiKham

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Adapter_TaiKham(
    context: Context,
    private val data: MutableList<Appointment>,
    private val doctorMap: Map<Long, String>,
    private val onClickEdit: (Appointment) -> Unit
) : ArrayAdapter<Appointment>(context, 0, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_tai_kham, parent, false)

        val appointment = data[position]

        val btnEdit = view.findViewById<Button>(R.id.btn_edit)
        val txtNgay = view.findViewById<TextView>(R.id.txt_LS_NgayTK)
        val txtBacSi = view.findViewById<TextView>(R.id.txt_TenBacSi) // ID mới thêm trong XML

        // Format ngày giờ từ Long
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        txtNgay.text = sdf.format(Date(appointment.appointmentDate))

        // Hiển thị tên bác sĩ từ Map
        val doctorName = doctorMap[appointment.doctorId] ?: "Chưa chỉ định"
        txtBacSi.text = "BS. $doctorName"

        // Logic ẩn/hiện nút sửa (Chỉ cho sửa lịch sắp tới)
        val hienTai = System.currentTimeMillis()
        val isUpcoming = appointment.appointmentDate > hienTai && appointment.status == "SCHEDULED"

        btnEdit.isEnabled = isUpcoming
        btnEdit.alpha = if (isUpcoming) 1f else 0.5f

        // Đổi màu trạng thái nếu cần
        if (appointment.status == "MISSED" || appointment.status == "CANCELLED") {
            txtNgay.setTextColor(Color.RED)
        } else {
            txtNgay.setTextColor(Color.BLACK)
        }

        btnEdit.setOnClickListener {
            onClickEdit(appointment)
        }

        return view
    }
}