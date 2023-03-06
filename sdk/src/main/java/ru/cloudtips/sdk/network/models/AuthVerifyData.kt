package ru.cloudtips.sdk.network.models

data class AuthVerifyData(
    private val status: String?,
    private val token: String?
) {
    fun getToken() = token
    fun getStatus() = AuthVerifyStatus.getByString(status)

    enum class AuthVerifyStatus {
        SUCCESS, FAILURE;

        companion object {
            fun getByString(value: String?) {
                if (value.equals("passed", true)) SUCCESS else FAILURE
            }
        }
    }
}