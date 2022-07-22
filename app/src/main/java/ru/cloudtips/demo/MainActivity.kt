package ru.cloudtips.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.cloudtips.demo.databinding.ActivityMainBinding
import ru.cloudtips.sdk.CloudTipsSDK
import ru.cloudtips.sdk.TipsConfiguration
import ru.cloudtips.sdk.TipsData


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_TIPS = 1
    }


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonContinue.setOnClickListener {
            val phone = binding.editTextPhone.text.toString()

            if (phone.length != 12 || phone.take(2) != "+7") {
                Toast.makeText(this, R.string.main_phone_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipsData = TipsData(phone, "CloudTips demo user", "partner_id")
            val configuration = if (BuildConfig.DEBUG) {
                TipsConfiguration(tipsData, true) // Режим тестирования
            } else {
                TipsConfiguration(tipsData)
            }
            CloudTipsSDK.getInstance().start(configuration, this, REQUEST_CODE_TIPS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        when (requestCode) {
            REQUEST_CODE_TIPS -> {
                val transactionStatus =
                    data?.getSerializableExtra(CloudTipsSDK.IntentKeys.TransactionStatus.name) as? CloudTipsSDK.TransactionStatus

                if (transactionStatus != null) {
                    if (transactionStatus == CloudTipsSDK.TransactionStatus.Succeeded) {
                        Toast.makeText(this, "Чаевые получены", Toast.LENGTH_SHORT).show()
                    } else if (transactionStatus == CloudTipsSDK.TransactionStatus.Cancelled) {
                        Toast.makeText(
                            this,
                            "Пользователь закрыл форму не оставив чаевых",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                Unit
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
}