package com.pab.digitallearning.ui.admin.plotting.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.data.model.StudentClassroom
import com.pab.digitallearning.databinding.ItemPlotingSiswaBinding

class PlotingSiswaAdapter(private val onAction: (Action, StudentClassroom) -> Unit) :
    ListAdapter<StudentClassroom, PlotingSiswaAdapter.ViewHolder>(DiffCallback()) {

    enum class Action { EDIT, DELETE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlotingSiswaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPlotingSiswaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StudentClassroom) {
            binding.tvId.text = item.id.toString()
            binding.tvNis.text = item.student?.nis ?: "-"
            binding.tvNamaMurid.text = item.student?.namaLengkap ?: "-"
            binding.tvNamaKelas.text = item.classroom?.namaKelas ?: "-"

            binding.btnEdit.setOnClickListener { onAction(Action.EDIT, item) }
            binding.btnDelete.setOnClickListener { onAction(Action.DELETE, item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StudentClassroom>() {
        override fun areItemsTheSame(oldItem: StudentClassroom, newItem: StudentClassroom) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StudentClassroom, newItem: StudentClassroom) = oldItem == newItem
    }
}
