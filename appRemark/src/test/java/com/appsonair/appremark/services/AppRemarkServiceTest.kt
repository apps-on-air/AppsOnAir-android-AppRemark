package com.appsonair.appremark.services

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.test.core.app.ApplicationProvider
import com.appsonair.appremark.activities.RemarkActivity
import com.appsonair.appremark.enums.ShakeSensitivity
import com.appsonair.appremark.testutil.TestUtils
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSensor

@RunWith(RobolectricTestRunner::class)
class AppRemarkServiceTest {

    // ContextWrapper subclasses in this package print as "com.appsonair.appremark...", which
    // initialize() treats as an SDK-internal context
    private class SdkInternalContext(base: Context) : ContextWrapper(base)

    private lateinit var app: Application
    private lateinit var sensorManager: SensorManager

    private val defaults = mapOf(
        "pagebackgroundcolor" to "#E8F1FF",
        "appbarbackgroundcolor" to "#E8F1FF",
        "appbartitletext" to "Add Remark",
        "appbartitlecolor" to "#000000",
        "remarktypelabeltext" to "Remark Type",
        "descriptionlabeltext" to "Description",
        "descriptionhinttext" to "Add description here…",
        "descriptionmaxlength" to 255,
        "buttontext" to "Submit",
        "buttontextcolor" to "#FFFFFF",
        "buttonbackgroundcolor" to "#007AFF",
        "labelcolor" to "#000000",
        "hintcolor" to "#B1B1B3",
        "inputtextcolor" to "#000000"
    )

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shadowOf(sensorManager).addSensor(ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER))
        ShakeDetectorService.isProcessing = false
        AppRemarkService.setAdditionalMetaData(emptyMap())
    }

    private fun options() = AppRemarkService.Properties.getOptions()

    private fun init(options: Map<String, Any>, shake: Boolean = false) {
        AppRemarkService.initialize(app, shakeGestureEnable = shake, options = options) {}
    }

    // option handling

    @Test
    fun initialize_withDefaults_exposesAllDefaultOptions() {
        AppRemarkService.initialize(app) {}

        assertEquals(defaults, options())
    }

    @Test
    fun initialize_withEmptyOptions_fallsBackToDefaults() {
        init(emptyMap())

        assertEquals(defaults, options())
    }

    @Test
    fun initialize_validOptions_areApplied() {
        val custom = mapOf(
            "pageBackgroundColor" to "#101010",
            "appBarBackgroundColor" to "#202020",
            "appBarTitleText" to "Feedback",
            "appBarTitleColor" to "#303030",
            "remarkTypeLabelText" to "Kind",
            "descriptionLabelText" to "Details",
            "descriptionHintText" to "Tell us more",
            "descriptionMaxLength" to 500,
            "buttonText" to "Send",
            "buttonTextColor" to "#404040",
            "buttonBackgroundColor" to "#505050",
            "labelColor" to "#606060",
            "hintColor" to "#707070",
            "inputTextColor" to "#80808080"
        )

        init(custom)

        assertEquals(custom.mapKeys { it.key.lowercase() }, options())
    }

    @Test
    fun initialize_keysAreCaseInsensitive() {
        init(mapOf("APPBARTITLETEXT" to "Upper", "ButtonText" to "Mixed"))

        assertEquals("Upper", options()["appbartitletext"])
        assertEquals("Mixed", options()["buttontext"])
    }

    @Test
    fun initialize_invalidColors_fallBackToDefaults() {
        init(
            mapOf(
                "pageBackgroundColor" to "not-a-color",
                "appBarBackgroundColor" to "#GGGGGG",
                "appBarTitleColor" to "",
                "buttonTextColor" to "#FFF", // short hex isn't supported by Color.parseColor
                "buttonBackgroundColor" to "007AFF", // missing '#'
                "labelColor" to 12345,
                "hintColor" to "#12345",
                "inputTextColor" to " "
            )
        )

        assertEquals("#E8F1FF", options()["pagebackgroundcolor"])
        assertEquals("#E8F1FF", options()["appbarbackgroundcolor"])
        assertEquals("#000000", options()["appbartitlecolor"])
        assertEquals("#FFFFFF", options()["buttontextcolor"])
        assertEquals("#007AFF", options()["buttonbackgroundcolor"])
        assertEquals("#000000", options()["labelcolor"])
        assertEquals("#B1B1B3", options()["hintcolor"])
        assertEquals("#000000", options()["inputtextcolor"])
    }

    @Test
    fun initialize_namedColors_areAccepted() {
        init(mapOf("labelColor" to "red", "hintColor" to "gray"))

        assertEquals("red", options()["labelcolor"])
        assertEquals("gray", options()["hintcolor"])
    }

    @Test
    fun initialize_emptyText_fallsBackToDefaults() {
        init(
            mapOf(
                "appBarTitleText" to "",
                "remarkTypeLabelText" to "",
                "descriptionLabelText" to "",
                "descriptionHintText" to "",
                "buttonText" to ""
            )
        )

        assertEquals("Add Remark", options()["appbartitletext"])
        assertEquals("Remark Type", options()["remarktypelabeltext"])
        assertEquals("Description", options()["descriptionlabeltext"])
        assertEquals("Add description here…", options()["descriptionhinttext"])
        assertEquals("Submit", options()["buttontext"])
    }

    @Test
    fun initialize_nonStringText_isStringified() {
        init(mapOf("buttonText" to 42, "appBarTitleText" to true))

        assertEquals("42", options()["buttontext"])
        assertEquals("true", options()["appbartitletext"])
    }

    @Test
    fun initialize_whitespaceText_isKept() {
        // only empty strings are rejected, blank ones pass validation
        init(mapOf("buttonText" to "  "))

        assertEquals("  ", options()["buttontext"])
    }

    @Test
    fun initialize_maxLength_acceptsNumericStrings() {
        init(mapOf("descriptionMaxLength" to "120"))

        assertEquals(120, options()["descriptionmaxlength"])
    }

    @Test
    fun initialize_maxLength_rejectsNonPositiveOrNonInteger() {
        for (invalid in listOf<Any>(0, -10, "abc", "", 12.5, "9999999999")) {
            init(mapOf("descriptionMaxLength" to invalid))
            assertEquals("for $invalid", 255, options()["descriptionmaxlength"])
        }
    }

    @Test
    fun initialize_unknownKeys_arePreservedLowercased() {
        init(mapOf("CustomKey" to "value"))

        assertEquals("value", options()["customkey"])
    }

    @Test
    fun initialize_replacesOptionsFromPreviousCall() {
        init(mapOf("buttonText" to "First"))
        init(mapOf("appBarTitleText" to "Second"))

        assertEquals("Submit", options()["buttontext"])
        assertEquals("Second", options()["appbartitletext"])
    }

    @Test
    fun initialize_doesNotMutateCallerMap() {
        val callerOptions = mutableMapOf<String, Any>("buttonText" to "")

        init(callerOptions)

        assertEquals(mapOf<String, Any>("buttonText" to ""), callerOptions)
    }

    @Test
    fun initialize_withoutAppId_stillAppliesOptions() {
        TestUtils.setAppId(app, null)

        init(mapOf("buttonText" to "Go"))

        assertEquals("Go", options()["buttontext"])
    }

    @Test
    fun initialize_fromSdkInternalContext_keepsExistingOptionsAndSkipsShakeDetection() {
        init(mapOf("buttonText" to "Kept"))
        var received: JSONObject? = null

        AppRemarkService.initialize(
            SdkInternalContext(app),
            shakeGestureEnable = true,
            options = mapOf("buttonText" to "Ignored")
        ) { received = it }

        assertEquals("Kept", options()["buttontext"])
        assertTrue(shadowOf(sensorManager).listeners.isEmpty())
        // the callback is still replaced
        val payload = JSONObject()
        AppRemarkService.setRemarkResponse(payload)
        assertSame(payload, received)
    }

    // shake detection

    @Test
    fun initialize_shakeEnabled_registersAccelerometerListener() {
        init(emptyMap(), shake = true)

        assertEquals(1, shadowOf(sensorManager).listeners.size)
    }

    @Test
    fun initialize_shakeDisabled_doesNotRegisterListener() {
        init(emptyMap(), shake = false)

        assertTrue(shadowOf(sensorManager).listeners.isEmpty())
    }

    @Test
    fun initialize_defaultsToShakeEnabled() {
        AppRemarkService.initialize(app) {}

        assertEquals(1, shadowOf(sensorManager).listeners.size)
    }

    @Test
    fun initialize_repeatedCalls_keepSingleListener() {
        AppRemarkService.initialize(app, shakeSensitivity = ShakeSensitivity.LOW) {}
        AppRemarkService.initialize(app, shakeSensitivity = ShakeSensitivity.HIGH) {}

        assertEquals(1, shadowOf(sensorManager).listeners.size)
    }

    // callbacks and metadata

    @Test
    fun setRemarkResponse_invokesLatestCallback() {
        val first = mutableListOf<JSONObject>()
        val second = mutableListOf<JSONObject>()
        AppRemarkService.initialize(app, shakeGestureEnable = false) { first += it }
        AppRemarkService.initialize(app, shakeGestureEnable = false) { second += it }

        AppRemarkService.setRemarkResponse(JSONObject().put("status", "SUCCESS"))

        assertTrue(first.isEmpty())
        assertEquals("SUCCESS", second.single().getString("status"))
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun setRemarkResponse_beforeInitialize_throws() {
        AppRemarkService::class.java.getDeclaredField("remarkListener").apply {
            isAccessible = true
            set(null, null)
        }

        AppRemarkService.setRemarkResponse(JSONObject())
    }

    @Test
    fun setAdditionalMetaData_isExposedAsExtraPayload() {
        val payload = mapOf("userId" to "u-1", "plan" to "pro", "seats" to 3)

        AppRemarkService.setAdditionalMetaData(payload)

        assertEquals(payload, AppRemarkService.Properties.getExtraPayload())
    }

    @Test
    fun setAdditionalMetaData_replacesPreviousPayload() {
        AppRemarkService.setAdditionalMetaData(mapOf("a" to 1))
        AppRemarkService.setAdditionalMetaData(mapOf("b" to 2))

        assertEquals(mapOf("b" to 2), AppRemarkService.Properties.getExtraPayload())
    }

    @Test
    fun addRemark_opensRemarkScreenAsNewTask() {
        AppRemarkService.addRemark(app)

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(RemarkActivity::class.java.name, intent.component?.className)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
    }
}
