package ru.cloudtips.sdk.api.models

import com.google.gson.annotations.SerializedName

data class PaymentFee(
	@SerializedName("amountFromPayer") val amountFromPayer: Double?)
