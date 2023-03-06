package ru.cloudtips.sdk.network.models

data class PaymentAuthData(
    val acsUrl: String?,
    val md: String?,
    val paReq: String?,
    private val statusCode: String?
) {

    fun getStatusCode(): PaymentAuthStatusCode {
        return PaymentAuthStatusCode.getByName(statusCode)
    }

}

enum class PaymentAuthStatusCode {
    NEED3DS,
    SUCCESS,
    UNKNOWN;

    companion object {
        fun getByName(value: String?): PaymentAuthStatusCode {
            return values().find { it.name.equals(value, true) } ?: UNKNOWN
        }
    }

}