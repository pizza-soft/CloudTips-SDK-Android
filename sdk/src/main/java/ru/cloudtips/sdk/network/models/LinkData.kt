package ru.cloudtips.sdk.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LinkData(
    private val layoutId: String?,
    private val shortId: String?,
    val default: Boolean?,
    val disabled: Boolean?,
    val title: String?,
    val description: String?,
    val text: String?,
    val paymentLink: String?,
    val backgroundId: String?,
    val backgroundUrl: String?,
    val qrLink: String?,
    val placeId: String?
) : Parcelable {

    fun getLayoutId() = layoutId ?: shortId

}