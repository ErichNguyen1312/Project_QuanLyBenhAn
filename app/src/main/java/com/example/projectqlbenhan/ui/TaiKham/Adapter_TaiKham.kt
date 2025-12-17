package com.example.projectqlbenhan.ui.TaiKham

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.appointment.Appointment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.compareTo

class Adapter_TaiKham(context : Context, private val data : MutableList<Appointment>, private val onClickEdit : (Appointment) -> Unit)
    : ArrayAdapter<Appointment>(context, 0, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_tai_kham, parent, false)

        val appointment = data[position]

        val btnEdit = view.findViewById<Button>(R.id.btn_edit)
        val txtNgay = view.findViewById<TextView>(R.id.txt_LS_NgayTK)

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val ngay = sdfDate.format(Date(appointment.appointmentDate))
        val gio = appointment.appointmentTime

        txtNgay.text = "$ngay - $gio"

        val lichTaiKham = sdfDateTime.parse("$ngay $gio")!!.time
        val hienTai = System.currentTimeMillis()

        btnEdit.isEnabled = lichTaiKham > hienTai
        btnEdit.alpha = if (btnEdit.isEnabled) 1f else 0.5f

        btnEdit.setOnClickListener {
            onClickEdit(appointment)
        }

        return view
    }

    fun updateData(newData: MutableList<Appointment>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}