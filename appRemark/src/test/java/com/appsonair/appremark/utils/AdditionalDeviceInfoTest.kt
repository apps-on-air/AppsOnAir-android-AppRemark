package com.appsonair.appremark.utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.InstallSourceInfo
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AdditionalDeviceInfoTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // locale

    @Test
    @Config(qualifiers = "en-rUS")
    fun locale_combinesLanguageAndRegion() {
        assertEquals("en_US", AdditionalDeviceInfo.getLocale(app))
    }

    @Test
    @Config(qualifiers = "fr-rCA")
    fun locale_usesPrimaryLocale() {
        assertEquals("fr_CA", AdditionalDeviceInfo.getLocale(app))
    }

    @Test
    @Config(qualifiers = "de")
    fun locale_withoutRegion_hasEmptyRegionPart() {
        assertEquals("de_", AdditionalDeviceInfo.getLocale(app))
    }

    // theme mode

    @Test
    @Config(qualifiers = "notnight")
    fun themeMode_light() {
        assertEquals("light", AdditionalDeviceInfo.getThemeMode(app))
    }

    @Test
    @Config(qualifiers = "night")
    fun themeMode_dark() {
        assertEquals("dark", AdditionalDeviceInfo.getThemeMode(app))
    }

    @Test
    fun themeMode_undefined() {
        val configuration = android.content.res.Configuration()
        configuration.uiMode = android.content.res.Configuration.UI_MODE_TYPE_NORMAL or
                android.content.res.Configuration.UI_MODE_NIGHT_UNDEFINED
        val context = mockk<Context>()
        every { context.resources.configuration } returns configuration

        assertEquals("undefined", AdditionalDeviceInfo.getThemeMode(context))
    }

    // font scale

    @Test
    fun fontScale_default() {
        assertEquals(1.0f, AdditionalDeviceInfo.getFontScale(app))
    }

    @Test
    @Config(fontScale = 1.3f)
    fun fontScale_reflectsUserSetting() {
        assertEquals(1.3f, AdditionalDeviceInfo.getFontScale(app))
    }

    // install vendor

    private fun contextWithInstaller(name: String?): Context {
        val sourceInfo = mockk<InstallSourceInfo>()
        every { sourceInfo.installingPackageName } returns name
        val packageManager = mockk<PackageManager>()
        every { packageManager.getInstallSourceInfo("com.example") } returns sourceInfo
        val context = mockk<Context>()
        every { context.packageName } returns "com.example"
        every { context.packageManager } returns packageManager
        return context
    }

    @Test
    fun installVendor_playStore() {
        assertEquals("Google Play Store", AdditionalDeviceInfo.getInstallVendor(contextWithInstaller("com.android.vending")))
    }

    @Test
    fun installVendor_amazon() {
        assertEquals("Amazon Appstore", AdditionalDeviceInfo.getInstallVendor(contextWithInstaller("com.amazon.venezia")))
    }

    @Test
    fun installVendor_otherInstaller_returnsPackageName() {
        assertEquals("com.sec.android.app.samsungapps", AdditionalDeviceInfo.getInstallVendor(contextWithInstaller("com.sec.android.app.samsungapps")))
    }

    @Test
    fun installVendor_sideloaded_isUnknown() {
        assertEquals("Unknown", AdditionalDeviceInfo.getInstallVendor(contextWithInstaller(null)))
    }

    @Test
    fun installVendor_packageManagerThrows_isUnknown() {
        val context = mockk<Context>()
        every { context.packageName } returns "com.example"
        every { context.packageManager } throws RuntimeException("dead")

        assertEquals("Unknown", AdditionalDeviceInfo.getInstallVendor(context))
    }

    @Test
    @Config(sdk = [29])
    fun installVendor_beforeR_usesLegacyApi() {
        app.packageManager.setInstallerPackageName(app.packageName, "com.android.vending")
        assertEquals("Google Play Store", AdditionalDeviceInfo.getInstallVendor(app))
    }

    // unique identifier

    @Test
    fun uniqueIdentifier_readsAndroidId() {
        Settings.Secure.putString(app.contentResolver, Settings.Secure.ANDROID_ID, "abc123def")
        assertEquals("abc123def", AdditionalDeviceInfo.getUniqueIdentifier(app))
    }

    // biometrics

    private fun biometricResult(result: Int) {
        val manager = mockk<BiometricManager>()
        every { manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) } returns result
        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(any()) } returns manager
    }

    @Test
    fun biometric_success_isSupportedAndEnrolled() {
        biometricResult(BiometricManager.BIOMETRIC_SUCCESS)
        assertEquals(
            mapOf("isBiometricSupported" to true, "isBiometricEnrolled" to true),
            AdditionalDeviceInfo.getBiometricStatus(app)
        )
    }

    @Test
    fun biometric_noneEnrolled_isSupportedOnly() {
        biometricResult(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        assertEquals(
            mapOf("isBiometricSupported" to true, "isBiometricEnrolled" to false),
            AdditionalDeviceInfo.getBiometricStatus(app)
        )
    }

    @Test
    fun biometric_noHardware_isUnsupported() {
        for (code in listOf(
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED
        )) {
            biometricResult(code)
            assertEquals(
                mapOf("isBiometricSupported" to false, "isBiometricEnrolled" to false),
                AdditionalDeviceInfo.getBiometricStatus(app)
            )
        }
    }

    @Test
    @Config(sdk = [29])
    fun biometric_beforeR_alwaysReportsUnsupported() {
        assertEquals(
            mapOf("isBiometricSupported" to false, "isBiometricEnrolled" to false),
            AdditionalDeviceInfo.getBiometricStatus(app)
        )
    }

    // permissions

    private fun declaredPermissions(): List<String> = app.packageManager
        .getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
        .requestedPermissions!!.toList()

    @Test
    fun permissions_reportsStatusForEveryDeclaredPermission() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        shadowOf(app).denyPermissions(*declaredPermissions().toTypedArray())
        shadowOf(app).grantPermissions(Manifest.permission.INTERNET)
        shadowOf(app.packageManager).setShouldShowRequestPermissionRationale(
            Manifest.permission.READ_EXTERNAL_STORAGE, true
        )

        val result = AdditionalDeviceInfo.getPermissionsStatusList(activity)
        val statuses = result.associate { it.entries.single().toPair() }

        assertEquals(result.size, statuses.size)
        assertEquals("granted", statuses[Manifest.permission.INTERNET])
        assertEquals("denied", statuses[Manifest.permission.READ_EXTERNAL_STORAGE])
        assertEquals("not_determined", statuses[Manifest.permission.POST_NOTIFICATIONS])
    }

    @Test
    fun permissions_eachEntryIsSingleKeyMap() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

        val result = AdditionalDeviceInfo.getPermissionsStatusList(activity)

        assertTrue(result.isNotEmpty())
        result.forEach { assertEquals(1, it.size) }
    }

    @Test
    fun permissions_nonActivityContext_stopsAtFirstUngrantedPermission() {
        // the rationale check casts to ComponentActivity; the ClassCastException is swallowed
        val declared = declaredPermissions()
        shadowOf(app).denyPermissions(*declared.toTypedArray())
        shadowOf(app).grantPermissions(declared.first())

        val result = AdditionalDeviceInfo.getPermissionsStatusList(app)

        assertEquals(listOf(mapOf(declared.first() to "granted")), result)
    }

    @Test
    fun permissions_packageLookupFails_returnsEmptyList() {
        val context = mockk<Context>()
        every { context.packageName } returns "com.example"
        every { context.packageManager.getPackageInfo("com.example", PackageManager.GET_PERMISSIONS) } throws
                PackageManager.NameNotFoundException()

        assertEquals(emptyList<Map<String, String>>(), AdditionalDeviceInfo.getPermissionsStatusList(context))
    }
}
