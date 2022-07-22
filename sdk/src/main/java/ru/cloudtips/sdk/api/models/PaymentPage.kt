package ru.cloudtips.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class PaymentPage(

    @SerializedName("nameText") val nameText: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("paymentMessage") val paymentMessage: PaymentPageText?,
    @SerializedName("successMessage") val successMessage: PaymentPageText?,
    @SerializedName("amount") val amount: PaymentPageAmount?,
    @SerializedName("payerFee") val payerFee: PayerFee?,
    @SerializedName("availableFields") val availableFields: AvailableFields?,
    @SerializedName("googlePayEnabled") private val googlePayEnabled: Boolean?
) {
    fun getGooglePayEnabled() = googlePayEnabled ?: false
}

data class PaymentPageText(
    @SerializedName("ru") val ru: String?,
    @SerializedName("en") val en: String?
)

data class PaymentPageAmount(val range: PaymentPageRange?)

data class PaymentPageRange(
    val minimal: Double?,
    val maximal: Double?,
    val fixed: Double?,
)

data class PayerFee(
    @SerializedName("enabled") val enabled: Boolean?,
    @SerializedName("initialState") private val initialState: String?
) {
    fun getIsEnabled(): Boolean = initialState == "Enabled"
}

@Parcelize
data class AvailableFields(
    var comment: AvailableFieldsValue?,
    var email: AvailableFieldsValue?,
    var name: AvailableFieldsValue?,
    var payerCity: AvailableFieldsValue?,
    var phoneNumber: AvailableFieldsValue?
) : Parcelable {

    @Parcelize
    data class AvailableFieldsValue(
        var enabled: Boolean?,
        var required: Boolean?
    ) : Parcelable

    enum class FieldNames {
        COMMENT, EMAIL, NAME, CITY, PHONE_NUMBER;
    }
}