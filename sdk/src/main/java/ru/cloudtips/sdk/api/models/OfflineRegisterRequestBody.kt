package ru.cloudtips.sdk.api.models

import com.google.gson.annotations.SerializedName

data class OfflineRegisterRequestBody(
	@SerializedName("phoneNumber") val phoneNumber: String?,
	@SerializedName("name") val name: String?,
	@SerializedName("agentCode") val agentCode: String?)
