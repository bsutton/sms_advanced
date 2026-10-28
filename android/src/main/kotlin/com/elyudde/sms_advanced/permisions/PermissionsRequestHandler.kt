package com.elyudde.sms_advanced.permisions

import android.annotation.TargetApi
import android.os.Build
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


/**
 * Created by crazygenius on 1/08/21.
 */
internal class PermissionsRequestHandler : RequestPermissionsResultListener {
    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        if (currentRequest?.id != requestCode) {
            return false
        }
        currentRequest = requests.poll()
        currentRequest?.execute()
        return false
    }

    companion object {
        private val requests: Queue<PermissionsRequest> = LinkedBlockingQueue()
        private var currentRequest: PermissionsRequest? = null
        @TargetApi(Build.VERSION_CODES.M)
        fun requestPermissions(permissionsRequest: PermissionsRequest) {
            if (currentRequest == null) {
                currentRequest = permissionsRequest
                permissionsRequest.execute()
            } else {
                requests.add(permissionsRequest)
            }
        }
    }
}
