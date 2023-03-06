package ru.cloudtips.sdk.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.cloudtips.sdk.BuildConfig
import ru.cloudtips.sdk.models.PaymentInfoData
import ru.cloudtips.sdk.network.models.*
import ru.cloudtips.sdk.network.postbodies.PostAuthVerify
import ru.cloudtips.sdk.network.postbodies.PostPartnerAuth
import ru.cloudtips.sdk.network.postbodies.PostPayment3ds
import ru.cloudtips.sdk.network.postbodies.PostPublicId
import java.io.IOException
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

class NetworkClient {
    private val apiUrl = BuildConfig.URL_API
    private val apiRequests: ApiRequests

    private val dispatcher = Dispatchers.IO

    init {
        val cookieManager = java.net.CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 1

        val defaultHttpClientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            defaultHttpClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }

        defaultHttpClientBuilder
            .dispatcher(dispatcher)
            .readTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .cookieJar(JavaNetCookieJar(cookieManager))

        val retrofitApi = Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(defaultHttpClientBuilder.build())
            .build()

        apiRequests = retrofitApi.create(ApiRequests::class.java)
    }

    fun getApiUrl(): String {
        return apiUrl
    }

    //API

    suspend fun getLayoutList(phone: String?): BasicResponse<List<Layout>> {
        return safeApiCall(dispatcher) { apiRequests.getLayoutList(phone) }
    }

    suspend fun getLink(layoutId: String?): BasicResponse<LinkData> {
        return safeApiCall(dispatcher) { apiRequests.getLink(layoutId) }
    }

    suspend fun getPaymentPage(layoutId: String?): BasicResponse<PaymentPageData> {
        return safeApiCall(dispatcher) { apiRequests.getPaymentPage(layoutId) }
    }

    suspend fun getPaymentFee(layoutId: String?, amount: Double): BasicResponse<PaymentFee> {
        return safeApiCall(dispatcher) { apiRequests.getPaymentFee(layoutId, amount) }
    }

    suspend fun getPublicId(layoutId: String?): BasicResponse<PublicIdData> {
        return safeApiCall(dispatcher) { apiRequests.getPublicId(PostPublicId(layoutId)) }
    }

    suspend fun postPaymentAuth(
        layoutId: String,
        info: PaymentInfoData,
        cryptogram: String,
        captchaVerificationToken: String? = null
    ): BasicResponse<PaymentAuthData> {
        val body = PostPartnerAuth(
            amount = info.getAmount(),
            feeFromPayer = info.feeFromPayer,
            payerName = info.sender.name,
            payerEmail = info.sender.email,
            payerPhone = info.sender.phone,
            payerCity = info.sender.city,
            payerComment = info.sender.comment,
            layoutId = layoutId,
            cryptogram = cryptogram,
            rating = if (info.rating?.score != null && info.rating.score > 0) PostPartnerAuth.Rating(
                info.rating.score,
                info.rating.components
            ) else null,
            captchaVerificationToken = captchaVerificationToken
        )
        return safeApiCall(dispatcher) { apiRequests.postPaymentAuth(body) }
    }

    suspend fun postPayment3ds(md: String, paRes: String): BasicResponse<PaymentAuthData> {
        return safeApiCall(dispatcher) { apiRequests.postPayment3ds(PostPayment3ds(md, paRes)) }
    }

    suspend fun postAuthVerify(amount: Double, layoutId: String, version: Int, token: String?): BasicResponse<AuthVerifyData> {
        return safeApiCall(dispatcher) { apiRequests.postAuthVerify(PostAuthVerify(amount, layoutId, version, token)) }
    }

    private suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> BasicResponse<T>): BasicResponse<T> {
        return withContext(dispatcher) {
            try {
                apiCall.invoke()
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> BasicResponse.getUnknownError()
                    is HttpException -> {
                        val code = throwable.code()
                        convertErrorBody<T>(throwable).apply {
                            this.code = code
                        }
                    }
                    else -> BasicResponse.getUnknownError()
                }
            }
        }
    }

    private fun <T> convertErrorBody(throwable: HttpException): BasicResponse<T> {
        return try {
            throwable.response()?.errorBody()?.string()?.let {
                val moshiAdapter = Moshi.Builder().build().adapter(BasicError::class.java)
                moshiAdapter.fromJson(it)?.toBasicResponse()
            } ?: BasicResponse.getUnknownError()
        } catch (exception: Exception) {
            BasicResponse.getUnknownError()
        }
    }

}

@JsonClass(generateAdapter = true)
data class ValidationErrors(
    val PhoneNumber: List<String>?
) {
    fun getErrors(): List<String> {
        //concat all texts
        return PhoneNumber ?: emptyList()
    }
}

@JsonClass(generateAdapter = true)
class BasicError {
    var succeed: Boolean = false
    var errors: List<String>? = emptyList()
    var validationErrors: ValidationErrors? = null
    var code: Int = 0
    private fun getErrorsList(): List<String> {
        val verr = validationErrors?.getErrors() ?: emptyList()
        val err = errors ?: emptyList()
        return verr.plus(err)
    }

    fun <T> toBasicResponse(): BasicResponse<T> {
        return BasicResponse<T>().apply {
            succeed = this@BasicError.succeed
            errors = this@BasicError.getErrorsList()
            code = this@BasicError.code
        }
    }
}

fun List<String>?.hasNeedCaptcha() = (this != null && this.find { it.equals("Неверно введена капча", true) } != null)

class BasicResponse<T> {
    var succeed: Boolean = true
    internal var errors: List<String>? = emptyList()
    var code: Int = 0
    var data: T? = null

    fun getErrors(): List<String> {
        return errors ?: emptyList()
    }

    companion object {
        fun <T> getUnknownError() = BasicResponse<T>().apply {
            succeed = false
        }
    }
}
