package ru.cloudtips.sdk.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingData(
    private val components: List<RatingComponentData>?,
    private val componentsText: RatingText?,
    private val enabled: Boolean?,
    private val starsText: RatingText?
) : Parcelable {
    fun getEnabled() = enabled ?: false
    fun getStarsText() = starsText?.getText()
    fun getComponentsText() = componentsText?.getText()
    fun getComponents() = components
}

@Parcelize
data class RatingComponentData(
    val id: String?,
    val imageUrl: String?,
    val title: String?
) : Parcelable

@Parcelize
data class RatingText(private val ru: String?) : Parcelable {
    fun getText() = ru
}