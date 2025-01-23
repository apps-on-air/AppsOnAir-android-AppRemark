package com.example.appsonair_android_appremark

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.appsonair.appremark.services.AppRemarkService

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {

                val options = mutableMapOf(
                    "pageBackGroundColor" to "#00FF00",
                    "appBarBackGroundColor" to "#FFFFFF",
                    "descriptionlabeltext" to "Description label text",
                    "appbartitlecolor" to "#FF0000",
                    "remarktypelabelText" to "remarktypelabeltext",
                    "descriptionhinttext" to "descriptionhinttext",
                    "descriptionMaxLength" to "100",
                    "buttontext" to "Submit Button",
                    "buttontextcolor" to "#000000",
                    "labelcolor" to "#008080",
                    "buttonbackgroundcolor" to "#FFFFC5",
                    "inputtextcolor" to "#121D45",
                    "hintcolor" to "#FFC0CB",
                    "APPBARTITLETEXT" to "Title Text"
                )

                //with default theme
                //AppRemarkService.initialize(activity)
                //with customize theme
                //shakeGestureEnable is set to true by default, allowing the device to capture your current screen when it shakes. If it is false, the device shake's auto-capture screen will be disabled.
                AppRemarkService.initialize(
                    activity,
                    shakeGestureEnable = true,
                    options = options
                )
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }
}

