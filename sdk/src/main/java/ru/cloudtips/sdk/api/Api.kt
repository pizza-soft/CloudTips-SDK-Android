package ru.cloudtips.sdk.api

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.cloudtips.sdk.api.models.*


class Api {

    enum class ResponseError {
        OTHER,
        INVALID_CAPTCHA;

        companion object {
            fun getByString(value: String?): ResponseError {
                if (value == "Неверно введена капча" || value == "Invalid captcha") return INVALID_CAPTCHA
                return OTHER
            }
        }
    }

    class ResponseWrapper<T>(
        val data: T?,
        private val succeed: Boolean?,
        private val errors: List<String>?
    ) {
        fun getErrors(): List<ResponseError> {
            return errors?.map { ResponseError.getByString(it) } ?: emptyList()
        }
    }

    companion object {

        fun getLayout(phoneNumber: String?): Single<ResponseWrapper<List<Layout>>> {
            return ApiFactory.getTipsApi()
                .getLayout(phoneNumber)
                .subscribeOn(Schedulers.io())
        }

        fun offlineRegister(
            phoneNumber: String?,
            name: String?,
            partner: String?
        ): Single<ResponseWrapper<ReceiverData>> {

            var body = OfflineRegisterRequestBody(
                phoneNumber = phoneNumber,
                name = name,
                agentCode = partner
            )

            return ApiFactory.getTipsApi()
                .offlineRegister(body)
                .subscribeOn(Schedulers.io())
        }

        fun getPaymentPage(layoutId: String): Single<ResponseWrapper<PaymentPage>> {
            return ApiFactory.getTipsApi()
                .getPaymentPage(layoutId)
                .subscribeOn(Schedulers.io())
        }

        fun getPublicId(layoutId: String?): Single<ResponseWrapper<PublicId>> {

            var body = GetPublicIdRequestBody(layoutId = layoutId)

            return ApiFactory.getTipsApi()
                .getPublicId(body)
                .subscribeOn(Schedulers.io())
        }

        fun getPaymentFee(layoutId: String, amount: Double): Single<ResponseWrapper<PaymentFee>> {
            return ApiFactory.getTipsApi()
                .getPaymentFee(amount, layoutId)
                .subscribeOn(Schedulers.io())
        }

        fun verify(
            version: String,
            token: String,
            amount: String?,
            layoutId: String?
        ): Single<ResponseWrapper<VerifyResponse>> {

            val body = VerifyRequestBody(
                version = version,
                token = token,
                amount = amount,
                layoutId = layoutId
            )

            return ApiFactory.getTipsApi()
                .verify(body)
                .subscribeOn(Schedulers.io())
        }

        fun auth(
            layoutId: String?,
            cryptogram: String?,
            amount: String?,
            comment: String?,
            feeFromPayer: Boolean,
            token: String?
        ): Single<ResponseWrapper<PaymentResponse>> {

            val body = PaymentRequestBody(
                layoutId = layoutId,
                cryptogram = cryptogram,
                amount = amount,
                comment = comment,
                captchaVerificationToken = token,
                feeFromPayer = feeFromPayer
            )

            return ApiFactory.getTipsApi()
                .auth(body)
                .subscribeOn(Schedulers.io())
        }

        fun postThreeDs(md: String, paRes: String): Single<ResponseWrapper<PaymentResponse>> {

            val body = PostThreeDsRequestBody(
                md = md,
                paRes = paRes
            )

            return ApiFactory.getTipsApi()
                .postThreeDs(body)
                .subscribeOn(Schedulers.io())
        }
    }
}