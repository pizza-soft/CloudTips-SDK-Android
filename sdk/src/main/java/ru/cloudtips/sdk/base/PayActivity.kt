package ru.cloudtips.sdk.base

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment
import ru.cloudtips.sdk.CloudTipsSDK
import ru.cloudtips.sdk.api.Api
import ru.cloudtips.sdk.api.ApiEndPoint
import ru.cloudtips.sdk.api.models.PaymentResponse
import ru.cloudtips.sdk.api.models.PaymentResponseStatus
import ru.cloudtips.sdk.api.models.VerifyResponse
import ru.cloudtips.sdk.ui.CompletionActivity

abstract class PayActivity : BaseActivity(), ThreeDsDialogFragment.ThreeDSDialogListener {

    companion object {
        const val REQUEST_CODE_COMPLETION_ACTIVITY = 100
    }

    protected fun authWithoutVerify() {
        auth(layoutId(), cryptogram(), amount(), comment(), feeFromPayer(), null)
    }

    protected fun verifyV3(amount: String?, layoutId: String?) {
        showLoading()
        val recaptchaVersion = "3"
        compositeDisposable.add(
            Api.verify(recaptchaVersion, "for_trusted_client", amount, layoutId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    checkVerifyV3Response(response)
                }, this::handleError)
        )
    }

    private fun checkVerifyV3Response(response: Api.ResponseWrapper<VerifyResponse>) {
        val data = response.data
        if (data != null && data.isPassed()) {
            auth(layoutId(), cryptogram(), amount(), comment(), feeFromPayer(), "")
        } else {
            if (response.getErrors().contains(Api.ResponseError.INVALID_CAPTCHA)) {
                SafetyNet.getClient(this).verifyWithRecaptcha(ApiEndPoint.getRecapchaV2Token())
                    .addOnSuccessListener(this) {
                        val tokenResult = it.tokenResult
                        if (!tokenResult.isNullOrEmpty()) {
                            handleVerify(tokenResult)
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        if (e is ApiException) {
                            Log.e(TAG, ("Error message: " + CommonStatusCodes.getStatusCodeString(e.statusCode)))
                        } else {
                            Log.e(TAG, "Unknown type of error: " + e.message)
                        }
                    }
            }
        }
    }

    private fun handleVerify(responseToken: String) {

        if (responseToken.isNotEmpty()) {
            verifyV2(responseToken, amount(), layoutId())
        }
    }

    private fun verifyV2(token: String, amount: String?, layoutId: String?) {
        showLoading()
        val recaptchaVersion = "4"
        compositeDisposable.add(
            Api.verify(recaptchaVersion, token, amount, layoutId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    checkVerifyV2Response(response)
                }, this::handleError)
        )
    }

    private fun checkVerifyV2Response(response: Api.ResponseWrapper<VerifyResponse>) {

        response.data?.getToken()?.let { auth(layoutId(), cryptogram(), amount(), comment(), feeFromPayer(), it) }
    }

    private fun auth(layoutId: String?, cryptogram: String?, amount: String?, comment: String?, feeFromPayer: Boolean, token: String?) {
        showLoading()
        compositeDisposable.add(
            Api.auth(layoutId, cryptogram, amount, comment, feeFromPayer, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ authResponse -> checkPaymentResponse(authResponse) }, this::handleError)
        )
    }

    private fun postThreeDs(md: String, paRes: String) {
        showLoading()
        compositeDisposable.add(
            Api.postThreeDs(md, paRes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ authResponse -> checkPaymentResponse(authResponse) }, this::handleError)
        )
    }

    private fun checkPaymentResponse(response: Api.ResponseWrapper<PaymentResponse>) {
        val data = response.data
        if (data == null) {
            hideLoading()
            return
        }
        if (data.status != null) {
            hideLoading()
            val intent = CompletionActivity.getStartIntent(this, photoUrl(), name(), false, data.title.toString(), data.detail.toString())
            startActivityForResult(intent, REQUEST_CODE_COMPLETION_ACTIVITY)
            return
        }

        if (data.getStatusCode() == PaymentResponseStatus.NEED_3DS) {
            val acsUrl = data.acsUrl
            val paReq = data.paReq
            val md = data.md
            if (acsUrl != null && paReq != null && md != null) {
                ThreeDsDialogFragment
                    .newInstance(acsUrl, paReq, md)
                    .show(supportFragmentManager, "3DS")
            } else {
                hideLoading()
            }
        } else if (data.getStatusCode() == PaymentResponseStatus.SUCCESS) {
            hideLoading()
            val intent = CompletionActivity.getStartIntent(this, photoUrl(), name(), true, "", "")
            startActivityForResult(intent, REQUEST_CODE_COMPLETION_ACTIVITY)
        } else {
            hideLoading()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when (requestCode) {
        REQUEST_CODE_COMPLETION_ACTIVITY -> {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    setResult(RESULT_OK, Intent().apply {
                        val transactionStatus =
                            data?.getSerializableExtra(CloudTipsSDK.IntentKeys.TransactionStatus.name) as? CloudTipsSDK.TransactionStatus
                        putExtra(CloudTipsSDK.IntentKeys.TransactionStatus.name, transactionStatus)
                    })
                    finish()
                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        }
        else -> super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onAuthorizationCompleted(md: String, paRes: String) {
        postThreeDs(md, paRes)
    }

    override fun onAuthorizationFailed(error: String?) {
    }

    abstract fun cryptogram(): String?
    abstract fun layoutId(): String?
    abstract fun amount(): String?
    abstract fun comment(): String?
    abstract fun photoUrl(): String?
    abstract fun name(): String?
    abstract fun feeFromPayer(): Boolean


}