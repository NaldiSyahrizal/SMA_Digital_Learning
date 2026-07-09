package com.pab.digitallearning.ui.admin.manage.subject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.R
import com.pab.digitallearning.data.model.Subject
import com.pab.digitallearning.util.setGemoyClick

class SubjectAdapter(
    private val onRowClick: (Subject) -> Unit
) : ListAdapter<Subject, SubjectAdapter.SubjectViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view, onRowClick)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubjectViewHolder(
        itemView: View,
        private val onRowClick: (Subject) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvKode: TextView = itemView.findViewById(R.id.tvKodeMapel)
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaMapel)
        private val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        private val tvTingkatan: TextView = itemView.findViewById(R.id.tvTingkatan)
        private val tvPaket: TextView = itemView.findViewById(R.id.tvNamaPaket)

        fun bind(subject: Subject) {
            tvKode.text = subject.kodeMapel ?: "-"
            tvNama.text = subject.nama ?: "-"
            tvKategori.text = subject.kategori ?: "-"
            tvTingkatan.text = subject.tingkatanName ?: "-"
            tvPaket.text = subject.packages ?: "-"

            // Klik baris dengan efek Gemoy
            itemView.setGemoyClick { onRowClick(subject) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Subject, newItem: Subject) = oldItem == newItem
    }
}
