package com.pab.digitallearning.ui.admin.plotting.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.data.model.StudentProfile
import com.pab.digitallearning.databinding.ItemSelectStudentBinding

class SelectStudentAdapter : RecyclerView.Adapter<SelectStudentAdapter.ViewHolder>() {

    private var students = listOf<StudentProfile>()
    private val selectedIds = mutableSetOf<Long>()

    fun submitList(newList: List<StudentProfile>) {
        students = newList
        notifyDataSetChanged()
    }

    fun getSelectedIds(): List<Long> = selectedIds.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student)
    }

    override fun getItemCount() = students.size

    inner class ViewHolder(private val binding: ItemSelectStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: StudentProfile) {
            binding.tvNama.text = student.namaLengkap
            binding.tvNis.text = student.nis
            binding.checkBox.isChecked = selectedIds.contains(student.id)

            binding.root.setOnClickListener {
                if (selectedIds.contains(student.id)) {
                    selectedIds.remove(student.id)
                } else {
                    student.id?.let { selectedIds.add(it) }
                }
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }
}
