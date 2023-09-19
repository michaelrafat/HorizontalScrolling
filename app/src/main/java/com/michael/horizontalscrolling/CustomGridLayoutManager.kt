package com.michael.horizontalscrolling

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import kotlin.math.ceil
import kotlin.math.roundToInt


class CustomGridLayoutManager<T, D : RecyclerView.ViewHolder?>(
    private val columns: Int,
    private val rows: Int,
    private val reverseLayout: Boolean = false,
    private var swipeAsPages: Boolean = true,
    private var enableDragAndDrop: Boolean = true,
    private val adapter: BaseAdapter<T, D>
) : LayoutManager() {

    private var horizontalScrollOffset = 0
    private var verticalScrollOffset = 0

    private val viewWidth by lazy { width / columns }
    private val viewHeight by lazy { height / rows }
    private var lastVisibleItem = 0
    private var currentPage = 1
    private var totalPages = adapter.getPagesCount()

    init {
        adapter.pageSize = rows * columns
    }

    override fun onAttachedToWindow(recyclerview: RecyclerView?) {
        super.onAttachedToWindow(recyclerview)
        recyclerview?.let {
            if (reverseLayout) {
                it.rotationY = 180f
            }
            if (swipeAsPages) {
                detectScrolling(it)
            }
            if (enableDragAndDrop) {
                ItemTouchHelper(setupDragAndDropFeature()).attachToRecyclerView(it)
            }
        }
    }

    override fun generateDefaultLayoutParams() =
        RecyclerView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        addPagesItems(recycler)
        if (lastVisibleItem == 0) {
            lastVisibleItem = getChildAt(getLastColumnIndex())?.let { getDecoratedLeft(it) } ?: 0
        }
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        val current = currentPage
        currentPage = ceil((itemCount).toDouble() / (adapter.pageSize ?: 1)).roundToInt()
        if (currentPage > current) {
            lastVisibleItem += width
            scrollToPage(currentPage, recyclerView)
        } else if (currentPage < current) {
            lastVisibleItem -= width
        } else {
            scrollToPage(getLastColumnIndex(), recyclerView)
        }
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        val current = currentPage
        currentPage = ceil((positionStart).toDouble() / (adapter.pageSize ?: 1)).roundToInt()
        if (currentPage < current) {
            lastVisibleItem -= width
            scrollToPage(currentPage, recyclerView)
        } else {
            scrollToPage(getLastColumnIndex(), recyclerView)
        }
    }

    private fun addPagesItems(recycler: Recycler) {
        detachAndScrapAttachedViews(recycler)
        for (pageIndex in 0 until adapter.getPagesCount()) {
            for (rowIndex in 0 until rows) {
                for (columnIndex in 0 until columns) {
                    val left =
                        (columnIndex * viewWidth - horizontalScrollOffset) + width * pageIndex
                    val right = left + viewWidth
                    val top = viewHeight * rowIndex
                    val bottom = ((rowIndex + 1) * viewHeight)
                    val position = (pageIndex * rows * columns) + (rowIndex * columns) + columnIndex
                    if (position >= itemCount) {
                        return
                    }
                    val view = recycler.getViewForPosition(position)
                    addView(view)
                    view.setMargins(5, 5, 5, 5)
                    view.setRecyclerViewItemShapeParam(viewWidth, viewHeight)
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

    private fun View.setRecyclerViewItemShapeParam(width: Int, height: Int) {
        val layoutParams = this.layoutParams as RecyclerView.LayoutParams
        layoutParams.width = width
        layoutParams.height = height
        this.layoutParams = layoutParams
        requestLayout()
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

        when (swipeAsPages) {

            true -> {
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
            }

            false -> {
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
            }
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

    fun nextPage(recyclerView: RecyclerView) {
        if (currentPage < totalPages) {
            currentPage++
            scrollToPage(currentPage, recyclerView)
        }
    }

    fun previousPage(recyclerView: RecyclerView) {
        if (currentPage > 1) {
            currentPage--
            scrollToPage(currentPage, recyclerView)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun detectScrolling(recyclerView: RecyclerView) {

        recyclerView.setOnTouchListener(
            object : OnSwipeTouchHelper(recyclerView.context) {
                override fun onSwipeLeft() {
                    if (currentPage < adapter.getPagesCount()) {
                        currentPage++
                        scrollToPage(currentPage, recyclerView)
                    } else {
                        currentPage--
                    }
                }

                override fun onSwipeRight() {
                    if (currentPage > 1) {
                        currentPage--
                        scrollToPage(currentPage, recyclerView)
                    } else {
                        currentPage++
                    }
                }
            })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // a very slow dx offset on scrolling to be different than the touch listener above (acting as a snap helper)
                if (currentPage > 1 && currentPage <= adapter.getPagesCount() && dx <= 2 && dx >= -2) {
                    val offset = horizontalScrollOffset + dx
                    val diff = offset - ((currentPage - 1) * width)
                    if (diff > 0 && diff > width / 2) {
                        currentPage++
                    } else if (diff < 0 && -diff > width / 2) {
                        currentPage--
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        scrollToPage(currentPage, recyclerView)
                    }, 50)
                }
            }
        })
    }

    private fun setupDragAndDropFeature(): ItemTouchHelper.Callback {

        val callback: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {

            override fun isLongPressDragEnabled() = true

            override fun isItemViewSwipeEnabled() = true

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags =
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                val swipeFlags =
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                val currentPageOffset = ceil(
                    (toPosition + 1) / (rows * columns).toDouble()
                ).roundToInt()

                if (currentPage != currentPageOffset) {
                    currentPage = if (currentPageOffset > adapter.getPagesCount()) {
                        adapter.getPagesCount()
                    } else {
                        currentPageOffset
                    }
                }

                adapter.onItemMoved(fromPosition, toPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
                if (swipeAsPages) {
                    scrollToPage(currentPage, recyclerView)
                }
            }

        }

        return callback
    }

}