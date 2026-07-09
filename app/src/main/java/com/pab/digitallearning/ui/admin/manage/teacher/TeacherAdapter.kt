package com.pab.digitallearning.ui.admin.manage.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.data.model.TeacherProfile
import com.pab.digitallearning.databinding.ItemTeacherBinding
import com.pab.digitallearning.util.setGemoyClick

class TeacherAdapter(private val onAction: (Action, TeacherProfile) -> Unit) :
    ListAdapter<TeacherProfile, TeacherAdapter.ViewHolder>(DiffCallback()) {

    enum class Action { EDIT, DELETE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeacherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTeacherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TeacherProfile) {
            binding.tvNama.text = item.namaLengkap ?: "-"
            binding.tvNip.text = item.nip ?: "-"
            binding.tvJk.text = if (item.jenisKelamin == "L") "Laki-laki" else if (item.jenisKelamin == "P") "Perempuan" else item.jenisKelamin ?: "-"
            binding.tvNoTelp.text = item.noTelp ?: "-"
            binding.tvEmail.text = item.email ?: "-"
            
            binding.root.setGemoyClick { onAction(Action.EDIT, item) } // Placeholder for click, will be updated to bottom sheet in fragment
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TeacherProfile>() {
        override fun areItemsTheSame(oldItem: TeacherProfile, newItem: TeacherProfile) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TeacherProfile, newItem: TeacherProfile) = oldItem == newItem
    }
}
