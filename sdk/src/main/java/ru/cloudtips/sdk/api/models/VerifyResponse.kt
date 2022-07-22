package ru.cloudtips.sdk.api.models

data class VerifyResponse(
    private val status: String?,
    private val token: String?
) {
    fun getToken() = token


    fun isPassed(): Boolean {
        return status.equals("passed", ignoreCase = true)
    }
}