package ru.cloudtips.sdk.api.models

data class ReceiverData(
    val email: String?,
    val layoutIds: List<String>?,
    val name: String?,
    val phoneNumber: String?,
    val phoneVerified: Boolean?,
    val photoId: String?,
    val photoUrl: String?,
    val userId: String?
)