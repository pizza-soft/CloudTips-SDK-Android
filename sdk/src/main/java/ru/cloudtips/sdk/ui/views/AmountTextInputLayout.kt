package ru.cloudtips.sdk.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

class AmountTextInputLayout : TextInputLayout {

    private val CURRENCY_SUFFIX = " ₽"
    private val currencyWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        private var current: String = ""
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val text = s.toString()
            if (text != current) {
                editText?.removeTextChangedListener(this)

                val rawValue = text.replace(CURRENCY_SUFFIX, "")
                if (rawValue.isEmpty()) {
                    editText?.setText("")
                } else {
                    val formatted = rawValue + CURRENCY_SUFFIX

                    current = formatted
                    editText?.setText(formatted)
                    editText?.setSelection(formatted.length - CURRENCY_SUFFIX.length)

                }
                editText?.addTextChangedListener(this)
            }
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    }

    private var isInputEnabled = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        addOnEditTextAttachedListener {
            editText?.addTextChangedListener(currencyWatcher)
            editText?.setOnKeyListener { p0, p1, p2 ->
                !isInputEnabled
            }
        }
    }

    fun setInputEnabled(value: Boolean) {
        isInputEnabled = value
    }

    fun getText(): String? {
        return editText?.text?.toString()?.replace(CURRENCY_SUFFIX, "")
    }

    fun setText(value: String?) {
        editText?.setText(value)
    }

    fun doAfterTextChanged(action: (Editable?) -> Unit) {
        editText?.doAfterTextChanged(action)
    }

}
