package ru.cloudtips.sdk.api.models

data class PaymentResponse(
    private val statusCode: String?,
    val transactionId: String?,
    val acsUrl: String?,
    val paReq: String?,
    val md: String?,
    val status: String?,
    val title: String?,
    val detail: String?
) {

    fun getStatusCode(): PaymentResponseStatus {
        return PaymentResponseStatus.getByString(statusCode)
    }

}

enum class PaymentResponseStatus {
    NEED_3DS,
    SUCCESS,
    UNKNOWN;

    companion object {
        fun getByString(value: String?): PaymentResponseStatus {
            if (value.equals("Need3ds", true)) return NEED_3DS
            if (value.equals("Success", true)) return SUCCESS
            return UNKNOWN
        }
    }

}
