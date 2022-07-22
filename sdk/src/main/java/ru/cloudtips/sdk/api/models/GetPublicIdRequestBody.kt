package ru.cloudtips.sdk.api.models

import com.google.gson.annotations.SerializedName

data class GetPublicIdRequestBody(
	@SerializedName("layoutId") val layoutId: String?)
