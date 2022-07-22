package ru.cloudtips.sdk

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.cloudtips.sdk.CloudTipsSDK.IntentKeys
import ru.cloudtips.sdk.CloudTipsSDK.TransactionStatus
import ru.cloudtips.sdk.CloudTipsSDK.TransactionStatus.Cancelled
import ru.cloudtips.sdk.CloudTipsSDK.TransactionStatus.Succeeded
import ru.cloudtips.sdk.ui.TipsActivity

internal class CloudTipsIntentSender : ActivityResultContract<TipsConfiguration, TransactionStatus>() {
  override fun createIntent(context: Context, input: TipsConfiguration): Intent {
    return TipsActivity.getStartIntent(context, input)
  }

  override fun parseResult(resultCode: Int, intent: Intent?): TransactionStatus {
    return if (intent?.getSerializableExtra(IntentKeys.TransactionStatus.name) == Succeeded
    ) Succeeded
    else {
      Cancelled
    }
  }
}