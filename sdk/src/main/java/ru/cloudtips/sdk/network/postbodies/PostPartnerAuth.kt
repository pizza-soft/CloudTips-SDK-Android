package ru.cloudtips.sdk.network.postbodies

import com.google.gson.annotations.SerializedName

data class PostPartnerAuth(
    val layoutId: String,
    @SerializedName("cardCryptogramPacket") val cryptogram: String,
    val amount: Double,
    val currency: String = "RUB",
    val feeFromPayer: Boolean,
    val cardholderName: String = "Cloudtips SDK",
    @SerializedName("name") val payerName: String?,
    @SerializedName("comment") val payerComment: String?,
    val payerCity: String?,
    val payerEmail: String?,
    @SerializedName("payerPhoneNumber") val payerPhone: String?,
    val rating: Rating?,
    val captchaVerificationToken: String?
) {
    data class Rating(
        val score: Int?,
        val selectedComponents: List<String>
    )
}


