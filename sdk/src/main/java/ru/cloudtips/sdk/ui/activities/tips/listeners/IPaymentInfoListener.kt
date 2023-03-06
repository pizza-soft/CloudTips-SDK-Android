package ru.cloudtips.sdk.ui.activities.tips.listeners

interface IPaymentInfoListener : IHeaderCloseListener {
    fun onPayInfoClick()
    fun onPaymentSuccess()
    fun onPaymentFailure()
}