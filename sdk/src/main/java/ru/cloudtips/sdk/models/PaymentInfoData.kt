package ru.cloudtips.sdk.models

data class PaymentInfoData(
    private var amount: Double,
    private var feeAmount: Double,
    var feeFromPayer: Boolean,
    val sender: PaymentInfoSenderData,
    val rating: PaymentInfoRatingData
) {
    fun getAmount(): Double = amount
    fun getAmountWithFee(): Double = if (feeFromPayer) feeAmount else amount
    fun setAmount(value: Double) { amount = value }
    fun setFeeAmount(value: Double) { feeAmount = value }
}

data class PaymentInfoSenderData(var name: String?, var email: String?, var phone: String?, var city: String?, var comment: String?)

data class PaymentInfoRatingData(var score: Int, val components: MutableList<String>)

data class PaymentCardData(val number: String?, val date: String?, val cvc: String?)