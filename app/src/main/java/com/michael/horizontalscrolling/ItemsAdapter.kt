package com.michael.horizontalscrolling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections


class ItemsAdapter(
    private val items: MutableList<String>
) : BaseAdapter<String, ItemsAdapter.ViewHolder>(baseItems = items) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView: View = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(item = baseItems[position])
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(baseItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemAdded(position: Int, item: String) {
        baseItems.add(item)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvItemTitle = itemView.findViewById<AppCompatTextView>(R.id.tvItemTitle)
        private val ivDelete = itemView.findViewById<AppCompatImageView>(R.id.ivDelete)

        init {
            ivDelete.setOnClickListener {
                baseItems.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            }
        }

        fun bind(item: String) {
            tvItemTitle.text = itemView.context.getString(R.string.item, item)
        }
    }

}