package com.michael.horizontalscrolling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView


class ItemsAdapter(
    private val items: MutableList<String>
) : BaseAdapter<String, ItemsAdapter.ViewHolder>(items = items) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView: View = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(item = items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvItemTitle = itemView.findViewById<AppCompatTextView>(R.id.tvItemTitle)

        fun bind(item: String) {
            tvItemTitle.text = itemView.context.getString(R.string.item, item)
        }
    }

}