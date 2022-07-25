package ru.cloudtips.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.cloudtips.sdk.CloudTipsSDK.TransactionStatus
import ru.cloudtips.sdk.ui.TipsActivity

interface CloudTipsSDK {

	fun start(configuration: TipsConfiguration, from: AppCompatActivity, requestCode: Int)
	fun launcher(from: AppCompatActivity, result: (TransactionStatus) -> Unit) : ActivityResultLauncher<TipsConfiguration>
	fun launcher(from: Fragment, result: (TransactionStatus) -> Unit) : ActivityResultLauncher<TipsConfiguration>

	enum class TransactionStatus {
		Succeeded,
		Cancelled;
	}
	enum class IntentKeys {
		TransactionStatus;
	}

	companion object {
		fun getInstance(): CloudTipsSDK {
			return CloudTipsSDKImpl()
		}

		fun getContract(): ActivityResultContract<TipsConfiguration, TransactionStatus?> {
			return CloudTipsContract()
		}
	}
}

internal class CloudTipsSDKImpl: CloudTipsSDK {

	override fun start(configuration: TipsConfiguration, from: AppCompatActivity, requestCode: Int) {
		from.startActivityForResult(TipsActivity.getStartIntent(from, configuration), requestCode)
	}

	override fun launcher(
		from: AppCompatActivity,
		result: (TransactionStatus) -> Unit): ActivityResultLauncher<TipsConfiguration>{
		return from.registerForActivityResult(CloudTipsIntentSender(), result)
	}

	override fun launcher(
		from: Fragment,
		result: (TransactionStatus) -> Unit
	): ActivityResultLauncher<TipsConfiguration> {
		return from.registerForActivityResult(CloudTipsIntentSender(), result)
	}
}

internal class CloudTipsContract :
	ActivityResultContract<TipsConfiguration, CloudTipsSDK.TransactionStatus?>() {
	override fun createIntent(context: Context, configuration: TipsConfiguration): Intent {
		return TipsActivity.getStartIntent(context, configuration)
	}

	override fun parseResult(resultCode: Int, result: Intent?): CloudTipsSDK.TransactionStatus? {
		if (result == null) return null
		if (resultCode != Activity.RESULT_OK) return null
		return when (val transactionStatus =
			result.getSerializableExtra(CloudTipsSDK.IntentKeys.TransactionStatus.name)) {
			is CloudTipsSDK.TransactionStatus -> transactionStatus
			else -> null
		}
	}
}