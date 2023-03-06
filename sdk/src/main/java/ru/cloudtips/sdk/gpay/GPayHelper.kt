package ru.cloudtips.sdk.gpay

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wallet.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.cloudtips.sdk.BuildConfig

object GPayHelper {

    private val PAYMENTS_ENVIRONMENT = if (BuildConfig.DEBUG) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION

    private fun getPaymentGatewayTokenizationParameters(merchantId: String?) = mapOf(
        "gateway" to "cloudpayments",
        "gatewayMerchantId" to merchantId
    )

    private val SUPPORTED_METHODS = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    private val SUPPORTED_NETWORKS = listOf("MASTERCARD", "VISA")

    private val merchantInfo: JSONObject =
        JSONObject().put("merchantName", "CloudTips")

    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    private val allowedCardAuthMethods = JSONArray(SUPPORTED_METHODS)

    private val allowedCardNetworks = JSONArray(SUPPORTED_NETWORKS)

    private fun baseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {
            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", false)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "MIN")
                })
            }

            put("type", "CARD")
            put("parameters", parameters)
        }
    }


    fun isReadyToPayRequest(): JSONObject? {
        return try {
            baseRequest.apply {
                put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
            }
        } catch (e: JSONException) {
            null
        }
    }

    fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(PAYMENTS_ENVIRONMENT)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    fun getPaymentDataRequest(amount: Double, merchantId: String?): JSONObject {
        return baseRequest.apply {
            put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod(merchantId)))
            put("transactionInfo", getTransactionInfo(amount))
            put("merchantInfo", merchantInfo)
            put("shippingAddressRequired", false)
        }
    }

    @Throws(JSONException::class)
    private fun getTransactionInfo(price: Double): JSONObject {
        return JSONObject().apply {
            put("totalPrice", price.toString())
            put("totalPriceStatus", "FINAL")
            put("currencyCode", "RUB")
        }
    }

    private fun cardPaymentMethod(merchantId: String?): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod()
        cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification(merchantId))

        return cardPaymentMethod
    }

    private fun gatewayTokenizationSpecification(merchantId: String?): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject(getPaymentGatewayTokenizationParameters(merchantId)))
        }
    }

}