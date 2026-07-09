package com.pab.digitallearning.ui.admin.plotting.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.data.model.Classroom
import com.pab.digitallearning.databinding.ItemPlotingClassBinding

class PlotingClassAdapter(
    private var classes: List<Classroom>,
    private val onClick: (Classroom) -> Unit
) : RecyclerView.Adapter<PlotingClassAdapter.ViewHolder>() {

    fun updateData(newClasses: List<Classroom>) {
        this.classes = newClasses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlotingClassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = classes[position]
        holder.binding.tvNamaKelas.text = item.namaKelas
        holder.binding.tvTotalSiswa.text = item.totalSiswa.toString()
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = classes.size

    inner class ViewHolder(val binding: ItemPlotingClassBinding) : RecyclerView.ViewHolder(binding.root)
}
