package com.pab.digitallearning.ui.admin.manage.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.data.model.StudentProfile
import com.pab.digitallearning.databinding.ItemStudentBinding
import com.pab.digitallearning.util.setGemoyClick

class StudentAdapter(private val onAction: (Action, StudentProfile) -> Unit) :
    ListAdapter<StudentProfile, StudentAdapter.ViewHolder>(DiffCallback()) {

    enum class Action { EDIT, DELETE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StudentProfile) {
            binding.tvNama.text = item.namaLengkap ?: "-"
            binding.tvNis.text = item.nis ?: "-"
            binding.tvJk.text = if (item.jenisKelamin == "L") "Laki-laki" else if (item.jenisKelamin == "P") "Perempuan" else item.jenisKelamin ?: "-"
            binding.tvNoTelp.text = item.noTelp ?: "-"
            binding.tvEmail.text = item.email ?: "-"
            
            if (item.isActive == false) {
                binding.tvStatusBadge.visibility = android.view.View.VISIBLE
            } else {
                binding.tvStatusBadge.visibility = android.view.View.GONE
            }
            
            binding.root.setGemoyClick { onAction(Action.EDIT, item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StudentProfile>() {
        override fun areItemsTheSame(oldItem: StudentProfile, newItem: StudentProfile) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StudentProfile, newItem: StudentProfile) = oldItem == newItem
    }
}

