package ru.cloudtips.sdk.api.models

import com.google.gson.annotations.SerializedName

data class PaymentRequestBody(
		@SerializedName("cardholderName") val cardholderName: String = "Cloudtips SDK",
		@SerializedName("cardCryptogramPacket") val cryptogram: String?,
		@SerializedName("amount") val amount: String?,
		@SerializedName("currency") val currency: String = "RUB",
		@SerializedName("comment") val comment: String? = "",
		@SerializedName("layoutId") val layoutId: String?,
		@SerializedName("feeFromPayer") val feeFromPayer: Boolean,
		@SerializedName("captchaVerificationToken") val captchaVerificationToken: String?)