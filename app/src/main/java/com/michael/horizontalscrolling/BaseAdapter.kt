package com.michael.horizontalscrolling

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, V : RecyclerView.ViewHolder?>(
    open val baseItems: MutableList<T>,
    var pageSize: Int? = null
) : RecyclerView.Adapter<V>() {

    fun getItems() = baseItems

    fun getPagesCount() = baseItems.chunked(pageSize ?: 1).size

    override fun getItemCount() = baseItems.size

    abstract fun onItemMoved(fromPosition: Int, toPosition: Int)

    abstract fun onItemAdded(position: Int, item: String)

}