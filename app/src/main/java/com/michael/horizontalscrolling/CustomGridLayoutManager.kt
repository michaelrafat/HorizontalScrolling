package com.michael.horizontalscrolling

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler


class CustomGridLayoutManager<T, D : RecyclerView.ViewHolder?>(
    private val columns: Int,
    private val rows: Int,
    private val reverseLayout: Boolean = false,
    private val adapter: BaseAdapter<T, D>
) : LayoutManager() {

    private var horizontalScrollOffset = 0
    private var verticalScrollOffset = 0

    private val viewWidth by lazy { width / columns }
    private val viewHeight by lazy { height / rows }
    private var lastVisibleItem = 0
    private var currentPage = 1

    init {
        adapter.pageSize = rows * columns
    }

    override fun onAttachedToWindow(recyclerview: RecyclerView?) {
        super.onAttachedToWindow(recyclerview)
        if (reverseLayout) {
            recyclerview?.rotationY = 180f
        }
        recyclerview?.let { detectScrolling(it) }
    }

    override fun generateDefaultLayoutParams() =
        RecyclerView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun addView(child: View?, index: Int) {
        super.addView(child, index)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        addPagesItems(recycler)
        lastVisibleItem = getChildAt(getLastColumnIndex())?.let { getDecoratedLeft(it) } ?: 0
    }

    private fun addPagesItems(recycler: Recycler) {
        detachAndScrapAttachedViews(recycler)
        for (pageIndex in 0 until adapter.getPagesCount()) {
            for (rowIndex in 0 until rows) {
                for (columnIndex in 0 until columns) {
                    val left =
                        (columnIndex * viewWidth - horizontalScrollOffset) + width * pageIndex
                    val right = left + viewWidth
                    val top = 0
                    val bottom = ((rowIndex + 1) * viewHeight)
                    val position = (pageIndex * rows * columns) + (rowIndex * columns) + columnIndex
                    if (position >= itemCount) {
                        return
                    }
                    val view = recycler.getViewForPosition(position)
                    addView(view)
                    view.setMargins(10, viewHeight * rowIndex, 10, 10)
                    if (reverseLayout) {
                        view.rotationY = 180f
                    }
                    measureChildWithMargins(view, viewWidth, viewHeight)
                    layoutDecoratedWithMargins(view, left, top, right, bottom)
                }
            }
        }
        val scrapListCopy = recycler.scrapList.toList()
        scrapListCopy.forEach {
            recycler.recycleView(it.itemView)
        }
    }

    private fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        if (layoutParams is MarginLayoutParams) {
            val params = layoutParams as MarginLayoutParams
            params.setMargins(left, top, right, bottom)
            requestLayout()
        }
    }

    override fun canScrollVertically() = true

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State?): Int {
        if (itemCount == 0) return 0
        verticalScrollOffset += dy
        return dy
    }

    override fun canScrollHorizontally() = true

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {

        if (itemCount == 0) return 0

        val offset: Int
        val startItem = 0

        if (dx + horizontalScrollOffset < startItem) {
            offset = horizontalScrollOffset
            horizontalScrollOffset = startItem
        } else if (dx + horizontalScrollOffset > lastVisibleItem) {
            offset = lastVisibleItem - horizontalScrollOffset
            horizontalScrollOffset = lastVisibleItem
        } else {
            offset = dx
            horizontalScrollOffset += dx
        }

        addPagesItems(recycler)
        return offset
    }

    private fun getLastColumnIndex(): Int {
        val lastColumnIndex = (adapter.getPagesCount() - 1) * (rows * columns)
        if (lastColumnIndex > itemCount - 1) {
            return itemCount - 1
        }
        return lastColumnIndex
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        val smoothScroller = object : LinearSmoothScroller(recyclerView?.context) {
            override fun getHorizontalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    fun scrollToPage(page: Int, recyclerView: RecyclerView) {
        val item = (page - 1) * columns * rows
        smoothScrollToPosition(recyclerView = recyclerView, state = RecyclerView.State(), item)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun detectScrolling(recyclerView: RecyclerView) {
        recyclerView.setOnTouchListener(
            object : OnSwipeTouchHelper(recyclerView.context) {
                override fun onSwipeLeft() {
                    currentPage++
                    if (currentPage < adapter.getPagesCount()) {
                        scrollToPage(currentPage, recyclerView)
                    } else {
                        currentPage = adapter.getPagesCount() - 2
                    }
                }

                override fun onSwipeRight() {
                    currentPage--
                    if (currentPage >= 0) {
                        scrollToPage(currentPage, recyclerView)
                    } else {
                        currentPage = 0
                    }
                }
            })
    }

}