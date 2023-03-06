package ru.cloudtips.sdk.ui.activities.tips.listeners

interface IPaymentCardListener : IHeaderCloseListener {
    fun onPaymentCardSuccess()
    fun onPaymentCardFailure()
}