package com.michael.horizontalscrolling

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, V : RecyclerView.ViewHolder?>(
    private var items: List<T>,
    var pageSize: Int? = null
) : RecyclerView.Adapter<V>() {

    fun getItems() = items

    fun getPagesCount() = items.chunked(pageSize ?: 1).size

    override fun getItemCount() = items.size

}