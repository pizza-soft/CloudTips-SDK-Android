package ru.cloudtips.sdk.ui.activities.tips.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import ru.cloudtips.sdk.BuildConfig
import ru.cloudtips.sdk.R
import ru.cloudtips.sdk.card.Card
import ru.cloudtips.sdk.card.ThreeDsDialogFragment
import ru.cloudtips.sdk.databinding.FragmentPaymentCardBinding
import ru.cloudtips.sdk.helpers.CommonHelper
import ru.cloudtips.sdk.network.BasicResponse
import ru.cloudtips.sdk.network.hasNeedCaptcha
import ru.cloudtips.sdk.network.models.PaymentAuthData
import ru.cloudtips.sdk.network.models.PaymentAuthStatusCode
import ru.cloudtips.sdk.ui.activities.tips.listeners.IPaymentCardListener
import ru.cloudtips.sdk.ui.activities.tips.viewmodels.TipsViewModel
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

class PaymentCardFragment : Fragment(R.layout.fragment_payment_card), ThreeDsDialogFragment.ThreeDSDialogListener {
    private val viewBinding: FragmentPaymentCardBinding by viewBinding()
    private val viewModel: TipsViewModel by activityViewModels()

    private var listener: IPaymentCardListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? IPaymentCardListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {

            headerBackButton.setOnClickListener {
                activity?.onBackPressed()
            }

            headerCloseButton.setOnClickListener {
                listener?.onCloseClick()
            }

            mainButton.setOnClickListener {
                if (isValid()) {
                    val cardNumber = cardNumberInput.text?.toString()
                    val cardDate = cardDateInput.text?.toString()
                    val cardCvc = cardCvcInput.text?.toString()
                    viewModel.putPaymentCardData(cardNumber, cardDate, cardCvc)
                    launchPayment()
                }
            }

            val cardNumberFormatWatcher = MaskFormatWatcher(MaskImpl.createTerminated(PredefinedSlots.CARD_NUMBER_STANDARD))
            cardNumberFormatWatcher.installOn(cardNumberInput)
            cardNumberInput.doAfterTextChanged { cardNumberInputLayout.helperText = null}

            val cardDateFormatWatcher = MaskFormatWatcher(MaskImpl.createTerminated(UnderscoreDigitSlotsParser().parseSlots("__/__")))
            cardDateFormatWatcher.installOn(cardDateInput)
            cardDateInput.doAfterTextChanged { cardDateInputLayout.helperText = null}

            val cardCvcFormatWatcher = MaskFormatWatcher(MaskImpl.createTerminated(UnderscoreDigitSlotsParser().parseSlots("___")))
            cardCvcFormatWatcher.installOn(cardCvcInput)
            cardCvcInput.doAfterTextChanged { cardCvcInputLayout.helperText = null}
        }

        viewModel.getPaymentInfoData().observe(viewLifecycleOwner) {
            hideSpinner()
            viewBinding.sumTextView.text = getString(R.string.card_fragment_sum_text, CommonHelper.formatDouble(it?.getAmountWithFee()))
        }

    }

    private fun isValid(): Boolean = with(viewBinding) {
        val cardNumber = cardNumberInput.text?.toString() ?: ""
        if (!Card.isValidNumber(cardNumber)) {
            cardNumberInputLayout.helperText = getString(R.string.card_fragment_error_wrong_card_number)
            cardNumberInputLayout.requestFocus()
            return false
        }
        val cardDate = cardDateInput.text?.toString() ?: ""
        //date is always valid

        val cardCvc = cardCvcInput.text?.toString() ?: ""
        if (cardCvc.length != 3) {
            cardCvcInputLayout.helperText = getString(R.string.card_fragment_error_wrong_card_cvc)
            cardCvcInputLayout.requestFocus()
            return false
        }

        return true
    }

    private fun launchPayment(captcha: String? = null) {
        showSpinner()
        viewModel.launchPayment(captcha).observe(viewLifecycleOwner) {
            hideSpinner()
            onPaymentResponse(it)
        }
    }

    private fun onPaymentResponse(response: BasicResponse<PaymentAuthData>) {
        if (response.succeed) {
            val data = response.data
            when (data?.getStatusCode()) {
                PaymentAuthStatusCode.NEED3DS -> {
                    val acsUrl = data.acsUrl ?: ""
                    val paReq = data.paReq ?: ""
                    val md = data.md ?: ""
                    val fragment3ds = ThreeDsDialogFragment.newInstance(acsUrl, paReq, md)
                    fragment3ds.show(parentFragmentManager, "NEED3DS")
                    fragment3ds.setTargetFragment(this@PaymentCardFragment, REQUEST_CODE_3DS)
                }
                PaymentAuthStatusCode.SUCCESS -> {
                    onPaymentSuccess()
                }
                else -> {
                    onPaymentFailure()
                }
            }
        } else {
            if (response.getErrors().hasNeedCaptcha()) {
                SafetyNet.getClient(requireContext()).verifyWithRecaptcha(BuildConfig.RECAPTCHA_TOKEN)
                    .addOnSuccessListener(requireActivity()) { result ->
                        // Indicates communication with reCAPTCHA service was
                        // successful.
                        val token = result.tokenResult
                        if (!token.isNullOrEmpty()) {
                            launchPayment(token)
                        }
                    }
                    .addOnFailureListener(requireActivity()) { e ->
                        if (e is ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            Log.d("recaptcha", "Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d("recaptcha", "Error: ${e.message}")
                        }
                    }
            } else {
                onPaymentFailure()
            }
        }
    }

    override fun onAuthorizationCompleted(md: String, paRes: String) {
        showSpinner()
        viewModel.postPayment3ds(md, paRes).observe(this) {
            hideSpinner()
            onPaymentResponse(it)
        }
    }

    override fun onAuthorizationFailed(error: String?) {
        onPaymentFailure()
    }

    private fun onPaymentSuccess() {
        listener?.onPaymentCardSuccess()
    }

    private fun onPaymentFailure() {
        listener?.onPaymentCardFailure()
    }

    private fun showSpinner() = with(viewBinding) {
        spinnerLayout.root.visibility = View.VISIBLE
    }

    private fun hideSpinner() = with(viewBinding) {
        spinnerLayout.root.visibility = View.GONE
    }

    companion object {
        private const val REQUEST_CODE_3DS = 1001
        fun newInstance() = PaymentCardFragment().apply {
            arguments = Bundle()
        }
    }
}