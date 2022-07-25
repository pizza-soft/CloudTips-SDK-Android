package ru.cloudtips.sdk.base

import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.TextView.BufferType
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.disposables.CompositeDisposable
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransactionError
import ru.cloudtips.sdk.BuildConfig
import ru.cloudtips.sdk.CloudTipsSDK
import ru.cloudtips.sdk.R
import java.net.UnknownHostException
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    protected val TAG = "TAG_" + javaClass.simpleName.toUpperCase(Locale.getDefault())
    protected var compositeDisposable = CompositeDisposable()

    abstract fun showLoading()

    abstract fun hideLoading()

    fun errorMode(isErrorMode: Boolean, editText: TextInputEditText){
        if (isErrorMode) {
            editText.setBackgroundResource(R.drawable.selector_bg_edit_text_error)
        } else {
            editText.setBackgroundResource(R.drawable.selector_bg_edit_text)
        }
    }

    fun showToast(@StringRes resId: Int) {
        showToast(getString(resId))
    }

    fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun log(message: String?) {
        Log.d(TAG, message.orEmpty())
    }

    open fun handleError(throwable: Throwable, vararg ignoreClasses: Class<*>?) {
        hideLoading()
        Log.e("handleError", throwable.message, throwable)
        if (ignoreClasses.isNotEmpty()) {
            val classList = listOf(*ignoreClasses)
            if (classList.contains(throwable.javaClass)) {
                return
            }
        }
        when (throwable) {
            is CloudpaymentsTransactionError -> {
                val message: String = throwable.message
                showToast(message)
            }
            is UnknownHostException -> showToast(R.string.app_no_internet_connection)
            else -> if (BuildConfig.DEBUG) showToast("debugError: ${throwable.message}")
        }
        setResult(RESULT_OK, Intent().apply {
            val transactionStatus = CloudTipsSDK.TransactionStatus.Cancelled
            putExtra(CloudTipsSDK.IntentKeys.TransactionStatus.name, transactionStatus)
        })
        finish()
    }

    fun initRecaptchaTextView(view: TextView) {
        val recaptchaText = SpannableStringBuilder(
            getString(R.string.app_recaptcha_this_site_is_protected_by_reCAPTCHA_and_the)
        )
        recaptchaText.append(getString(R.string.app_recaptcha_google_privacy_policy))
        recaptchaText.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW,  Uri.parse("https://policies.google.com/privacy"))
                startActivity(intent)
            }
        }, recaptchaText.length - getString(R.string.app_recaptcha_google_privacy_policy).length, recaptchaText.length, 0)
        recaptchaText.append(getString(R.string.app_recaptcha_and))
        recaptchaText.append(getString(R.string.app_recaptcha_terms_of_service))
        recaptchaText.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW,  Uri.parse("https://policies.google.com/terms"))
                startActivity(intent)
            }
        }, recaptchaText.length - getString(R.string.app_recaptcha_terms_of_service).length, recaptchaText.length, 0)
        recaptchaText.append(getString(R.string.app_recaptcha_apply))
        view.movementMethod = LinkMovementMethod.getInstance()
        view.setText(recaptchaText, BufferType.SPANNABLE)
    }
}