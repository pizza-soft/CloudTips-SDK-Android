package ru.cloudtips.sdk.ui.activities.tips.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.cloudtips.sdk.R
import ru.cloudtips.sdk.databinding.FragmentPaymentErrorBinding
import ru.cloudtips.sdk.ui.activities.tips.listeners.IHeaderCloseListener

class PaymentErrorFragment : Fragment(R.layout.fragment_payment_error) {

    private val viewBinding: FragmentPaymentErrorBinding by viewBinding()

    private var listener: IHeaderCloseListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? IHeaderCloseListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            headerCloseButton.setOnClickListener {
                listener?.onCloseClick()
            }
            mainButton.setOnClickListener {
                listener?.onCloseClick()
            }
        }
    }

    companion object {
        fun newInstance() = PaymentErrorFragment()
    }
}