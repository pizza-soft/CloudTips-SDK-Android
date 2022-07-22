package ru.cloudtips.sdk.api.models

import com.google.gson.annotations.SerializedName

data class VerifyRequestBody(
		@SerializedName("version") val version: String,
		@SerializedName("token") val token: String,
		@SerializedName("amount") val amount: String?,
		@SerializedName("layoutId") val layoutId: String?)
