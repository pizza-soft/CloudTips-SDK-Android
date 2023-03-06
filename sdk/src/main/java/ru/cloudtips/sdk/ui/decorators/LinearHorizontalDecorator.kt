package ru.cloudtips.sdk.ui.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LinearHorizontalDecorator(private val outerSpacing: Int, private val innerSpacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val lastIndex = (parent.adapter?.itemCount ?: 0) - 1
        if (itemPosition == RecyclerView.NO_POSITION) return
        if (itemPosition == 0) outRect.left = outerSpacing else outRect.left = innerSpacing / 2
        if (itemPosition == lastIndex) outRect.right = outerSpacing else outRect.right = outerSpacing / 2
    }
}