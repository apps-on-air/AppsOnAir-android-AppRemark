package com.appsonair.appremark.testutil

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Looper
import org.junit.Assert.fail
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetwork

object TestUtils {

    // Robolectric runs the main looper paused, so OkHttp callbacks posted via runOnUiThread
    // only execute when the looper is idled from the test thread.
    fun awaitUntil(timeoutMs: Long = 5000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            Thread.sleep(10)
        }
        fail("Condition not met within ${timeoutMs}ms")
    }

    fun setAppId(context: Context, appId: String?) {
        val packageInfo = shadowOf(context.packageManager).getInternalMutablePackageInfo(context.packageName)
        packageInfo.applicationInfo.metaData = Bundle().apply {
            if (appId != null) putString("appId", appId)
        }
    }

    fun reportNetwork(context: Context, available: Boolean) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = ShadowNetwork.newInstance(1)
        shadowOf(cm).networkCallbacks.forEach {
            if (available) it.onAvailable(network) else it.onLost(network)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }
}
