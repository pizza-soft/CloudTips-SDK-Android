package ru.cloudtips.sdk.ui.activities.tips.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.method.LinkMovementMethod
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import ru.cloudtips.sdk.R
import ru.cloudtips.sdk.network.models.PaymentPageData
import ru.cloudtips.sdk.ui.activities.tips.viewmodels.TipsViewModel
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.android.material.textfield.TextInputLayout
import com.yandex.pay.core.*
import com.yandex.pay.core.data.*
import com.yandex.pay.core.ui.YandexPayButton
import ru.cloudtips.sdk.databinding.FragmentPaymentInfoBinding
import ru.cloudtips.sdk.gpay.GPayClient
import ru.cloudtips.sdk.models.RatingComponent
import ru.cloudtips.sdk.ui.activities.tips.adapters.BubblesAdapter
import ru.cloudtips.sdk.ui.activities.tips.adapters.ComponentsAdapter
import ru.cloudtips.sdk.ui.activities.tips.listeners.IPaymentInfoListener
import ru.cloudtips.sdk.ui.decorators.LinearHorizontalDecorator
import ru.cloudtips.sdk.ui.elements.ClickableUrlSpan
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.roundToInt
import ru.cloudtips.sdk.BuildConfig
import ru.cloudtips.sdk.card.ThreeDsDialogFragment
import ru.cloudtips.sdk.helpers.CommonHelper
import ru.cloudtips.sdk.models.PaymentInfoRatingData
import ru.cloudtips.sdk.network.BasicResponse
import ru.cloudtips.sdk.network.models.PaymentAuthData
import ru.cloudtips.sdk.network.models.PaymentAuthStatusCode

class PaymentInfoFragment : Fragment(R.layout.fragment_payment_info), ClickableUrlSpan.ISpanUrlClick, ThreeDsDialogFragment.ThreeDSDialogListener {

    private var gPayClient: GPayClient? = null

    private var listener: IPaymentInfoListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        gPayClient = GPayClient(context)
        listener = context as? IPaymentInfoListener
        initYPayment()
    }

    override fun onDetach() {
        super.onDetach()
        gPayClient = null
        listener = null
    }

    override fun onPause() {
        super.onPause()
        requestFeeRunnable?.let {
            requestFeeHandler.removeCallbacks(it)
            requestFeeRunnable = null
        }
    }

    private val viewBinding: FragmentPaymentInfoBinding by viewBinding()
    private val viewModel: TipsViewModel by activityViewModels()

    private var paymentPageData: PaymentPageData? = null
    private var feeAmount = 0.0

    private var ratingComponents = listOf<RatingComponent>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        viewModel.getPaymentPageData().observe(viewLifecycleOwner) { data ->
            if (data != null) {
                viewBinding.mainLayout.visibility = View.VISIBLE
                hideSpinner()
                paymentPageData = data
                fillBackground()
                fillUserData()
                fillPaymentData()
                fillInfoData()
                updateFeeValue()
                if (paymentPageData?.getGooglePayEnabled() == true) {
                    gPayClient?.canUseGooglePay?.observe(viewLifecycleOwner) {
                        updateGooglePayButton(it)
                    }
                } else {
                    updateGooglePayButton(false)
                }
                updateYPayButton(YandexPayLib.isSupported)
            } else {
                //TODO: show error
            }
        }

        fillLinksInfo()
    }

    private fun initViews() = with(viewBinding) {
        feeSwitch.setOnCheckedChangeListener { _, _ ->
            updateFeeValue()
        }
        amountInputLayout.apply {
            doAfterTextChanged {
                requestFeeValue(false)
            }
            setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) requestFeeValue(true) }
        }

        nameInput.doAfterTextChanged { savePaymentInfoSender() }
        emailInput.doAfterTextChanged { savePaymentInfoSender() }
        phoneInput.doAfterTextChanged { savePaymentInfoSender() }
        cityInput.doAfterTextChanged { savePaymentInfoSender() }
        commentInput.doAfterTextChanged { savePaymentInfoSender() }

        headerCloseButton.setOnClickListener {
            listener?.onCloseClick()
        }

        cardButton.setOnClickListener {
            if (validatePayClick()) {
                launchPaymentClick { listener?.onPayInfoClick() }
            }
        }
        gpayButton.setOnClickListener {
            if (validatePayClick()) {
                launchPaymentClick { requestGPayClick() }
            }
        }
        ypayButton.setOnClickListener(YandexPayButton.OnClickListener {
            if (validatePayClick()) {
                launchPaymentClick { requestYPayClick() }
            }
        })
    }

    private fun launchPaymentClick(callback: () -> Unit) {
        val amount = getAmount()
        viewModel.getFeeValue(amount).observe(viewLifecycleOwner) {
            callback.invoke()
        }
    }

    private fun validatePayClick(): Boolean {
        return if (isValid()) {
            savePaymentInfoAmount()
            savePaymentInfoSender()
            savePaymentInfoRating()
            true
        } else false
    }

    private fun savePaymentInfoAmount() = with(viewBinding) {
        viewModel.putPaymentInfoAmountData(getAmount(), feeAmount, feeSwitch.isChecked)
    }

    private fun savePaymentInfoSender() = with(viewBinding) {
        val name = nameInput.text?.toString()
        val email = emailInput.text?.toString()
        val phone = phoneInput.text?.toString()
        val city = cityInput.text?.toString()
        val comment = commentInput.text?.toString()
        viewModel.putPaymentInfoSenderData(name, email, phone, city, comment)
    }

    private fun savePaymentInfoRating() = with(viewBinding) {
        val rating = rating
        val ratingComponents = ratingComponents.filter { it.selected }
        viewModel.putPaymentInfoRatingData(rating, ratingComponents)
    }

    private fun updateGooglePayButton(available: Boolean) = with(viewBinding) {
        gpayButton.visibility = if (available) View.VISIBLE else View.GONE
    }

    private fun requestGPayClick() = with(viewBinding) {
        // Disables the button to prevent multiple clicks.
        gpayButton.isClickable = false
        viewModel.getMerchantId().observe(viewLifecycleOwner) {
            gPayClient?.getLoadPaymentDataTask(getAmount(), it?.publicId)?.addOnCompleteListener { completedTask ->
                if (completedTask.isSuccessful) {
                    launchGPayment(completedTask.result)
                } else {
                    when (val exception = completedTask.exception) {
                        is ResolvableApiException -> {
                            resolveGPaymentForResult.launch(
                                IntentSenderRequest.Builder(exception.resolution).build()
                            )
                        }
                        is ApiException -> {
                            handleError(exception.statusCode, exception.message)
                        }
                        else -> {
                            handleError(
                                CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                                        " exception when trying to deliver the task result to an activity!"
                            )
                        }
                    }
                }

                // Re-enables the Google Pay payment button.
                gpayButton.isClickable = true
            }
        }
    }

    private val resolveGPaymentForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
        when (result.resultCode) {
            AppCompatActivity.RESULT_OK ->
                result.data?.let { intent ->
                    launchGPayment(PaymentData.getFromIntent(intent))
                }

            AppCompatActivity.RESULT_CANCELED -> {
                // The user cancelled the payment attempt
            }
        }
    }

    private fun handleError(statusCode: Int, message: String?) {
        Log.e("Google Pay API error", "Error code: $statusCode, Message: $message")
    }

    private fun launchGPayment(paymentData: PaymentData?) {
        showSpinner()
        viewModel.launchGPayment(paymentData).observe(viewLifecycleOwner) { response ->
            hideSpinner()
            handlePaymentResponse(response)
        }
    }

    private fun handlePaymentResponse(response: BasicResponse<PaymentAuthData>) {
        val data = response.data
        when (data?.getStatusCode()) {
            PaymentAuthStatusCode.NEED3DS -> {
                val acsUrl = data.acsUrl ?: ""
                val paReq = data.paReq ?: ""
                val md = data.md ?: ""
                val fragment3ds = ThreeDsDialogFragment.newInstance(acsUrl, paReq, md)
                fragment3ds.show(parentFragmentManager, "NEED3DS")
                fragment3ds.setTargetFragment(this@PaymentInfoFragment, REQUEST_CODE_3DS)
            }
            PaymentAuthStatusCode.SUCCESS -> {
                onPaymentSuccess()
            }
            else -> {
                onPaymentFailure()
            }
        }
    }

    private fun onPaymentSuccess() {
        listener?.onPaymentSuccess()
    }

    private fun onPaymentFailure() {
        listener?.onPaymentFailure()
    }

    private fun updateYPayButton(available: Boolean) = with(viewBinding) {
        ypayButton.visibility = if (available) View.VISIBLE else View.GONE
    }

    private fun initYPayment() {
        if (YandexPayLib.isSupported) {
            val environment: YandexPayEnvironment
            val logging: Boolean
            if (BuildConfig.DEBUG) {
                environment = YandexPayEnvironment.SANDBOX
                logging = true
            } else {
                environment = YandexPayEnvironment.PROD
                logging = false
            }
            YandexPayLib.initialize(
                requireContext(), YandexPayLibConfig(
                    environment = environment,
                    logging = logging,
                    locale = YandexPayLocale.RU,
                    merchantDetails = Merchant(
                        id = MerchantId.from(getString(R.string.ypay_merchant_id)),
                        name = getString(R.string.ypay_merchant_name),
                        url = getString(R.string.ypay_merchant_url)
                    )
                )
            )
        }
    }

    private fun requestYPayClick() = with(viewBinding) {
        ypayButton.isClickable = false
        viewModel.getMerchantId().observe(viewLifecycleOwner) {
            val publicId = it?.publicId ?: ""
            val name = if (!nameTextView.text.isNullOrEmpty()) nameTextView.text.toString() else "CloudTips"
            yandexPayLauncher.launch(
                OrderDetails(
                    order = Order(
                        id = OrderID.from(name),
                        amount = Amount.from(getAmount().toString()),
                        label = name,
                        listOf()
                    ),
                    paymentMethods = listOf(
                        PaymentMethod(
                            allowedAuthMethods = listOf(AuthMethod.PanOnly),
                            type = PaymentMethodType.Card,
                            gateway = Gateway.from("cloudpayments"),
                            allowedCardNetworks = listOf(CardNetwork.Visa, CardNetwork.MasterCard, CardNetwork.MIR),
                            gatewayMerchantId = GatewayMerchantID.from(publicId),
                        )
                    )
                )
            )
        }
    }

    private val yandexPayLauncher = registerForActivityResult(OpenYandexPayContract()) { result: YandexPayResult ->
        viewBinding.ypayButton.isClickable = true
        when (result) {
            is YandexPayResult.Success -> handleYPaySuccess(result.paymentToken)
            is YandexPayResult.Failure -> when (result) {
                is YandexPayResult.Failure.Validation -> handleYPayFailure(result.details.name)
                is YandexPayResult.Failure.Internal -> handleYPayFailure(result.message)
            }
            YandexPayResult.Cancelled -> {}
        }
    }

    private fun handleYPaySuccess(paymentToken: PaymentToken) {
        val token = String(Base64.decode(paymentToken.toString(), Base64.DEFAULT))
        showSpinner()
        viewModel.launchYPayment(token).observe(viewLifecycleOwner) { response ->
            hideSpinner()
            handlePaymentResponse(response)
        }
    }

    private fun handleYPayFailure(message: String?) {
        Log.w("loadPaymentData failed", String.format("Ya payment error: %s", message))
        onPaymentFailure()
    }

    override fun onAuthorizationCompleted(md: String, paRes: String) {
        showSpinner()
        viewModel.postPayment3ds(md, paRes).observe(this) {
            hideSpinner()
            handlePaymentResponse(it)
        }

    }

    override fun onAuthorizationFailed(error: String?) {
        onPaymentFailure()
    }


    private fun isValid(): Boolean {
        return isValidAmount() && isValidFields() && isValidRating()
    }

    private fun isValidAmount(): Boolean = with(viewBinding) {
        val data = paymentPageData ?: return false
        val amount = getAmount()
        val result = when (data.getPaymentType()) {
            PaymentPageData.PaymentType.FIXED -> {
                true
            }
            PaymentPageData.PaymentType.MIN,
            PaymentPageData.PaymentType.VOLUNTARY -> {
                val minAmount = paymentPageData?.getPaymentValue() ?: 0.0
                val maxAmount = paymentPageData?.getAmount()?.range?.getMaximal() ?: 0.0

                amount in minAmount..maxAmount
            }
            PaymentPageData.PaymentType.GOAL -> {
                val maxAmount = paymentPageData?.getAmount()?.range?.getMaximal() ?: 0.0
                val minAmount = paymentPageData?.getAmount()?.range?.getMinimal() ?: 0.0

                amount in minAmount..maxAmount
            }
        }

        amountInputLayout.helperText = if (!result) getString(R.string.field_amount_incorrect) else null

        if (!result) {
            scrollToView(amountInputLayout)
            amountInputLayout.requestFocus()
        }

        return result
    }

    private fun scrollToView(view: View) = with(viewBinding) {
        var dY = view.top
        var parent = view.parent
        while (parent != null && parent != mainLayout) {
            dY += (parent as? View)?.top ?: 0
            parent = parent.parent
        }
        mainLayout.scrollTo(0, dY)
    }

    private fun getAmount(): Double {
        val data = paymentPageData ?: return 0.0

        return when (data.getPaymentType()) {
            PaymentPageData.PaymentType.FIXED -> data.getPaymentValue() ?: 0.0
            else -> viewBinding.amountInputLayout.getText()?.toDoubleOrNull() ?: 0.0
        }
    }

    private fun setAmount(value: Double) = with(viewBinding) {
        if (value > 0) amountInputLayout.setText(CommonHelper.formatDouble(value))
    }

    private fun addAmount(additionalAmount: Int) = with(viewBinding) {
        val currentAmount = amountInputLayout.getText()?.toDoubleOrNull() ?: 0.0
        val newAmount = currentAmount + additionalAmount
        amountInputLayout.setText(CommonHelper.formatDouble(newAmount))
    }

    private fun isValidFields(): Boolean = with(viewBinding) {
        val data = paymentPageData ?: return false
        val fields = data.getAvailableFields()

        return isValidField(fields[PaymentPageData.AvailableFields.FieldNames.NAME], nameInputLayout) &&
                isValidField(fields[PaymentPageData.AvailableFields.FieldNames.EMAIL], emailInputLayout) && isValidEmail() &&
                isValidField(fields[PaymentPageData.AvailableFields.FieldNames.PHONE_NUMBER], phoneInputLayout) && isValidPhone() &&
                isValidField(fields[PaymentPageData.AvailableFields.FieldNames.CITY], cityInputLayout) &&
                isValidField(fields[PaymentPageData.AvailableFields.FieldNames.COMMENT], commentInputLayout)
    }

    private fun isValidField(field: PaymentPageData.AvailableFields.AvailableFieldsValue?, formField: TextInputLayout): Boolean {
        if (field == null) return true
        val value = formField.editText?.text?.toString()
        val isValid = !field.getRequired() || !value.isNullOrEmpty()
        formField.helperText = if (!isValid) getString(R.string.field_required) else null
        if (!isValid) formField.requestFocus()
        return isValid
    }

    private fun isValidEmail(): Boolean {
        val formField = viewBinding.emailInputLayout
        val email = viewBinding.emailInput.text
        if (email.isNullOrEmpty()) return true
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        formField.helperText = if (!isValid) getString(R.string.field_invalid) else null
        if (!isValid) formField.requestFocus()
        return isValid
    }

    private fun isValidPhone(): Boolean {
        val formField = viewBinding.phoneInputLayout
        val email = viewBinding.phoneInput.text
        if (email.isNullOrEmpty()) return true
        val isValid = Patterns.PHONE.matcher(email).matches()
        formField.helperText = if (!isValid) getString(R.string.field_invalid) else null
        if (!isValid) formField.requestFocus()
        return isValid
    }

    private fun isValidRating(): Boolean {
        return true
    }


    private fun fillBackground() = with(viewBinding) {
        val background = paymentPageData?.getBackground()
        if (background.isNullOrEmpty()) {
            backgroundImageLayout.visibility = View.GONE
        } else {
            backgroundImageLayout.visibility = View.VISIBLE
            Glide.with(backgroundImageView).load(background).centerCrop().into(backgroundImageView)
        }
    }

    private fun fillUserData() = with(viewBinding) {
        val data = paymentPageData ?: return@with
        if (!data.getUserName().isNullOrEmpty()) {
            nameTextView.visibility = View.VISIBLE
            nameTextView.text = data.getUserName()
        } else nameTextView.visibility = View.GONE
        if (!data.getPaymentMessage().isNullOrEmpty()) nameMessageView.text = data.getPaymentMessage()
        Glide.with(avatarView).load(data.getUserAvatar())
            .placeholder(R.drawable.ic_empty_avatar)
            .error(R.drawable.ic_empty_avatar)
            .transform(
                CenterCrop(),
                RoundedCornersTransformation(
                    resources.getDimensionPixelSize(R.dimen.profile_avatar_big_radius),
                    0
                )
            ).into(avatarView)

    }

    private fun fillPaymentData() = with(viewBinding) {
        val data = paymentPageData ?: return@with
        when (data.getPaymentType()) {
            PaymentPageData.PaymentType.FIXED -> {
                priceInputLayout.visibility = View.GONE
                fixedPriceLayout.visibility = View.VISIBLE
                fixedPriceView.text = getString(R.string.main_balance, data.getPaymentValue()?.roundToInt())
            }
            PaymentPageData.PaymentType.MIN,
            PaymentPageData.PaymentType.VOLUNTARY -> {
                priceInputLayout.visibility = View.VISIBLE
                fixedPriceLayout.visibility = View.GONE

                priceGoalLayout.visibility = View.GONE
                fillPrice(data)
            }
            PaymentPageData.PaymentType.GOAL -> {
                priceInputLayout.visibility = View.VISIBLE
                fixedPriceLayout.visibility = View.GONE

                priceGoalLayout.visibility = View.VISIBLE

                fillGoal(data.getTarget())
                fillPrice(data)
            }
        }
    }

    private fun fillGoal(target: PaymentPageData.Target?) = with(viewBinding) {
        if (target == null) return@with
        val maxValue = (target.getAmount() ?: 0.0).roundToInt()
        val curValue = (target.getCurrentAmount() ?: 0.0).roundToInt()
        val leftValue = max(maxValue - curValue, 0)
        goalTargetView.text = getString(R.string.main_balance, maxValue)
        goalCurrentView.text = getString(R.string.main_balance, leftValue)
        val progress = if (maxValue > 0) (100 * curValue / maxValue) else 0
        goalProgressbarView.progress = progress
    }

    private fun fillPrice(paymentPageData: PaymentPageData?) = with(viewBinding) {
        val defaultMin = 0.0
        val defaultMax = 0.0

        val userMin = paymentPageData?.getAmount()?.range?.getMinimal() ?: 0.0
        val userMax = paymentPageData?.getAmount()?.range?.getMaximal() ?: 0.0
        val minValue = if (userMin > 1e-6) userMin else defaultMin
        val maxValue = if (userMax > 1e-6) userMax else defaultMax

        priceInputView.text = getString(R.string.link_edit_payment_page_set_price, minValue.toInt(), maxValue.toInt())
        val bubbles = bubbleValues.filter { it >= minValue && it <= maxValue }
        if (bubbles.isEmpty()) {
            bubbleLayout.visibility = View.GONE
        } else {
            bubbleLayout.visibility = View.VISIBLE
            bubbleLayout.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = BubblesAdapter(bubbles, object : BubblesAdapter.IBubblesAdapterListener {
                    override fun onBubbleClicked(item: Int) {
                        addAmount(item)
                    }
                })
                if (itemDecorationCount == 0) addItemDecoration(
                    LinearHorizontalDecorator(
                        resources.getDimensionPixelSize(R.dimen.bubble_outer_spacing),
                        resources.getDimensionPixelSize(R.dimen.bubble_inner_spacing)
                    )
                )
            }
        }
    }

    private fun fillInfoData() = with(viewBinding) {
        viewModel.getPaymentInfoData().observe(viewLifecycleOwner) { infoData ->
            val data = paymentPageData ?: return@observe
            setAmount(infoData.getAmount())
            val fields = data.getAvailableFields()
            fields[PaymentPageData.AvailableFields.FieldNames.NAME]?.let { field ->
                nameInputLayout.visibility = if (field.getEnabled()) View.VISIBLE else View.GONE
                nameInput.setText(infoData?.sender?.name)
            }
            fields[PaymentPageData.AvailableFields.FieldNames.EMAIL]?.let { field ->
                emailInputLayout.visibility = if (field.getEnabled()) View.VISIBLE else View.GONE
                emailInput.setText(infoData?.sender?.email)
            }
            fields[PaymentPageData.AvailableFields.FieldNames.PHONE_NUMBER]?.let { field ->
                phoneInputLayout.visibility = if (field.getEnabled()) View.VISIBLE else View.GONE
                phoneInput.setText(infoData?.sender?.phone)
            }
            fields[PaymentPageData.AvailableFields.FieldNames.CITY]?.let { field ->
                cityInputLayout.visibility = if (field.getEnabled()) View.VISIBLE else View.GONE
                cityInput.setText(infoData?.sender?.city)
            }
            fields[PaymentPageData.AvailableFields.FieldNames.COMMENT]?.let { field ->
                commentInputLayout.visibility = if (field.getEnabled()) View.VISIBLE else View.GONE
                commentInput.setText(infoData?.sender?.comment)
            }
            fillRating(infoData?.rating)
        }
    }

    private fun fillRating(ratingInfoData: PaymentInfoRatingData?) = with(viewBinding) {
        val ratingData = paymentPageData?.getRating()
        if (ratingData == null || !ratingData.getEnabled()) {
            ratingLayout.visibility = View.GONE
            return@with
        }
        ratingLayout.visibility = View.VISIBLE
        ratingStarsTitle.text = resources.getString(R.string.link_edit_payment_page_do_you_like, ratingData.getStarsText())
        ratingImage1.setOnClickListener { setRating(1) }
        ratingImage2.setOnClickListener { setRating(2) }
        ratingImage3.setOnClickListener { setRating(3) }
        ratingImage4.setOnClickListener { setRating(4) }
        ratingImage5.setOnClickListener { setRating(5) }

        ratingComponents = (ratingData.getComponents() ?: emptyList()).map {
            RatingComponent(it.id, it.title, it.imageUrl).apply {
                selected = ratingInfoData?.components?.contains(it.id) ?: false
            }
        }

        hideRatingComponents()
        if (ratingComponents.isNotEmpty()) {
            ratingComponentsTitle.text = ratingData.getComponentsText()
            ratingComponentsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                if (itemDecorationCount == 0)
                    addItemDecoration(
                        LinearHorizontalDecorator(
                            resources.getDimensionPixelSize(R.dimen.components_outer_spacing),
                            resources.getDimensionPixelSize(R.dimen.components_inner_spacing)
                        )
                    )
                adapter = ComponentsAdapter(ratingComponents, object : ComponentsAdapter.IComponentsAdapterListener {
                    override fun onItemClicked(item: RatingComponent) {
                        changeRatingComponent(item)
                    }

                })
            }
        }
        setRating(ratingInfoData?.score ?: 0)
    }

    private fun changeRatingComponent(item: RatingComponent) {
        val index = ratingComponents.indexOf(item)
        if (index < 0) return
        ratingComponents[index].selected = !ratingComponents[index].selected
        viewBinding.ratingComponentsRecyclerView.adapter?.notifyItemChanged(index)

        viewModel.putPaymentInfoRatingData(rating, ratingComponents)
    }

    private var rating: Int = 0
    private fun setRating(value: Int) = with(viewBinding) {
        rating = value
        ratingImage1.setImageResource(if (value >= 1) R.drawable.ic_star_filled else R.drawable.ic_star_empty)
        ratingImage2.setImageResource(if (value >= 2) R.drawable.ic_star_filled else R.drawable.ic_star_empty)
        ratingImage3.setImageResource(if (value >= 3) R.drawable.ic_star_filled else R.drawable.ic_star_empty)
        ratingImage4.setImageResource(if (value >= 4) R.drawable.ic_star_filled else R.drawable.ic_star_empty)
        ratingImage5.setImageResource(if (value >= 5) R.drawable.ic_star_filled else R.drawable.ic_star_empty)
        if (rating > 3) showRatingComponents()
        else hideRatingComponents()

        viewModel.putPaymentInfoRatingData(rating, ratingComponents)
    }

    private fun showRatingComponents() = with(viewBinding) {
        ratingComponentsTitle.visibility = View.VISIBLE
        ratingComponentsRecyclerView.visibility = View.VISIBLE
    }

    private fun hideRatingComponents() = with(viewBinding) {
        ratingComponentsTitle.visibility = View.GONE
        ratingComponentsRecyclerView.visibility = View.GONE
    }

    private fun isFeeVisible(): Boolean {
        return paymentPageData?.payerFee?.getIsEnabled() ?: false
    }

    private val requestFeeHandler = Handler()
    private var requestFeeRunnable: Runnable? = null
    private fun requestFeeValue(immediately: Boolean) {
        if (!isFeeVisible()) return
        viewBinding.amountInputLayout.helperText = null
        requestFeeRunnable?.let { requestFeeHandler.removeCallbacks(it) }
        requestFeeRunnable = Runnable {
            val amount = getAmount()
            viewModel.getFeeValue(amount).observe(viewLifecycleOwner) {
                feeAmount = it
                updateFeeValue()
                savePaymentInfoAmount()
            }
        }
        val delay = if (immediately) 0L else 1000L
        requestFeeRunnable?.let { requestFeeHandler.postDelayed(it, delay) }
    }

    private fun updateFeeValue() = with(viewBinding) {
        val formattedValue = DecimalFormat("#.#").format(feeAmount)
        feeHint.text = getString(R.string.link_edit_payment_page_tips_info_subtitle, formattedValue)
    }

    private fun fillLinksInfo() = with(viewBinding) {
        val licenseUrl = getString(R.string.license_url)
        val licenseSub = getString(R.string.link_edit_payment_page_info_text_subs)
        val licenseText = getString(R.string.link_edit_payment_page_info_text, licenseSub)
        val licenseStart = licenseText.indexOf(licenseSub)
        val spannedLicense = SpannableStringBuilder(licenseText)
        spannedLicense.setSpan(
            ClickableUrlSpan(licenseUrl, this@PaymentInfoFragment, true),
            licenseStart,
            licenseStart + licenseSub.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        licenseTextView.text = spannedLicense
        licenseTextView.movementMethod = LinkMovementMethod.getInstance()


        val privacyUrl = getString(R.string.google_captch_privacy)
        val termsUrl = getString(R.string.google_captch_terms)
        val privacySub = getString(R.string.link_edit_payment_page_captcha_text_subs_privacy)
        val termsSub = getString(R.string.link_edit_payment_page_captcha_text_subs_terms)
        val captchaText = getString(R.string.link_edit_payment_page_captcha_text, privacySub, termsSub)
        val privacyStart = captchaText.indexOf(privacySub)
        val termsStart = captchaText.indexOf(termsSub)
        val spannedCaptcha = SpannableStringBuilder(captchaText)
        spannedCaptcha.setSpan(
            ClickableUrlSpan(privacyUrl, this@PaymentInfoFragment, true),
            privacyStart,
            privacyStart + privacySub.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannedCaptcha.setSpan(
            ClickableUrlSpan(termsUrl, this@PaymentInfoFragment, true),
            termsStart,
            termsStart + termsSub.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )

        captchaTextView.text = spannedCaptcha
        captchaTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onUrlClick(url: String?) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        requireContext().let {
            if (intent.resolveActivity(it.packageManager) != null) {
                it.startActivity(intent)
            }
        }
    }

    private fun showSpinner() = with(viewBinding) {
        spinnerLayout.root.visibility = View.VISIBLE
    }

    private fun hideSpinner() = with(viewBinding) {
        spinnerLayout.root.visibility = View.GONE
    }

    companion object {
        private const val REQUEST_CODE_3DS = 1001
        private val bubbleValues = listOf(100, 200, 300, 500, 1000, 3000, 5000)

        fun newInstance() = PaymentInfoFragment()

    }

}