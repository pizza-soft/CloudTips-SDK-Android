package ru.cloudtips.sdk.ui.elements

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class ClickableUrlSpan(private val url: String, private val listener: ISpanUrlClick?, private var isUnderline: Boolean = false) : ClickableSpan() {

    override fun onClick(view: View) {
        listener?.onUrlClick(url)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = isUnderline
    }

    interface ISpanUrlClick {
        fun onUrlClick(url: String?)
    }
}