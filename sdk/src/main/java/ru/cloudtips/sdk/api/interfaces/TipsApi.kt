package ru.cloudtips.sdk.api.interfaces

import io.reactivex.Single
import retrofit2.http.*
import ru.cloudtips.sdk.api.Api
import ru.cloudtips.sdk.api.models.*

interface TipsApi {

    @GET("layouts/list/{phoneNumber}")
    fun getLayout(@Path("phoneNumber") phoneNumber: String?): Single<Api.ResponseWrapper<List<Layout>>>

    @POST("/api/receivers")
    fun offlineRegister(@Body body: OfflineRegisterRequestBody): Single<Api.ResponseWrapper<ReceiverData>>

    @GET("paymentpages/{layoutId}")
    fun getPaymentPage(@Path("layoutId") layoutId: String): Single<Api.ResponseWrapper<PaymentPage>>

    @POST("payment/publicId")
    fun getPublicId(@Body body: GetPublicIdRequestBody): Single<Api.ResponseWrapper<PublicId>>

    @GET("payment/fee")
    fun getPaymentFee(@Query("amount") amount: Double, @Query("layoutId") layoutId: String): Single<Api.ResponseWrapper<PaymentFee>>

    @POST("captcha/verify")
    fun verify(@Body body: VerifyRequestBody): Single<Api.ResponseWrapper<VerifyResponse>>

    @POST("payment/auth")
    fun auth(@Body body: PaymentRequestBody): Single<Api.ResponseWrapper<PaymentResponse>>

    @POST("payment/post3ds")
    fun postThreeDs(@Body body: PostThreeDsRequestBody): Single<Api.ResponseWrapper<PaymentResponse>>
}

