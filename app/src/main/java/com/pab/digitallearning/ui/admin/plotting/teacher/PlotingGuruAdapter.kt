package com.pab.digitallearning.ui.admin.plotting.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.data.model.TeachingAssignment
import com.pab.digitallearning.databinding.ItemTeachingAssignmentBinding

class PlotingGuruAdapter(private val onAction: (Action, TeachingAssignment) -> Unit) :
    ListAdapter<TeachingAssignment, PlotingGuruAdapter.ViewHolder>(DiffCallback()) {

    enum class Action { EDIT, DELETE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeachingAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTeachingAssignmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TeachingAssignment) {
            binding.tvTeacherName.text = item.teacherName
            binding.tvSubjectName.text = item.subjectName
            binding.tvClassName.text = "Kelas: ${item.className}"
            
            binding.btnEdit.setOnClickListener { onAction(Action.EDIT, item) }
            binding.btnDelete.setOnClickListener { onAction(Action.DELETE, item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TeachingAssignment>() {
        override fun areItemsTheSame(oldItem: TeachingAssignment, newItem: TeachingAssignment) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TeachingAssignment, newItem: TeachingAssignment) = oldItem == newItem
    }
}
