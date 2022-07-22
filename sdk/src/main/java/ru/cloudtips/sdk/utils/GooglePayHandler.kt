package ru.cloudtips.sdk.utils

import com.google.android.gms.wallet.*
import io.ashdavies.rx.rxtasks.toSingle
import io.reactivex.Single
import ru.cloudtips.sdk.ui.TipsActivity

internal class GooglePayHandler {

	companion object {
		fun present(publicId: String, amount: String, activity: TipsActivity, requestCode: Int) {
			val transactionInfo = TransactionInfo.newBuilder()
				.setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
				.setTotalPrice(amount)
				.setCurrencyCode("RUB")
				.build()
			val request = createPaymentDataRequest(transactionInfo, publicId)
			val client = createPaymentsClient(activity)
			AutoResolveHelper.resolveTask(client.loadPaymentData(request), activity, requestCode)
		}

		private fun createPaymentDataRequest(transactionInfo: TransactionInfo, publicId: String): PaymentDataRequest {
			val paramsBuilder = PaymentMethodTokenizationParameters.newBuilder()
				.setPaymentMethodTokenizationType(
					WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
				.addParameter("gateway", "cloudpayments")
				.addParameter("gatewayMerchantId", publicId)

			return createPaymentDataRequest(transactionInfo, paramsBuilder.build())
		}

		private fun createPaymentDataRequest(transactionInfo: TransactionInfo, params: PaymentMethodTokenizationParameters): PaymentDataRequest {
			return PaymentDataRequest.newBuilder()
				.setPhoneNumberRequired(false)
				.setEmailRequired(false)
				.setShippingAddressRequired(false)
				.setTransactionInfo(transactionInfo)
				.addAllowedPaymentMethods(GOOGLE_PAY_SUPPORTED_METHODS)
				.setCardRequirements(
					CardRequirements.newBuilder()
						.addAllowedCardNetworks(GOOGLE_PAY_SUPPORTED_NETWORKS)
						.setAllowPrepaidCards(true)
						.setBillingAddressRequired(false)
						.setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_MIN)
						.build())
				.setPaymentMethodTokenizationParameters(params)
				.setUiRequired(true)
				.build()
		}

		private fun createPaymentsClient(activity: TipsActivity): PaymentsClient {
			val walletOptions = Wallet.WalletOptions.Builder()
				.setEnvironment(GOOGLE_PAY_ENVIRONMENT)
				.build()
			return Wallet.getPaymentsClient(activity, walletOptions)
		}

		fun isReadyToMakeGooglePay(activity: TipsActivity): Single<Boolean> {
			val request = IsReadyToPayRequest.newBuilder()
			for (allowedMethod in GOOGLE_PAY_SUPPORTED_METHODS) {
				request.addAllowedPaymentMethod(allowedMethod)
			}
			return createPaymentsClient(activity).isReadyToPay(request.build()).toSingle()
		}
	}
}