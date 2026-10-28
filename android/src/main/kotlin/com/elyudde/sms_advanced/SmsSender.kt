package com.elyudde.sms_advanced

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import com.elyudde.sms_advanced.permisions.Permissions
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import java.util.*


/**
 * Created by crazygenius on 1/08/21.
 */
@TargetApi(Build.VERSION_CODES.DONUT)
internal class SmsSenderMethodHandler(
    val context: Context,
    private val result: MethodChannel.Result,
    private val address: String,
    private val body: String,
    private val sentId: Int,
    private val sessionId: String,
    private val subId: Int?,
    private val requestCode: Int,
    private val binding: ActivityPluginBinding
) :
    RequestPermissionsResultListener {
    private val permissionsList =
        arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE)
    private var completed = false

    fun handle(permissions: Permissions) {
        if (permissions.checkAndRequestPermission(permissionsList, requestCode)) {
            completeAndSend()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode != this.requestCode) {
            return false
        }
        if (completed) {
            return false
        }
        val isOk = permissions.contentEquals(permissionsList) &&
            grantResults.size == permissionsList.size &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (isOk) {
            completeAndSend()
        } else {
            completed = true
            removeListenerAfterDispatch()
            result.error("#01", "permission denied for sending sms", null)
        }
        return true
    }

    private fun completeAndSend() {
        if (completed) {
            return
        }
        completed = true
        removeListenerAfterDispatch()
        try {
            sendSmsMessage()
        } catch (error: Exception) {
            result.error("#04", "could not send sms", null)
        }
    }

    private fun removeListenerAfterDispatch() {
        // Flutter iterates a HashSet of listeners while delivering the result.
        Handler(Looper.getMainLooper()).post {
            binding.removeRequestPermissionsResultListener(this)
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun sendSmsMessage() {
        val sentIntent = Intent("${context.packageName}.sms_advanced.SMS_SENT")
            .setPackage(context.packageName)
            .setData(Uri.parse("sms-advanced://callback/$sessionId/$sentId/sent"))
            .putExtra("sentId", sentId)
            .putExtra("sessionId", sessionId)
        val sentPendingIntent = PendingIntent.getBroadcast(
            context,
            sentId,
            sentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )
        val deliveredIntent = Intent("${context.packageName}.sms_advanced.SMS_DELIVERED")
            .setPackage(context.packageName)
            .setData(Uri.parse("sms-advanced://callback/$sessionId/$sentId/delivered"))
            .putExtra("sentId", sentId)
            .putExtra("sessionId", sessionId)
        val deliveredPendingIntent = PendingIntent.getBroadcast(
            context,
            sentId,
            deliveredIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )
        val sms: SmsManager = if (subId == null) {
            SmsManager.getDefault()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SmsManager.getSmsManagerForSubscriptionId(subId)
            } else {
                result.error("#03", "this version of android does not support multicard SIM", null)
                return
            }
        }
        sms.sendTextMessage(address, null, body, sentPendingIntent, deliveredPendingIntent)
        result.success(null)
    }

    companion object {
        private val sms = SmsManager.getDefault()
    }
}

@TargetApi(Build.VERSION_CODES.DONUT)
internal class SmsSender(val context: Context, private val binding: ActivityPluginBinding) : MethodCallHandler {
    private val permissions: Permissions = Permissions(context, binding.activity as FlutterFragmentActivity)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "sendSMS") {
            val address = call.argument<String>("address")
            val body = call.argument<String>("body")
            val sentId = call.argument<Int>("sentId")
            val sessionId = call.argument<String>("sessionId")
            val subId = call.argument<Int>("subId")
            if (address.isNullOrBlank()) {
                result.error("#02", "missing argument 'address'", null)
            } else if (body == null) {
                result.error("#02", "missing argument 'body'", null)
            } else if (sentId == null) {
                result.error("#02", "missing argument 'sentId'", null)
            } else if (sessionId.isNullOrBlank()) {
                result.error("#02", "missing argument 'sessionId'", null)
            } else {
                val handler =
                    SmsSenderMethodHandler(context, result, address, body, sentId, sessionId, subId,
                        Permissions.nextRequestCode(), binding)
                binding.addRequestPermissionsResultListener(handler)
                handler.handle(permissions)
            }
        } else {
            result.notImplemented()
        }
    }

}
