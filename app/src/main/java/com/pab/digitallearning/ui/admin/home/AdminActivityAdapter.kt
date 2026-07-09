package com.pab.digitallearning.ui.admin.home

import android.graphics.Color
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.R
import com.pab.digitallearning.databinding.ItemAdminActivityBinding
import com.pab.digitallearning.util.AdminActivity

class AdminActivityAdapter : ListAdapter<AdminActivity, AdminActivityAdapter.ActivityViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemAdminActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityViewHolder(
        private val binding: ItemAdminActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminActivity) {
            binding.tvActivityTitle.text = item.title
            binding.tvActivityTime.text = item.timestamp

            // Dynamic styling based on activity type
            val (bgColor, iconRes, tintColor) = when (item.type) {
                "add" -> Triple(
                    Color.parseColor("#E6F9F0"),
                    R.drawable.ic_add,
                    Color.parseColor("#00C49F")
                )
                "edit" -> Triple(
                    Color.parseColor("#E8EEF9"),
                    R.drawable.ic_plotting,
                    Color.parseColor("#102B5E")
                )
                "delete" -> Triple(
                    Color.parseColor("#FFF0F2"),
                    R.drawable.ic_error_circle,
                    Color.parseColor("#FF4A5F")
                )
                "security" -> Triple(
                    Color.parseColor("#FFF7E6"),
                    R.drawable.ic_person,
                    Color.parseColor("#FF9F1C")
                )
                else -> Triple(
                    Color.parseColor("#EEF2F9"),
                    R.drawable.ic_notifications,
                    Color.parseColor("#5C6F94")
                )
            }

            binding.iconContainer.setCardBackgroundColor(bgColor)
            binding.ivIcon.setImageResource(iconRes)
            binding.ivIcon.imageTintList = ColorStateList.valueOf(tintColor)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AdminActivity>() {
            override fun areItemsTheSame(oldItem: AdminActivity, newItem: AdminActivity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: AdminActivity, newItem: AdminActivity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
