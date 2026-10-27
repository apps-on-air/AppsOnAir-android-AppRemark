package com.appsonair.appremark.services

import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppRemarkListenerTest {

    @Test
    fun setRemarkResponse_forwardsToRegisteredCallback() {
        var received: JSONObject? = null
        AppRemarkService.initialize(
            ApplicationProvider.getApplicationContext(),
            shakeGestureEnable = false
        ) { received = it }
        val payload = JSONObject().put("status", "SUCCESS")

        AppRemarkListener.setRemarkResponse(payload)

        assertSame(payload, received)
    }
}
