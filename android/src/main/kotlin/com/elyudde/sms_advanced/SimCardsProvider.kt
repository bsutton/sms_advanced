package com.elyudde.sms_advanced

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.elyudde.sms_advanced.permisions.Permissions
import com.elyudde.sms_advanced.telephony.TelephonyManager
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by crazygenius on 1/08/21.
 */
internal class SimCardsHandler(
    private val context: Context,
    private val result: MethodChannel.Result
) :
 
class RequestPermissionsResultListener {
    private val permissionsList = arrayOf(Manifest.permission.READ_PHONE_STATE)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        Log.d("RequestPermissions", "permissionsList: ${permissionsList.joinToString()}")
        Log.d("RequestPermissions", "permissions: ${permissions.joinToString()}")
        Log.d("RequestPermissions", "grantResults: ${grantResults.joinToString()}")

        if (requestCode != Permissions.READ_PHONE_STATE) {
            return false
        }
        var isOk = true
        for ((index, res) in grantResults.withIndex()) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                isOk = false
                Log.d("RequestPermissions", "Permission denied: ${permissions[index]}")
                break
            }
        }
        if (isOk) {
            simCards
            return true
        }
        result.error("#01", "permission READ_PHONE_STATE denied -2 ", null)
        return false
    }

    fun handle(permissions: Permissions) {
        Log.d("RequestPermissions", "permissionsList: ${permissionsList.joinToString()}")
        if (permissions.checkAndRequestPermission(permissionsList, Permissions.READ_PHONE_STATE)) {
            simCards
        }
    }
}
    private val simCards: Unit
        @RequiresApi(Build.VERSION_CODES.M) private get() {
            val simCards = JSONArray()
            try {
                val telephonyManager = TelephonyManager(
                    context
                )
                val phoneCount = telephonyManager.simCount
                for (i in 0 until phoneCount) {
                    val simCard = JSONObject()
                    simCard.put("slot", i + 1)
                    simCard.put("imei", telephonyManager.getSimId(i))
                    simCard.put("state", telephonyManager.getSimState(i))
                    simCards.put(simCard)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                result.error("2", e.message, null)
                return
            }
            result.success(simCards)
        }

}

internal class SimCardsProvider(val context: Context, private val binding: ActivityPluginBinding) : MethodCallHandler {
    private val permissions: Permissions = Permissions(context, binding.activity as FlutterFragmentActivity)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method != "getSimCards") {
            result.notImplemented()
        } else {
            getSimCards(result)
        }
    }

    private fun getSimCards(result: MethodChannel.Result) {
        val handler = SimCardsHandler(context, result)
        binding.addRequestPermissionsResultListener(handler)
        handler.handle(permissions)
    }

}