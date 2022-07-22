package ru.cloudtips.sdk

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class TipsData private constructor(
    val phone: String,
    val name: String,
    val partner: String,
    val layoutId: String,
) : Parcelable {

    constructor(phone: String, name: String, partner: String) : this(phone, name, partner, "")
    constructor(phone: String, name: String) : this(phone, name, "", "")
    constructor(layoutId: String) : this("", "", "", layoutId)
}