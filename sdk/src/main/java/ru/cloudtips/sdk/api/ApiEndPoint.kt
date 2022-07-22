package ru.cloudtips.sdk.api

const val HOST = "https://api.cloudtips.ru/"
const val HOST_PREPROD = "https://api-preprod.cloudtips.ru/"
const val API_URL = "api/"

const val API_ENDPOINT = HOST + API_URL
const val API_ENDPOINT_PREPROD = HOST_PREPROD + API_URL

const val RECAPCHA_V2_TOKEN = "6Ld2OBAaAAAAABt2wqNwimkSNPYh_w0QkUaDO-5L"
const val RECAPCHA_V2_TOKEN_PREPROD = "6LevqioaAAAAACV-3MsrVGj10t9fp4C5phtabS67"

object ApiEndPoint {

    var testMode = false

    fun getUrl(): String {
        return if (testMode) {
            API_ENDPOINT_PREPROD
        } else {
            API_ENDPOINT
        }
    }

    fun getRecapchaV2Token(): String {
        return if (testMode) {
            RECAPCHA_V2_TOKEN_PREPROD
        } else {
            RECAPCHA_V2_TOKEN
        }
    }
}