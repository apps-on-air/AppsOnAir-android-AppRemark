package com.appsonair.appremark.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

internal class AdditionalDeviceInfo {

     companion object {

         fun getThemeMode(context: Context): String {
            val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> "light"
                Configuration.UI_MODE_NIGHT_YES -> "dark"
                else -> "undefined"
            }
        }

        fun getFontScale(context: Context): Float {
            return context.resources.configuration.fontScale
        }

        fun getInstallVendor(context: Context): String {
            return try {
                val pm = context.packageManager
                @Suppress("DEPRECATION") val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    pm.getInstallSourceInfo(context.packageName).installingPackageName
                } else {
                    pm.getInstallerPackageName(context.packageName)
                }

                when (installer) {
                    "com.android.vending" -> "Google Play Store"
                    "com.amazon.venezia" -> "Amazon Appstore"
                    null -> "Unknown"
                    else -> installer
                }
            } catch (e: Exception) {
                "Unknown"
            }
        }

        @SuppressLint("HardwareIds")
        fun getUniqueIdentifier(context: Context): String {
            return Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }

        fun getBiometricStatus(context: Context): Map<String, Boolean> {
            val result = mutableMapOf<String, Boolean>()

            var isBiometricSupported = false
            var isBiometricEnrolled = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val biometricManager = BiometricManager.from(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                        BiometricManager.BIOMETRIC_SUCCESS -> {
                            isBiometricSupported = true
                            isBiometricEnrolled = true
                        }

                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                            isBiometricSupported = true
                        }

                        else -> {
                            // No-op
                        }
                    }
                }
            }

            result["isBiometricSupported"] = isBiometricSupported
            result["isBiometricEnrolled"] = isBiometricEnrolled

            return result
        }

        fun getPermissionsStatusList(context: Context): List<Map<String, String>> {
            val resultList = mutableListOf<Map<String, String>>()

            try {
                val packageManager = context.packageManager
                val packageInfo: PackageInfo = packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_PERMISSIONS
                )

                val declaredPermissions = packageInfo.requestedPermissions ?: emptyArray()

                for (permission in declaredPermissions) {
                    val status = when {
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                            "granted"
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(context as ComponentActivity, permission) -> {
                            "denied"
                        }

                        else -> {
                            "not_determined"
                        }
                    }

                    val permissionMap = mapOf(permission to status)
                    resultList.add(permissionMap)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return resultList
        }
    }
}
