package ru.cloudtips.sdk

import androidx.activity.result.ActivityResultLauncher
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