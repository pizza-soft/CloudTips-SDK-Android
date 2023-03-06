package ru.cloudtips.sdk.network

import retrofit2.http.*
import ru.cloudtips.sdk.network.models.*
import ru.cloudtips.sdk.network.postbodies.PostAuthVerify
import ru.cloudtips.sdk.network.postbodies.PostPartnerAuth
import ru.cloudtips.sdk.network.postbodies.PostPayment3ds
import ru.cloudtips.sdk.network.postbodies.PostPublicId

interface ApiRequests {

    @GET("/api/layouts/{layoutId}")
    suspend fun getLink(@Path("layoutId") layoutId: String?): BasicResponse<LinkData>

    @GET("/api/paymentpages/{layoutId}")
    suspend fun getPaymentPage(@Path("layoutId") layoutId: String?): BasicResponse<PaymentPageData>

    @GET("/api/layouts/list/{phoneNumber}")
    suspend fun getLayoutList(@Path("phoneNumber") phoneNumber: String?): BasicResponse<List<Layout>>

    @GET("/api/payment/fee")
    suspend fun getPaymentFee(@Query("layoutId") layoutId: String?, @Query("amount") amount: Double): BasicResponse<PaymentFee>

    @POST("/api/payment/publicid")
    suspend fun getPublicId(@Body body: PostPublicId): BasicResponse<PublicIdData>

    @POST("/api/payment/auth")
    suspend fun postPaymentAuth(@Body body: PostPartnerAuth): BasicResponse<PaymentAuthData>

    @POST("/api/payment/post3ds")
    suspend fun postPayment3ds(@Body body: PostPayment3ds): BasicResponse<PaymentAuthData>

    @POST("captcha/verify")
    suspend fun postAuthVerify(@Body body: PostAuthVerify): BasicResponse<AuthVerifyData>
}