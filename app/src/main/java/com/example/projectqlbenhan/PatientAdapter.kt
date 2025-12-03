package com.example.projectqlbenhan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class PatientAdapter(
    context: Context,
    private val patients: List<Patient>
) : ArrayAdapter<Patient>(context, 0, patients) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_patient, parent, false)

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvInfo = view.findViewById<TextView>(R.id.tvInfo)

        val p = patients[position]

        tvName.text = p.name
        tvInfo.text = "${p.age} Tuổi - ${p.gender} - ${p.recordId}"

        return view
    }
}