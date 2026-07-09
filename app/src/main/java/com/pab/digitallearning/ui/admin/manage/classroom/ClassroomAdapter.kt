package com.pab.digitallearning.ui.admin.manage.classroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.data.model.Classroom
import com.pab.digitallearning.databinding.ItemClassroomBinding

class ClassroomAdapter(
    private var classes: List<Classroom>,
    private val onClick: (Classroom) -> Unit
) : RecyclerView.Adapter<ClassroomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClassroomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = classes[position]
        holder.binding.tvNamaKelas.text = item.namaKelas
        holder.binding.tvTingkatan.text = item.tingkatanName ?: "-"
        holder.binding.tvWaliKelas.text = item.waliKelasName ?: "Belum ada wali"
        
        holder.binding.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = classes.size

    fun updateList(newClasses: List<Classroom>) {
        classes = newClasses
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemClassroomBinding) : RecyclerView.ViewHolder(binding.root)
}
