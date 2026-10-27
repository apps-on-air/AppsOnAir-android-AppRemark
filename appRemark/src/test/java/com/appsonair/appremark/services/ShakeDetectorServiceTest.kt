package com.appsonair.appremark.services

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Handler
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.test.core.app.ApplicationProvider
import com.appsonair.appremark.R
import com.appsonair.appremark.activities.EditImageActivity
import com.appsonair.appremark.activities.RemarkActivity
import com.appsonair.appremark.enums.ShakeSensitivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSensor
import org.robolectric.shadows.ShadowSensorManager
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ShakeDetectorServiceTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var app: Application
    private lateinit var sensorManager: SensorManager
    private lateinit var activity: Activity
    private lateinit var window: Window
    private lateinit var rootView: View
    private val startedIntents = mutableListOf<Intent>()

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shadowOf(sensorManager).addSensor(ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER))
        ShakeDetectorService.isProcessing = false

        rootView = mockk(relaxed = true)
        every { rootView.drawingCache } returns Bitmap.createBitmap(40, 80, Bitmap.Config.ARGB_8888)
        every { rootView.width } returns 40
        every { rootView.height } returns 80
        window = mockk(relaxed = true)
        every { window.decorView.rootView } returns rootView

        activity = mockk(relaxed = true)
        every { activity.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager
        every { activity.window } returns window
        every { activity.cacheDir } returns tmp.root
        every { activity.getString(R.string.app_name) } returns "App name"
        every { activity.startActivity(any()) } answers { startedIntents += firstArg<Intent>() }
    }

    @After
    fun tearDown() {
        unmockkAll()
        ShakeDetectorService.isProcessing = false
    }

    private fun stubPixelCopy(result: Int) {
        mockkStatic(PixelCopy::class)
        val listener = slot<PixelCopy.OnPixelCopyFinishedListener>()
        every {
            PixelCopy.request(any<Window>(), any<Rect>(), any<Bitmap>(), capture(listener), any<Handler>())
        } answers { listener.captured.onPixelCopyFinished(result) }
    }

    private fun send(x: Float, timeMs: Long, y: Float = 0f, z: Float = 0f) {
        val event = ShadowSensorManager.createSensorEvent(3, Sensor.TYPE_ACCELEROMETER)
        event.values[0] = x
        event.values[1] = y
        event.values[2] = z
        event.timestamp = timeMs * 1_000_000
        shadowOf(sensorManager).sendSensorEventToListeners(event)
    }

    /** A jolt that crosses MEDIUM followed by continued movement once the confirm window opens. */
    private fun shake() {
        send(30f, 1000)
        send(20f, 1400)
    }

    // registration

    @Test
    fun shakeDetect_registersSingleAccelerometerListener() {
        ShakeDetectorService.shakeDetect(app)

        assertEquals(1, shadowOf(sensorManager).listeners.size)
    }

    @Test
    fun shakeDetect_calledAgain_replacesPreviousListener() {
        ShakeDetectorService.shakeDetect(app, 20f)
        val first = shadowOf(sensorManager).listeners.single()

        ShakeDetectorService.shakeDetect(app, 12f)

        val listeners = shadowOf(sensorManager).listeners
        assertEquals(1, listeners.size)
        assertFalse(listeners.contains(first))
    }

    @Test(expected = NullPointerException::class)
    fun shakeDetect_withoutSensorService_throws() {
        val context = mockk<Context>()
        every { context.getSystemService(Context.SENSOR_SERVICE) } returns null

        ShakeDetectorService.shakeDetect(context, 20f)
    }

    @Test
    fun accuracyChanges_areIgnored() {
        ShakeDetectorService.shakeDetect(activity)

        shadowOf(sensorManager).listeners.single()
            .onAccuracyChanged(ShadowSensor.newInstance(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_STATUS_ACCURACY_LOW)

        assertFalse(ShakeDetectorService.isProcessing)
    }

    // shake recognition

    @Test
    fun shake_capturesScreenAndOpensEditor() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        shake()

        assertTrue(ShakeDetectorService.isProcessing)
        verify { rootView.isDrawingCacheEnabled = true }
        verify { rootView.isDrawingCacheEnabled = false }
        val intent = startedIntents.single()
        assertEquals(EditImageActivity::class.java.name, intent.component?.className)
        assertEquals(Intent.ACTION_EDIT, intent.action)
        val uri = intent.getParcelableExtra<Uri>("IMAGE_PATH")!!
        assertEquals("file", uri.scheme)
        assertTrue(File(uri.path!!).exists())
        assertTrue(File(uri.path!!).name.matches(Regex("App name_\\d{8}_\\d{6}\\.jpg")))
    }

    @Test
    fun shake_editorIntentHasExpectedFlags() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        shake()

        val flags = startedIntents.single().flags
        for (flag in listOf(
            Intent.FLAG_ACTIVITY_SINGLE_TOP,
            Intent.FLAG_ACTIVITY_CLEAR_TOP,
            Intent.FLAG_ACTIVITY_NEW_TASK,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )) {
            assertTrue(flags and flag != 0)
        }
    }

    @Test
    fun shake_pixelCopyFailure_doesNotOpenEditor() {
        stubPixelCopy(PixelCopy.ERROR_SOURCE_INVALID)
        ShakeDetectorService.shakeDetect(activity)

        shake()

        assertTrue(startedIntents.isEmpty())
        // the flag stays set until the editor is destroyed, which never happens here
        assertTrue(ShakeDetectorService.isProcessing)
    }

    @Test
    fun shake_whileProcessing_isIgnored() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)
        ShakeDetectorService.isProcessing = true

        shake()

        verify(exactly = 0) { activity.window }
        assertTrue(startedIntents.isEmpty())
    }

    @Test
    fun secondShake_beforeEditorCloses_isIgnored() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        shake()
        send(30f, 3000)
        send(20f, 3400)

        assertEquals(1, startedIntents.size)
    }

    @Test
    fun shake_afterProcessingReset_isDetectedAgain() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        shake()
        ShakeDetectorService.isProcessing = false
        send(9.80665f, 2500) // settle
        send(40f, 3000)
        send(20f, 3400)

        assertEquals(2, startedIntents.size)
    }

    @Test
    fun captureFailure_resetsProcessingFlag() {
        every { activity.window } throws IllegalStateException("no window")
        ShakeDetectorService.shakeDetect(activity)

        shake()

        verify { activity.window }
        assertFalse(ShakeDetectorService.isProcessing)
    }

    @Test
    fun shake_withNonActivityContext_resetsProcessingFlag() {
        ShakeDetectorService.shakeDetect(app)

        shake()

        assertFalse(ShakeDetectorService.isProcessing)
        assertNull(shadowOf(app).nextStartedActivity)
    }

    @Test
    fun putDown_settlesBeforeConfirmWindowEnds_isIgnored() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        send(30f, 1000)          // jolt
        send(9.80665f, 1400)     // resting at 1g inside the confirm window
        send(9.9f, 1700)         // still resting
        send(20f, 1900)          // movement after the window closes only resets the jolt

        assertTrue(startedIntents.isEmpty())
        assertFalse(ShakeDetectorService.isProcessing)
    }

    @Test
    fun movementBeforeConfirmWindow_doesNotTriggerYet() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        send(30f, 1000)
        send(25f, 1200) // 200ms: too early to tell a shake from a drop
        assertTrue(startedIntents.isEmpty())

        send(20f, 1360)
        assertEquals(1, startedIntents.size)
    }

    @Test
    fun smallDeviationInsideWindow_waitsForLargerMovement() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        send(30f, 1000)
        send(12.5f, 1400) // within STILL_TOLERANCE (3 m/s²) of gravity
        assertTrue(startedIntents.isEmpty())

        send(14f, 1500)
        assertEquals(1, startedIntents.size)
    }

    @Test
    fun deviationBelowGravity_alsoCountsAsMovement() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        send(30f, 1000)
        send(2f, 1400) // free-fall-ish reading, |2 - 9.8| > 3

        assertEquals(1, startedIntents.size)
    }

    @Test
    fun accelerationOnAnyAxis_isConsidered() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        send(0f, 1000, y = 18f, z = 24f) // magnitude 30
        send(0f, 1400, z = 20f)

        assertEquals(1, startedIntents.size)
    }

    @Test
    fun jolt_isNotRearmedInsideConfirmWindow() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)

        send(30f, 1000)
        send(9.80665f, 1300)
        send(9.80665f, 1900) // window expired, jolt cleared
        send(9.80665f, 2300) // no new jolt, so nothing to confirm

        assertTrue(startedIntents.isEmpty())
    }

    // sensitivity

    private fun triggersAt(threshold: Float, joltMagnitude: Float): Boolean {
        startedIntents.clear()
        ShakeDetectorService.isProcessing = false
        ShakeDetectorService.shakeDetect(activity, threshold)
        send(joltMagnitude, 1000)
        send(20f, 1400)
        return startedIntents.isNotEmpty()
    }

    @Test
    fun sensitivity_moderateShake_triggersOnlySensitiveLevels() {
        stubPixelCopy(PixelCopy.SUCCESS)
        // first reading of 18 m/s² gives a smoothed delta of ~17.2
        assertTrue(triggersAt(ShakeSensitivity.VERY_HIGH.threshold, 18f))
        assertTrue(triggersAt(ShakeSensitivity.HIGH.threshold, 18f))
        assertFalse(triggersAt(ShakeSensitivity.MEDIUM.threshold, 18f))
        assertFalse(triggersAt(ShakeSensitivity.LOW.threshold, 18f))
        assertFalse(triggersAt(ShakeSensitivity.VERY_LOW.threshold, 18f))
    }

    @Test
    fun sensitivity_violentShake_triggersEveryLevel() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeSensitivity.values().forEach {
            assertTrue("$it", triggersAt(it.threshold, 40f))
        }
    }

    @Test
    fun sensitivity_defaultOverloadUsesMedium() {
        stubPixelCopy(PixelCopy.SUCCESS)
        ShakeDetectorService.shakeDetect(activity)
        send(18f, 1000)
        send(20f, 1400)
        assertTrue(startedIntents.isEmpty())

        assertTrue(triggersAt(ShakeSensitivity.MEDIUM.threshold, 25f))
    }

    @Test
    fun sensitivity_thresholdIsExclusive() {
        stubPixelCopy(PixelCopy.SUCCESS)
        // smoothed value = 9 + (x - g); pick x so it lands exactly on the threshold
        val exact = 20f - 9f + SensorManager.GRAVITY_EARTH
        assertFalse(triggersAt(20f, exact))
        assertTrue(triggersAt(20f, exact + 0.01f))
    }

    // navigation helpers

    @Test
    fun goToImageEditActivity_savesBitmapAndOpensEditor() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        ShakeDetectorService.goToImageEditActivity(app, bitmap)

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(EditImageActivity::class.java.name, intent.component?.className)
        val uri = intent.getParcelableExtra<Uri>("IMAGE_PATH")
        assertNotNull(uri)
        val file = File(uri!!.path!!)
        assertEquals(app.cacheDir.absolutePath, file.parentFile!!.absolutePath)
        assertTrue(file.length() > 0)
    }

    @Test
    fun goToImageEditActivity_unwritableCache_doesNothing() {
        val context = mockk<Context>(relaxed = true)
        every { context.cacheDir } returns File(tmp.root, "does/not/exist")
        every { context.getString(R.string.app_name) } returns "App"

        ShakeDetectorService.goToImageEditActivity(context, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun addRemarkManually_opensRemarkScreen() {
        ShakeDetectorService.addRemarkManually(app)

        val intent = shadowOf(app).nextStartedActivity
        assertEquals(RemarkActivity::class.java.name, intent.component?.className)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
        assertFalse(intent.hasExtra("IMAGE_PATH"))
    }
}
