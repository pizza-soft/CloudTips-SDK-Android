package ru.cloudtips.sdk.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PayerFee(
    private val enabled: Boolean?,
    private val initialState: String?
) : Parcelable {
    fun getIsEnabled() = enabled ?: false
    fun getIsOnStart(): Boolean = initialState == "Enabled"
}
