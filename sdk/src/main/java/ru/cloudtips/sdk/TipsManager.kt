package ru.cloudtips.sdk

import android.content.Context
import ru.cloudtips.sdk.network.NetworkClient
import ru.cloudtips.sdk.ui.activities.tips.PaymentTipsActivity

internal val networkClient: NetworkClient by lazy {
    TipsManager.mNetworkClient!!
}

class TipsManager(private val context: Context) {
    init {
        mNetworkClient = NetworkClient()
    }

    fun launch(layoutId: String?) {

        context.startActivity(PaymentTipsActivity.newIntent(context, layoutId))
    }


    companion object {
        internal var mNetworkClient: NetworkClient? = null

        fun getInstance(context: Context): TipsManager {
            return TipsManager(context)
        }
    }
}