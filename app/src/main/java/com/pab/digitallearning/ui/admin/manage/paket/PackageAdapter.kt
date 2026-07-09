package com.pab.digitallearning.ui.admin.manage.paket

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.R
import com.pab.digitallearning.data.model.Package
import com.pab.digitallearning.util.setGemoyClick

class PackageAdapter(
    private val onRowClick: (Package) -> Unit
) : ListAdapter<Package, PackageAdapter.PackageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_package, parent, false)
        return PackageViewHolder(view, onRowClick)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PackageViewHolder(
        itemView: View,
        private val onRowClick: (Package) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaPaket)
        private val tvJurusan: TextView = itemView.findViewById(R.id.tvJurusan)
        private val tvTingkatan: TextView = itemView.findViewById(R.id.tvTingkatan)
        private val tvDeskripsi: TextView = itemView.findViewById(R.id.tvDeskripsi)
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)

        fun bind(pkg: Package) {
            tvNama.text = pkg.namaPaket ?: "-"
            tvJurusan.text = pkg.jurusan ?: "-"
            tvTingkatan.text = pkg.tingkatanName ?: "-"
            tvDeskripsi.text = pkg.deskripsi ?: "-"
            tvTanggal.text = pkg.createdAt?.take(10) ?: "-"

            // Klik seluruh baris dengan efek Gemoy
            itemView.setGemoyClick { onRowClick(pkg) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Package>() {
        override fun areItemsTheSame(oldItem: Package, newItem: Package) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Package, newItem: Package) = oldItem == newItem
    }
}
