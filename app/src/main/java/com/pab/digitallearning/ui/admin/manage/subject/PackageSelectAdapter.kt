package com.pab.digitallearning.ui.admin.manage.subject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.R
import com.pab.digitallearning.data.model.Package

class PackageSelectAdapter(
    private val allPackages: List<Package>,
    initialSelected: List<Long>
) : RecyclerView.Adapter<PackageSelectAdapter.ViewHolder>() {

    private val selectedIds = initialSelected.toMutableSet()
    private var displayedPackages: List<Package> = allPackages

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_package_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pkg = displayedPackages[position]
        holder.bind(pkg)
    }

    override fun getItemCount() = displayedPackages.size

    fun getSelectedIds(): List<Long> = selectedIds.toList()
    
    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase()
        displayedPackages = if (lowerCaseQuery.isEmpty()) {
            allPackages
        } else {
            allPackages.filter { it.namaPaket?.lowercase()?.contains(lowerCaseQuery) == true }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cb: CheckBox = itemView.findViewById(R.id.cbPackage)
        private val tvName: TextView = itemView.findViewById(R.id.tvPackageName)
        private val tvTingkatan: TextView = itemView.findViewById(R.id.tvPackageTingkatan)

        fun bind(pkg: Package) {
            tvName.text = pkg.namaPaket
            tvTingkatan.text = pkg.tingkatanName ?: "Tingkatan -"
            
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = selectedIds.contains(pkg.id)

            itemView.setOnClickListener {
                cb.isChecked = !cb.isChecked
                pkg.id?.let { updateSelection(it, cb.isChecked) }
            }

            cb.setOnCheckedChangeListener { _, isChecked ->
                pkg.id?.let { updateSelection(it, isChecked) }
            }
        }

        private fun updateSelection(id: Long, isSelected: Boolean) {
            if (isSelected) selectedIds.add(id)
            else selectedIds.remove(id)
        }
    }
}
