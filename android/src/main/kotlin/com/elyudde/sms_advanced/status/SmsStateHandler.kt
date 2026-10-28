package com.elyudde.sms_advanced.status

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink


/**
 * Created by crazygenius on 1/08/21.
 */
class SmsStateHandler(private val context: Context) : EventChannel.StreamHandler {
    private var smsStateChangeReceiver: BroadcastReceiver? = null
    override fun onListen(argument: Any?, eventSink: EventSink) {
        onCancel(null)
        val newReceiver = SmsStateChangeReceiver(eventSink)
        val filter = IntentFilter().apply {
            addAction("${context.packageName}.sms_advanced.SMS_SENT")
            addAction("${context.packageName}.sms_advanced.SMS_DELIVERED")
            addDataScheme("sms-advanced")
        }
        ContextCompat.registerReceiver(
            context,
            newReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        smsStateChangeReceiver = newReceiver
    }

    override fun onCancel(argument: Any?) {
        smsStateChangeReceiver?.let { context.unregisterReceiver(it) }
        smsStateChangeReceiver = null
    }
}
