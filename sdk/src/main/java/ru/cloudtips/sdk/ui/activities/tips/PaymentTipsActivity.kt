package ru.cloudtips.sdk.ui.activities.tips

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import ru.cloudtips.sdk.R
import ru.cloudtips.sdk.helpers.cleanBackStack
import ru.cloudtips.sdk.helpers.replaceFragment
import ru.cloudtips.sdk.helpers.replaceFragmentWithBackStack
import ru.cloudtips.sdk.ui.activities.tips.fragments.*
import ru.cloudtips.sdk.ui.activities.tips.listeners.IPaymentCardListener
import ru.cloudtips.sdk.ui.activities.tips.listeners.IPaymentInfoListener
import ru.cloudtips.sdk.ui.activities.tips.listeners.IPaymentSuccessListener
import ru.cloudtips.sdk.ui.activities.tips.viewmodels.TipsViewModel

class PaymentTipsActivity : AppCompatActivity(), IPaymentInfoListener, IPaymentCardListener, IPaymentSuccessListener {

    private val viewModel: TipsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_payment_tips)
        startPayment()
    }

    private fun startPayment() {
        val layoutId = intent.getStringExtra(EXTRA_LAYOUT_ID)
        viewModel.requestPaymentPageData(layoutId).observe(this) { response ->
            if (!response.succeed || response.data == null) {
                onShowError()
            }
        }
        replaceFragment(PaymentInfoFragment.newInstance(), R.id.container)
    }

    private fun onShowError() {
        replaceFragment(PaymentErrorFragment.newInstance(), R.id.container)
    }

    override fun onPayInfoClick() {
        replaceFragmentWithBackStack(PaymentCardFragment.newInstance(), R.id.container)
    }

    override fun onPaymentSuccess() {
        onPaymentCardSuccess()
    }

    override fun onPaymentFailure() {
        onPaymentCardFailure()
    }

    override fun onSuccessClick() {
        cleanBackStack()
        startPayment()
    }

    override fun onPaymentCardSuccess() {
        replaceFragment(PaymentSuccessFragment.newInstance(), R.id.container)
    }

    override fun onPaymentCardFailure() {
        replaceFragmentWithBackStack(PaymentFailureFragment.newInstance(), R.id.container)
    }

    override fun onCloseClick() {
        finish()
    }

    companion object {
        private const val EXTRA_LAYOUT_ID = "EXTRA_LAYOUT_ID"
        fun newIntent(context: Context, layoutId: String?) = Intent(context, PaymentTipsActivity::class.java).apply {
            putExtra(EXTRA_LAYOUT_ID, layoutId)
        }
    }

}