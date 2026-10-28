package com.appsonair.appremark.activities

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.text.InputFilter
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.appsonair.appremark.BuildConfig
import com.appsonair.appremark.R
import com.appsonair.appremark.services.AppRemarkService
import com.appsonair.appremark.testutil.TestUtils
import com.appsonair.appremark.testutil.ViewTestUtils
import com.appsonair.core.services.CoreService
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.skydoves.powerspinner.PowerSpinnerView
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class RemarkActivityTest {

    private lateinit var app: Application
    private lateinit var server: MockWebServer
    private val routes = ConcurrentHashMap<String, () -> MockResponse>()
    private val responses = CopyOnWriteArrayList<JSONObject>()
    private val paths = CopyOnWriteArrayList<String>()
    @Volatile private var lastFeedbackBody: String? = null
    private val deviceInfoExtras = slot<Map<String, Any>>()

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl!!.encodedPath
                paths += path
                if (path == "/feedback") lastFeedbackBody = request.body.clone().readUtf8()
                return routes[path]?.invoke() ?: MockResponse().setResponseCode(404)
            }
        }
        server.start()

        // The SDK builds its own OkHttpClient against BuildConfig.BASE_URL; route every request to MockWebServer
        // while keeping the original path. The client must be built before the constructor is mocked.
        val testClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val url = original.url.newBuilder()
                    .scheme("http").host(server.hostName).port(server.port)
                    .build()
                chain.proceed(original.newBuilder().url(url).build())
            }
            .build()
        mockkConstructor(OkHttpClient.Builder::class)
        every { anyConstructed<OkHttpClient.Builder>().build() } returns testClient

        mockkObject(CoreService.Companion)
        every { CoreService.Companion.getDeviceInfo(any(), capture(deviceInfoExtras)) } answers {
            JSONObject(
                """
                {
                  "appInfo": {"appName": "Sample", "bundleIdentifier": "com.example.sample"},
                  "deviceInfo": {"deviceModel": "Pixel 8", "firstInstallTime": "2024-01-02 03:04:05"}
                }
                """
            )
        }

        TestUtils.setAppId(app, "test-app-id")
        Settings.Secure.putString(app.contentResolver, Settings.Secure.ANDROID_ID, "a1-b2_c3")
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypeMapping("png", "image/png")
        AppRemarkService.setAdditionalMetaData(emptyMap())
        AppRemarkService.initialize(app, shakeGestureEnable = false) { responses += it }
    }

    @After
    fun tearDown() {
        unmockkAll()
        server.shutdown()
    }

    // helpers

    private fun image(name: String = "shot.png", bytes: Int = 2048): Uri {
        val file = File(app.cacheDir, name)
        FileOutputStream(file).use {
            Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, it)
            it.write(ByteArray(bytes))
        }
        return Uri.fromFile(file)
    }

    private fun launch(imageUri: Uri? = null, network: Boolean = true): RemarkActivity {
        val intent = Intent(app, RemarkActivity::class.java)
        if (imageUri != null) intent.putExtra("IMAGE_PATH", imageUri)
        val activity = Robolectric.buildActivity(RemarkActivity::class.java, intent).setup().get()
        if (network) TestUtils.reportNetwork(activity, true)
        return activity
    }

    private fun json(code: Int, body: String) = MockResponse().setResponseCode(code).setBody(body)

    private fun route(path: String, response: () -> MockResponse) {
        routes[path] = response
    }

    private fun signedUrlOk() = route("/signed-url") {
        json(200, """{"data":{"fileUrl":"https://cdn.example.com/remarks/shot.png","signedURL":"${server.url("/upload")}"}}""")
    }

    private fun RemarkActivity.progress() = findViewById<ProgressBar>(R.id.progress_bar)
    private fun RemarkActivity.description() = findViewById<TextInputEditText>(R.id.et_description)
    private fun RemarkActivity.submit() = findViewById<Button>(R.id.btn_submit).performClick()
    private fun RemarkActivity.images() = findViewById<RecyclerView>(R.id.rv_image)
    private fun RemarkActivity.addButton() = findViewById<ImageView>(R.id.img_add)

    private fun awaitResponse(): JSONObject {
        TestUtils.awaitUntil { responses.isNotEmpty() }
        return responses.single()
    }

    private fun takeRequest(): RecordedRequest = server.takeRequest(5, TimeUnit.SECONDS)!!

    private fun RecordedRequest.json() = JSONObject(body.readUtf8())

    private fun pickFromGallery(activity: RemarkActivity, uri: Uri) {
        activity.addButton().performClick()
        val started = shadowOf(activity).nextStartedActivityForResult
        shadowOf(activity).receiveResult(started.intent, Activity.RESULT_OK, Intent().setData(uri))
    }

    // screen setup

    @Test
    fun defaultOptions_areAppliedToViews() {
        val activity = launch()

        assertEquals("Add Remark", activity.findViewById<TextView>(R.id.tv_appbar_title).text.toString())
        assertEquals("Remark Type", activity.findViewById<TextView>(R.id.tv_remark_type).text.toString())
        assertEquals("Description", activity.findViewById<TextView>(R.id.tv_description).text.toString())
        assertEquals("Submit", activity.findViewById<Button>(R.id.btn_submit).text.toString())
        assertEquals(Color.parseColor("#E8F1FF"), (activity.findViewById<LinearLayout>(R.id.ll_main).background as ColorDrawable).color)
        assertEquals(Color.parseColor("#FFFFFF"), activity.findViewById<Button>(R.id.btn_submit).currentTextColor)
        assertEquals(Color.parseColor("#007AFF"), activity.findViewById<Button>(R.id.btn_submit).backgroundTintList!!.defaultColor)
        assertEquals(View.GONE, activity.progress().visibility)
    }

    @Test
    fun customOptions_areAppliedToViews() {
        AppRemarkService.initialize(
            app,
            shakeGestureEnable = false,
            options = mapOf(
                "appBarTitleText" to "Report a problem",
                "appBarTitleColor" to "#112233",
                "appBarBackgroundColor" to "#445566",
                "pageBackgroundColor" to "#778899",
                "remarkTypeLabelText" to "Category",
                "descriptionLabelText" to "What happened?",
                "descriptionHintText" to "Steps to reproduce",
                "descriptionMaxLength" to 40,
                "buttonText" to "Send report",
                "buttonTextColor" to "#010203",
                "buttonBackgroundColor" to "#040506",
                "labelColor" to "#070809",
                "hintColor" to "#0A0B0C",
                "inputTextColor" to "#0D0E0F"
            )
        ) { responses += it }

        val activity = launch()

        val title = activity.findViewById<TextView>(R.id.tv_appbar_title)
        assertEquals("Report a problem", title.text.toString())
        assertEquals(Color.parseColor("#112233"), title.currentTextColor)
        assertEquals(Color.parseColor("#445566"), (activity.findViewById<LinearLayout>(R.id.ll_appbar).background as ColorDrawable).color)
        assertEquals(Color.parseColor("#778899"), (activity.findViewById<LinearLayout>(R.id.ll_main).background as ColorDrawable).color)
        assertEquals("Category", activity.findViewById<TextView>(R.id.tv_remark_type).text.toString())
        assertEquals(Color.parseColor("#070809"), activity.findViewById<TextView>(R.id.tv_remark_type).currentTextColor)
        assertEquals("What happened?", activity.findViewById<TextView>(R.id.tv_description).text.toString())
        assertEquals(Color.parseColor("#0D0E0F"), activity.description().currentTextColor)
        val til = activity.findViewById<TextInputLayout>(R.id.til_description)
        assertEquals("Steps to reproduce", til.placeholderText.toString())
        assertEquals(40, til.counterMaxLength)
        assertEquals(40, (activity.description().filters.single() as InputFilter.LengthFilter).max)
        val submit = activity.findViewById<Button>(R.id.btn_submit)
        assertEquals("Send report", submit.text.toString())
        assertEquals(Color.parseColor("#010203"), submit.currentTextColor)
        assertEquals(Color.parseColor("#040506"), submit.backgroundTintList!!.defaultColor)
    }

    @Test
    fun descriptionLength_isLimitedByOption() {
        AppRemarkService.initialize(app, shakeGestureEnable = false, options = mapOf("descriptionMaxLength" to 5)) {}
        val activity = launch()

        activity.description().setText("123456789")

        assertEquals("12345", activity.description().text.toString())
    }

    @Test
    fun screenshotExtra_isShownAsAttachment() {
        val activity = launch(image())
        ViewTestUtils.layout(activity.images())

        assertEquals(1, activity.images().adapter!!.itemCount)
        assertEquals(View.VISIBLE, activity.addButton().visibility)
    }

    @Test
    fun noExtra_startsWithoutAttachments() {
        val activity = launch()

        assertEquals(0, activity.images().adapter!!.itemCount)
    }

    @Test
    fun galleryPick_addsAttachmentAndHidesAddButtonAtTwo() {
        val activity = launch(image("first.png"))

        pickFromGallery(activity, image("second.png"))

        assertEquals(2, activity.images().adapter!!.itemCount)
        assertEquals(View.GONE, activity.addButton().visibility)
    }

    @Test
    fun galleryPick_launchesImagePicker() {
        val activity = launch()

        activity.addButton().performClick()

        val started = shadowOf(activity).nextStartedActivityForResult
        assertEquals(Intent.ACTION_PICK, started.intent.action)
        assertEquals(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, started.intent.data)
    }

    @Test
    fun galleryPick_cancelled_addsNothing() {
        val activity = launch()
        activity.addButton().performClick()
        val started = shadowOf(activity).nextStartedActivityForResult

        shadowOf(activity).receiveResult(started.intent, Activity.RESULT_CANCELED, null)

        assertEquals(0, activity.images().adapter!!.itemCount)
    }

    @Test
    fun galleryPick_withoutData_addsNothing() {
        val activity = launch()
        activity.addButton().performClick()
        val started = shadowOf(activity).nextStartedActivityForResult

        shadowOf(activity).receiveResult(started.intent, Activity.RESULT_OK, Intent())

        assertEquals(0, activity.images().adapter!!.itemCount)
    }

    @Test
    fun removingAttachment_showsAddButtonAgain() {
        val activity = launch(image("first.png"))
        pickFromGallery(activity, image("second.png"))
        ViewTestUtils.layout(activity.images(), width = 5000)

        activity.images().findViewHolderForAdapterPosition(0)!!.itemView
            .findViewById<View>(R.id.img_remove).performClick()

        assertEquals(1, activity.images().adapter!!.itemCount)
        assertEquals(View.VISIBLE, activity.addButton().visibility)
    }

    @Test
    fun closeButton_finishesScreen() {
        val activity = launch()

        activity.findViewById<View>(R.id.img_close).performClick()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun permissionResult_isHandledWithoutSideEffects() {
        val activity = launch()

        activity.onRequestPermissionsResult(100, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), intArrayOf(0))
        activity.onRequestPermissionsResult(100, emptyArray(), IntArray(0))
        activity.onRequestPermissionsResult(1, emptyArray(), IntArray(0))

        assertFalse(activity.isFinishing)
    }

    // submit preconditions

    @Test
    fun submit_withoutNetwork_showsToastAndSendsNothing() {
        val activity = launch(network = false)

        activity.submit()

        assertEquals("Please check your internet connection!", ShadowToast.getTextOfLatestToast())
        assertEquals(0, server.requestCount)
        assertEquals(View.GONE, activity.progress().visibility)
    }

    @Test
    fun submit_afterNetworkLost_showsToast() {
        val activity = launch()
        TestUtils.reportNetwork(activity, false)

        activity.submit()

        assertEquals("Please check your internet connection!", ShadowToast.getTextOfLatestToast())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun submit_withoutAppId_sendsNothing() {
        TestUtils.setAppId(app, null)
        val activity = launch()

        activity.submit()
        Thread.sleep(200)

        assertEquals(0, server.requestCount)
        assertEquals(View.GONE, activity.progress().visibility)
        assertTrue(responses.isEmpty())
    }

    @Test
    fun submit_whileRequestInFlight_isIgnored() {
        route("/feedback") { json(200, """{"message":"ok"}""").setBodyDelay(500, TimeUnit.MILLISECONDS) }
        val activity = launch()

        activity.submit()
        assertEquals(View.VISIBLE, activity.progress().visibility)
        activity.submit()
        awaitResponse()

        assertEquals(1, server.requestCount)
    }

    // remark without attachments

    @Test
    fun submit_postsRemarkPayload() {
        route("/feedback") { json(200, """{"message":"Thanks!"}""") }
        AppRemarkService.setAdditionalMetaData(mapOf("userId" to "u-42", "plan" to "pro"))
        val activity = launch()
        activity.description().setText("   Crash on login   ")

        activity.submit()
        awaitResponse()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/feedback", request.requestUrl!!.encodedPath)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("application/json"))
        val body = request.json()
        assertEquals("test-app-id", body.getJSONObject("where").getString("appId"))
        val data = body.getJSONObject("data")
        assertEquals("Crash on login", data.getString("description"))
        assertEquals("SUGGESTION", data.getString("type"))
        assertEquals("u-42", data.getJSONObject("additionalMetadata").getString("userId"))
        assertEquals("pro", data.getJSONObject("additionalMetadata").getString("plan"))
        assertFalse(data.has("attachments"))
    }

    @Test
    fun submit_mergesAppAndDeviceInfo() {
        route("/feedback") { json(200, """{"message":"ok"}""") }
        val activity = launch()

        activity.submit()
        awaitResponse()

        val deviceInfo = takeRequest().json().getJSONObject("data").getJSONObject("deviceInfo")
        // appInfo and deviceInfo from the core SDK are flattened into one object
        assertEquals("Sample", deviceInfo.getString("appName"))
        assertEquals("com.example.sample", deviceInfo.getString("bundleIdentifier"))
        assertEquals("Pixel 8", deviceInfo.getString("deviceModel"))
        // fields added by AppRemark
        assertEquals("light", deviceInfo.getString("themeMode"))
        assertTrue(deviceInfo.getString("locale").contains("_"))
        assertEquals(1.0, deviceInfo.getDouble("fontScale"), 0.0)
        assertEquals("Unknown", deviceInfo.getString("installVendor"))
        assertTrue(deviceInfo.has("biometricInfo"))
        assertTrue(deviceInfo.has("app_permissions"))
        // ANDROID_ID + firstInstallTime with everything but [a-zA-Z0-9] removed
        assertEquals("a1b2c320240102030405", deviceInfo.getString("externalReferenceId"))
        assertEquals(mapOf("appRemarkVersion" to BuildConfig.VERSION_NAME), deviceInfoExtras.captured)
    }

    @Test
    fun submit_withEmptyMetadata_sendsEmptyObject() {
        route("/feedback") { json(200, """{"message":"ok"}""") }
        val activity = launch()

        activity.submit()
        awaitResponse()

        assertEquals(0, takeRequest().json().getJSONObject("data").getJSONObject("additionalMetadata").length())
    }

    @Test
    fun submit_bugReportType_isMapped() {
        route("/feedback") { json(200, """{"message":"ok"}""") }
        val activity = launch()
        activity.findViewById<PowerSpinnerView>(R.id.sp_remark_type).selectItemByIndex(1)

        activity.submit()
        awaitResponse()

        assertEquals("BUG", takeRequest().json().getJSONObject("data").getString("type"))
    }

    @Test
    fun submit_emptyDescription_isAllowed() {
        route("/feedback") { json(200, """{"message":"ok"}""") }
        val activity = launch()

        activity.submit()
        awaitResponse()

        assertEquals("", takeRequest().json().getJSONObject("data").getString("description"))
    }

    @Test
    fun success_reportsServerMessageAndCloses() {
        route("/feedback") { json(200, """{"message":"Thanks!"}""") }
        val activity = launch()

        activity.submit()
        val response = awaitResponse()

        assertEquals("SUCCESS", response.getString("status"))
        assertEquals("Thanks!", response.getString("message"))
        assertEquals("Thanks!", ShadowToast.getTextOfLatestToast())
        assertEquals(View.GONE, activity.progress().visibility)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun success_withEmptyBody_usesDefaultMessage() {
        route("/feedback") { MockResponse().setResponseCode(200) }
        val activity = launch()

        activity.submit()
        val response = awaitResponse()

        assertEquals("SUCCESS", response.getString("status"))
        assertEquals("Remark added successfully!", response.getString("message"))
        assertTrue(activity.isFinishing)
    }

    @Test
    fun success_withEmptyMessage_usesDefaultMessage() {
        route("/feedback") { json(200, """{"message":""}""") }
        val activity = launch()

        activity.submit()

        assertEquals("Remark added successfully!", awaitResponse().getString("message"))
    }

    @Test
    fun serverError_reportsServerMessageAndStaysOpen() {
        route("/feedback") { json(422, """{"message":"Description too short"}""") }
        val activity = launch()

        activity.submit()
        val response = awaitResponse()

        assertEquals("FAILURE", response.getString("status"))
        assertEquals("Description too short", response.getString("message"))
        assertEquals("Something went wrong, please try later!", ShadowToast.getTextOfLatestToast())
        assertEquals(View.GONE, activity.progress().visibility)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun serverError_withoutMessage_reportsFailure() {
        route("/feedback") { json(500, "{}") }
        val activity = launch()

        activity.submit()

        assertEquals("FAILURE", awaitResponse().getString("status"))
        assertFalse(activity.isFinishing)
    }

    @Test
    fun serverError_withNonJsonBody_reportsGenericFailure() {
        route("/feedback") { json(502, "<html>Bad gateway</html>") }
        val activity = launch()

        activity.submit()
        val response = awaitResponse()

        assertEquals("FAILURE", response.getString("status"))
        assertEquals("Something went wrong, please try later!", response.getString("message"))
        TestUtils.awaitUntil { activity.progress().visibility == View.GONE }
    }

    @Test
    fun success_withMalformedJson_reportsGenericFailure() {
        route("/feedback") { json(200, "not json") }
        val activity = launch()

        activity.submit()
        val response = awaitResponse()

        assertEquals("FAILURE", response.getString("status"))
        assertEquals("Something went wrong, please try later!", response.getString("message"))
        assertFalse(activity.isFinishing)
    }

    @Test
    fun connectionFailure_reportsGenericFailure() {
        route("/feedback") { MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST) }
        val activity = launch()

        activity.submit()
        val response = awaitResponse()

        assertEquals("FAILURE", response.getString("status"))
        assertEquals("Something went wrong, please try later!", response.getString("message"))
        TestUtils.awaitUntil { activity.progress().visibility == View.GONE }
        assertFalse(activity.isFinishing)
    }

    @Test
    fun failure_thenRetry_canSucceed() {
        route("/feedback") { json(500, """{"message":"down"}""") }
        val activity = launch()
        activity.submit()
        awaitResponse()

        responses.clear()
        route("/feedback") { json(200, """{"message":"ok"}""") }
        activity.submit()

        assertEquals("SUCCESS", awaitResponse().getString("status"))
    }

    // remark with attachments

    @Test
    fun attachment_isUploadedThenLinkedInRemark() {
        signedUrlOk()
        route("/upload") { MockResponse().setResponseCode(200) }
        route("/feedback") { json(200, """{"message":"Got it"}""") }
        val uri = image(bytes = 4096)
        val activity = launch(uri)

        activity.submit()
        val response = awaitResponse()

        assertEquals("SUCCESS", response.getString("status"))
        assertEquals(3, server.requestCount)

        val signed = takeRequest()
        assertEquals("/signed-url", signed.requestUrl!!.encodedPath)
        val file = signed.json().getJSONObject("data")
        assertEquals("shot.png", file.getString("fileName"))
        assertEquals("image/png", file.getString("fileType"))
        assertEquals("test-app-id", file.getString("appId"))
        assertTrue(file.has("fileSize"))

        val upload = takeRequest()
        assertEquals("PUT", upload.method)
        assertEquals("/upload", upload.requestUrl!!.encodedPath)
        assertEquals("image/png", upload.getHeader("Content-Type"))
        assertEquals("public-read", upload.getHeader("x-amz-acl"))
        assertArrayEquals(File(uri.path!!).readBytes(), upload.body.readByteArray())

        val remark = takeRequest()
        assertEquals("/feedback", remark.requestUrl!!.encodedPath)
        val attachment = remark.json().getJSONObject("data").getJSONArray("attachments").getJSONObject(0)
        assertEquals("https://cdn.example.com/remarks/shot.png", attachment.getString("key"))
        assertEquals("image/png", attachment.getString("fileType"))
        assertTrue(attachment.has("size"))
    }

    @Test
    fun twoAttachments_areBothUploadedBeforeRemark() {
        signedUrlOk()
        // Upload callbacks share an unsynchronized list and both check "all uploaded?", so overlapping
        // responses can submit the remark twice. Stagger them to test the intended sequential flow.
        val uploads = AtomicInteger()
        route("/upload") {
            val delay = if (uploads.getAndIncrement() == 0) 0L else 400L
            MockResponse().setResponseCode(200).setHeadersDelay(delay, TimeUnit.MILLISECONDS)
        }
        route("/feedback") { json(200, """{"message":"ok"}""") }
        val activity = launch(image("first.png"))
        pickFromGallery(activity, image("second.png"))

        activity.submit()
        awaitResponse()

        Thread.sleep(300)
        assertEquals(1, responses.size)
        assertEquals(2, paths.count { it == "/signed-url" })
        assertEquals(2, paths.count { it == "/upload" })
        assertEquals(listOf("/feedback"), paths.filter { it == "/feedback" })
        assertEquals("/feedback", paths.last())
        val attachments = JSONObject(lastFeedbackBody!!).getJSONObject("data").getJSONArray("attachments")
        assertEquals(2, attachments.length())
    }

    @Test
    fun signedUrlError_reportsServerMessage() {
        route("/signed-url") { json(413, """{"message":"File too large"}""") }
        val activity = launch(image())

        activity.submit()
        val response = awaitResponse()

        assertEquals("FAILURE", response.getString("status"))
        assertEquals("File too large", response.getString("message"))
        TestUtils.awaitUntil { activity.progress().visibility == View.GONE }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun signedUrlError_withoutMessage_usesGenericMessage() {
        route("/signed-url") { json(500, "{}") }
        val activity = launch(image())

        activity.submit()

        assertEquals("Something went wrong, please try later!", awaitResponse().getString("message"))
    }

    @Test
    fun signedUrlMalformed_reportsGenericFailure() {
        route("/signed-url") { json(200, """{"data":{}}""") }
        val activity = launch(image())

        activity.submit()
        val response = awaitResponse()

        assertEquals("FAILURE", response.getString("status"))
        assertEquals("Something went wrong, please try later!", response.getString("message"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun signedUrlConnectionFailure_reportsGenericFailure() {
        route("/signed-url") { MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST) }
        val activity = launch(image())

        activity.submit()

        assertEquals("FAILURE", awaitResponse().getString("status"))
        TestUtils.awaitUntil { activity.progress().visibility == View.GONE }
    }

    @Test
    fun uploadConnectionFailure_reportsFailureAndSkipsRemark() {
        signedUrlOk()
        route("/upload") { MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST) }
        route("/feedback") { json(200, """{"message":"ok"}""") }
        val activity = launch(image())

        activity.submit()
        val response = awaitResponse()

        assertEquals("FAILURE", response.getString("status"))
        TestUtils.awaitUntil { activity.progress().visibility == View.GONE }
        Thread.sleep(200)
        assertFalse(paths.contains("/feedback"))
    }

    @Test
    fun attachmentFileMissing_stopsWithoutUploading() {
        signedUrlOk()
        val uri = image()
        val activity = launch(uri)
        File(uri.path!!).delete()

        activity.submit()
        TestUtils.awaitUntil { server.requestCount == 1 && activity.progress().visibility == View.GONE }

        assertEquals(1, server.requestCount)
        assertTrue(responses.isEmpty())
    }
}
