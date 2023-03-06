package ru.cloudtips.sdk_demo

import androidx.multidex.MultiDexApplication
import ru.cloudtips.sdk.network.NetworkClient

internal val networkClient: NetworkClient by lazy {
    App.mNetworkClient!!
}

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        mNetworkClient = NetworkClient()
    }

    companion object {
        internal var mNetworkClient: NetworkClient? = null
    }
}