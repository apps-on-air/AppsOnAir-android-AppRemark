package com.appsonair.appremark.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiUtilsTest {

    @Test
    fun endpoints_areRelativeToBaseUrl() {
        assertEquals("signed-url", ApiUtils.getSignedUrl)
        assertEquals("feedback", ApiUtils.createRemark)
    }
}
