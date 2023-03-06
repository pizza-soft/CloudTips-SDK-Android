package ru.cloudtips.sdk.helpers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        private var count = 0
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            count++
            if (t != null || count > 1) {
                removeObserver(this)
            }
        }
    })
}