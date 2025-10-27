package com.example.appsonair_android_appremark

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.appsonair.appremark.interfaces.RemarkResponse
import com.appsonair.appremark.services.AppRemarkService
import org.json.JSONObject

class MyApp : Application() {
    private var metaDataSet = false
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

                 val options = mutableMapOf(
                     "pageBackGroundColor" to "#007AFF",
                     "appBarBackGroundColor" to "#1c1c9e",
                     "descriptionLabelText" to "Add description here",
                     "appBarTitleColor" to "#FFFFFF",
                     "remarkTypeLabelText" to "Add Remark here",
                     "descriptionHintText" to "Add description here..",
                     "descriptionMaxLength" to "120",
                     "buttonText" to "Submit Remark",
                     "buttonTextColor" to "#000000",
                     "labelColor" to "#FFFFFF",
                     "buttonBackgroundColor" to "#FFFFFF",
                     "inputTextColor" to "#000000",
                     "hintColor" to "#000000",
                     "appBarTitleText" to "Feedback Screen"
                 )

                 //with default theme
                 //AppRemarkService.initialize(activity)
                 //with customize theme
                 //shakeGestureEnable is set to true by default, allowing the device to capture your current screen when it shakes. If it is false, the device shake's auto-capture screen will be disabled.
                 AppRemarkService.initialize(
                     activity,
                     shakeGestureEnable = true,
                     options = options
                 ) { result ->
                     Log.d("AppRemark", result.toString())
                 }
                if (!metaDataSet){
                 AppRemarkService.setAdditionalMetaData( mapOf(
                     "userId" to "USER_ID",
                     "isShake" to true
                 ))
                 metaDataSet = true
             }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

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

