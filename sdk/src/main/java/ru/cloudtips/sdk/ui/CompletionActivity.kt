package ru.cloudtips.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import ru.cloudtips.sdk.CloudTipsSDK
import ru.cloudtips.sdk.R
import ru.cloudtips.sdk.api.HOST
import ru.cloudtips.sdk.base.BaseActivity
import ru.cloudtips.sdk.databinding.ActivityCompletionBinding

class CompletionActivity : BaseActivity() {

    companion object {

        private const val EXTRA_PHOTO_URL = "EXTRA_PHOTO_URL"
        private const val EXTRA_NAME = "EXTRA_NAME"
        private const val EXTRA_SUCCESS = "EXTRA_SUCCESS"
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_TEXT = "EXTRA_TEXT"

        fun getStartIntent(context: Context, photoUrl: String?, name: String?, success: Boolean, title: String?, text: String?): Intent {
            val intent = Intent(context, CompletionActivity::class.java)
            intent.putExtra(EXTRA_PHOTO_URL, photoUrl)
            intent.putExtra(EXTRA_NAME, name)
            intent.putExtra(EXTRA_SUCCESS, success)
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_TEXT, text)

            return intent
        }
    }

    private val photoUrl by lazy {
        intent.getStringExtra(EXTRA_PHOTO_URL)
    }

    private val name by lazy {
        intent.getStringExtra(EXTRA_NAME)
    }

    private val success by lazy {
        intent.getBooleanExtra(EXTRA_SUCCESS, false)
    }

    private val title by lazy {
        intent.getStringExtra(EXTRA_TITLE)
    }

    private val text by lazy {
        intent.getStringExtra(EXTRA_TEXT)
    }

    private lateinit var binding: ActivityCompletionBinding

    override fun showLoading() {
    }

    override fun hideLoading() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (photoUrl != "") {

            Glide
                .with(this)
                .load(photoUrl)
                .apply(RequestOptions.bitmapTransform(CenterCrop()))
                .placeholder(R.drawable.no_avatar_tips_activity)
                .error(R.drawable.no_avatar_tips_activity)
                .circleCrop()
                .into(binding.imageViewAvatar)
        }

        if (name.isNullOrEmpty()) {
            binding.textViewName.visibility = View.GONE
        } else {
            binding.textViewName.visibility = View.VISIBLE
            binding.textViewName.text = name
        }

        if (success) {
            binding.imageViewStatus.setImageResource(R.drawable.ic_status_complete)

            binding.textViewReaction.visibility = View.VISIBLE

            if (name.isNullOrEmpty()) {
                binding.textViewReaction.setText(R.string.completion_success_true_name_is_empty)
            } else {
                binding.textViewReaction.setText(R.string.completion_success_true)
            }

            binding.textViewButtonAgain.setText(R.string.completion_send_again)
        } else {
            binding.imageViewStatus.setImageResource(R.drawable.ic_status_error)

            binding.textViewErrorCaption.visibility = View.VISIBLE
            binding.textViewErrorDescription.visibility = View.VISIBLE

            binding.textViewErrorCaption.text = title
            binding.textViewErrorDescription.text = text

            binding.textViewButtonAgain.setText(R.string.completion_try_again)
        }

        binding.buttonAgain.setOnClickListener {
            onBackPressed()
        }

        binding.buttonClose.setOnClickListener {
            close()
        }

        binding.imageViewClose.setOnClickListener {
            close()
        }
    }

    private fun close() {
        val status = if (success) {
            CloudTipsSDK.TransactionStatus.Succeeded
        } else {
            CloudTipsSDK.TransactionStatus.Cancelled
        }
        setResult(RESULT_OK, Intent().apply {
            putExtra(CloudTipsSDK.IntentKeys.TransactionStatus.name, status)
        })
        finish()
    }
}