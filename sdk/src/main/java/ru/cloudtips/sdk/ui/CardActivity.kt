package ru.cloudtips.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.cloudpayments.sdk.card.Card
import ru.cloudpayments.sdk.card.CardType
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment
import ru.cloudpayments.sdk.util.TextWatcherAdapter
import ru.cloudtips.sdk.CloudTipsSDK
import ru.cloudtips.sdk.R
import ru.cloudtips.sdk.api.Api
import ru.cloudtips.sdk.api.models.PublicId
import ru.cloudtips.sdk.base.PayActivity
import ru.cloudtips.sdk.databinding.ActivityCardBinding
import ru.tinkoff.decoro.MaskDescriptor
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher
import java.text.DecimalFormat

class CardActivity : PayActivity(), ThreeDsDialogFragment.ThreeDSDialogListener {

    companion object {

        private const val EXTRA_PHOTO_URL = "EXTRA_PHOTO_URL"
        private const val EXTRA_NAME = "EXTRA_NAME"
        private const val EXTRA_LAYOUT_ID = "EXTRA_LAYOUT_ID"
        private const val EXTRA_AMOUNT = "EXTRA_AMOUNT"
        private const val EXTRA_AMOUNT_FEE = "EXTRA_AMOUNT_FEE"
        private const val EXTRA_COMMENT = "EXTRA_COMMENT"
        private const val EXTRA_FEE_FROM_PAYER = "EXTRA_FEE_FROM_PAYER"

        fun getStartIntent(
            context: Context,
            photoUrl: String,
            name: String,
            layoutId: String,
            amount: String,
            amountFee: Double,
            comment: String,
            feeFromPayer: Boolean
        ): Intent {
            val intent = Intent(context, CardActivity::class.java)
            intent.putExtra(EXTRA_PHOTO_URL, photoUrl)
            intent.putExtra(EXTRA_NAME, name)
            intent.putExtra(EXTRA_LAYOUT_ID, layoutId)
            intent.putExtra(EXTRA_AMOUNT, amount)
            intent.putExtra(EXTRA_AMOUNT_FEE, amountFee)
            intent.putExtra(EXTRA_COMMENT, comment)
            intent.putExtra(EXTRA_FEE_FROM_PAYER, feeFromPayer)

            return intent
        }
    }

    private val photoUrl by lazy {
        intent.getStringExtra(EXTRA_PHOTO_URL)
    }

    private val name by lazy {
        intent.getStringExtra(EXTRA_NAME)
    }

    private val layoutId by lazy {
        intent.getStringExtra(EXTRA_LAYOUT_ID)
    }

    private val amount by lazy {
        intent.getStringExtra(EXTRA_AMOUNT)
    }

    private val amountFee by lazy {
        intent.getDoubleExtra(EXTRA_AMOUNT_FEE, 0.0)
    }

    private val comment by lazy {
        intent.getStringExtra(EXTRA_COMMENT)
    }

    private val feeFromPayer by lazy {
        intent.getBooleanExtra(EXTRA_FEE_FROM_PAYER, false)
    }


    private lateinit var publicId: String

    private val cardNumberFormatWatcher by lazy {
        val descriptor = MaskDescriptor.ofRawMask("____ ____ ____ ____ ___")
            .setTerminated(true)
            .setForbidInputWhenFilled(true)

        DescriptorFormatWatcher(UnderscoreDigitSlotsParser(), descriptor)
    }

    private val cardExpFormatWatcher by lazy {
        val descriptor = MaskDescriptor.ofRawMask("__/__")
            .setTerminated(true)
            .setForbidInputWhenFilled(true)

        DescriptorFormatWatcher(UnderscoreDigitSlotsParser(), descriptor)
    }

    private lateinit var binding: ActivityCardBinding

    override fun showLoading() {
        binding.loading.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
    }

    override fun hideLoading() {
        binding.loading.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPublicId(layoutId)

        initUI()
    }

    private fun initUI() {

        binding.imageViewClose.setOnClickListener {
            setResult(RESULT_OK, Intent().apply {
                putExtra(CloudTipsSDK.IntentKeys.TransactionStatus.name, CloudTipsSDK.TransactionStatus.Cancelled)
            })
            finish()
        }

        cardNumberFormatWatcher.installOn(binding.editTextCardNumber)
        cardExpFormatWatcher.installOn(binding.editTextExpDate)

        binding.editTextCardNumber.addTextChangedListener(object : TextWatcherAdapter() {

            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)

                errorMode(false, binding.editTextCardNumber)

                val cardNumber = s.toString().replace(" ", "")
                updatePaymentSystemIcon(cardNumber)
            }
        })

        binding.editTextExpDate.addTextChangedListener(object : TextWatcherAdapter() {

            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)

                errorMode(false, binding.editTextExpDate)
            }
        })

        binding.editTextCode.addTextChangedListener(object : TextWatcherAdapter() {

            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)

                errorMode(false, binding.editTextCode)
            }
        })

        val amountWithFee = DecimalFormat("#.#").format(amountFee)
        binding.textViewButtonPay.text = getString(R.string.card_pay) + " " + amountWithFee + " " + getString(R.string.app_rub_symbol)

        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }

        binding.buttonPay.setOnClickListener {

            if (isValid() && cryptogram() != null) {

                verifyV3(amount(), layoutId())
            }
        }

        initRecaptchaTextView(binding.textViewRecaptcha)
    }

    private fun getPublicId(layoutId: String?) {
        showLoading()
        compositeDisposable.add(
            Api.getPublicId(layoutId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ publicId -> checkGetPublicIdResponse(publicId) }, this::handleError)
        )
    }

    private fun checkGetPublicIdResponse(response: Api.ResponseWrapper<PublicId>) {
        this.publicId = response.data?.publicId ?: ""
        hideLoading()
    }

    private fun updatePaymentSystemIcon(cardNumber: String) {
        val cardType = CardType.getType(cardNumber)
        val psIcon = cardType.getIconRes()
        binding.imageViewPaymentSystem.setImageResource(psIcon ?: 0)
    }

    private fun isValid(): Boolean {
        val cardNumberIsValid = Card.isValidNumber(binding.editTextCardNumber.text.toString())
        val cardExpIsValid = true//Card.isValidExpDate(binding.editTextExpDate.text.toString())
        val cardCvvIsValid = binding.editTextCode.text.toString().length == 3

        errorMode(!cardNumberIsValid, binding.editTextCardNumber)
        errorMode(!cardExpIsValid, binding.editTextExpDate)
        errorMode(!cardCvvIsValid, binding.editTextCode)

        return cardNumberIsValid && cardExpIsValid && cardCvvIsValid
    }

    override fun cryptogram(): String {

        val cardNumber = binding.editTextCardNumber.text.toString()
        val cardExp = binding.editTextExpDate.text.toString()
        val cardCvv = binding.editTextCode.text.toString()

        val cryptogram = Card.cardCryptogram(cardNumber, cardExp, cardCvv, publicId)

        return cryptogram ?: ""
    }

    override fun layoutId(): String? {
        return layoutId
    }

    override fun amount(): String? {
        return amount
    }

    override fun comment(): String? {
        return comment
    }

    override fun photoUrl(): String? {
        return photoUrl
    }

    override fun name(): String? {
        return name
    }

    override fun feeFromPayer(): Boolean {
        return feeFromPayer
    }
}