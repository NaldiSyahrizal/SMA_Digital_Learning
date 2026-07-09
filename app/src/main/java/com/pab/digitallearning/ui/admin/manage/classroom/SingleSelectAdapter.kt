package com.pab.digitallearning.ui.admin.manage.classroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pab.digitallearning.R

class SingleSelectAdapter(
    private var items: List<Pair<String, Long>>,
    private val onItemSelected: (String, Long) -> Unit
) : RecyclerView.Adapter<SingleSelectAdapter.ViewHolder>() {

    private var filteredItems = items.toList()

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter { it.first.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_single_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.tvOptionText.text = item.first
        
        holder.itemView.setOnClickListener {
            onItemSelected(item.first, item.second)
        }
    }

    override fun getItemCount() = filteredItems.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOptionText: TextView = view.findViewById(R.id.tvOptionText)
    }
}
