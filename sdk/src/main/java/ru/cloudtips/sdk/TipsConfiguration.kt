package ru.cloudtips.sdk

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class TipsConfiguration(val tipsData: TipsData, val testMode: Boolean): Parcelable {

    constructor(tipsData: TipsData) : this(tipsData, false)
}