package com.example.projectqlbenhan.ui.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectqlbenhan.R
import com.example.projectqlbenhan.entity.patient.Patient
import java.util.Calendar

class PatientAdapterRecycler (
    private val patients: List<Patient>,
    private val onClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapterRecycler.PatientViewHolder>()
{
    inner class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private var tvName: TextView = itemView.findViewById(R.id.tvName)
        private var tvInfo: TextView = itemView.findViewById(R.id.tvInfo)

        fun bind(patient: Patient) {
            val name = patient.fullName ?: "?"

            val handleName  = getAvatarText(name)

            // Avatar = chữ cái đầu
            tvAvatar.text = handleName

            tvName.text = name
            tvInfo.text =
                "${patient.calculateAge(patient.dateOfBirth)} Tuổi · ${patient.gender} · ${patient.medicalRecordNumber}"

            itemView.setOnClickListener {
                onClick(patient)
            }
        }

    }

    private fun getAvatarText(fullName: String): String {
        val parts = fullName
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }

        return when (parts.size) {
            0 -> "?"
            1 -> parts[0].first().uppercaseChar().toString()
            else -> {
                val firstChar = parts.first().first().uppercaseChar()
                val lastChar = parts.last().first().uppercaseChar()
                "$firstChar$lastChar"
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }
    override fun getItemCount(): Int = patients.size


}