package ru.cloudtips.sdk.api.models

import com.google.gson.annotations.SerializedName

data class PostThreeDsRequestBody(
        @SerializedName("MD") val md: String,
        @SerializedName("PaRes") val paRes: String)