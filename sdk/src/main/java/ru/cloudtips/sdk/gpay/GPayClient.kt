package ru.cloudtips.sdk.gpay

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient

class GPayClient(context: Context) {

    //GPay section
    private val paymentsClient: PaymentsClient = GPayHelper.createPaymentsClient(context)

    private val _canUseGooglePay: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            fetchCanUseGooglePay()
        }
    }
    val canUseGooglePay: LiveData<Boolean> = _canUseGooglePay

    private fun fetchCanUseGooglePay() {
        val isReadyToPayJson = GPayHelper.isReadyToPayRequest()
        if (isReadyToPayJson == null) _canUseGooglePay.postValue(false)

        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                val result = completedTask.getResult(ApiException::class.java)
                _canUseGooglePay.postValue(result)
            } catch (exception: ApiException) {
                Log.w("isReadyToPay failed", exception)
                _canUseGooglePay.postValue(false)
            }
        }
    }

    fun getLoadPaymentDataTask(amount: Double, merchantId: String?): Task<PaymentData> {
        val paymentDataRequestJson = GPayHelper.getPaymentDataRequest(amount, merchantId)
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        return paymentsClient.loadPaymentData(request)
    }

}