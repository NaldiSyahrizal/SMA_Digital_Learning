package com.pab.digitallearning.ui.admin.plotting.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.databinding.ItemSearchableBinding

data class SearchableItem(
    val id: Long,
    val name: String,
    var isSelected: Boolean = false
)

class SearchableListAdapter(
    private val isMultiSelect: Boolean,
    private val onItemClick: (SearchableItem) -> Unit
) : RecyclerView.Adapter<SearchableListAdapter.ViewHolder>() {

    private var items = mutableListOf<SearchableItem>()

    fun submitList(newItems: List<SearchableItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<SearchableItem> = items.filter { it.isSelected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvItemName.text = item.name
        
        if (isMultiSelect) {
            holder.binding.cbItemSelect.visibility = View.VISIBLE
            holder.binding.cbItemSelect.isChecked = item.isSelected
        } else {
            holder.binding.cbItemSelect.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (isMultiSelect) {
                item.isSelected = !item.isSelected
                holder.binding.cbItemSelect.isChecked = item.isSelected
            } else {
                // For single select, we just trigger click
            }
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(val binding: ItemSearchableBinding) : RecyclerView.ViewHolder(binding.root)
}
