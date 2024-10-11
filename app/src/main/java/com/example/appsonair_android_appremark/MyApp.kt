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
                AppRemarkService.initialize(
                    activity,
                    options = mutableMapOf("pageBackgroundColor" to "#FFFFC5")
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

