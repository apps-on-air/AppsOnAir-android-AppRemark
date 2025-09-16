# AppsOnAir-android-AppRemark

## ⚠️ Important Notice ⚠️

The plugin is fully functional and integrates with our **AppRemark** service, which is currently in public beta. While you can start using it today, please note that there may be minor changes or improvements as we prepare for full production launch.

## How it works? 

This SDK is used to submit App Remark (Either it is bug/issue or any suggestion/feedback of your app).

AppsOnAir offers a service to monitor your problems or app recommendations/improvements by taking screenshots of your apps when you shake your device.

When you shake the device, it automatically takes screenshots of your apps. By modifying those app screenshots, users can draw attention to the specific problems or any app suggestions for enhancements.

Users have the option to turn off shakeGesture. They can also manually open the "Add Remark" screen.

#### To learn more about AppsOnAir AppRemark, please visit the [AppsOnAir](https://documentation.appsonair.com) website


## How to use?

Add meta-data to the app's AndroidManifest.xml file under the application tag.

>Make sure meta-data name is “AppsonairAppId”.

>Provide your application id in meta-data value.


```sh
</application>
    ...
    <meta-data
        android:name="AppsonairAppId"
        android:value="********-****-****-****-************" />
</application>
```


Add AppsOnAir AppRemark dependency to your gradle.

```sh
dependencies {
   implementation 'com.github.apps-on-air:AppsOnAir-android-AppRemark:TAG'
}
```

Add below code to setting.gradle.

```sh
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
       google()
       mavenCentral()
       maven {
           url = uri("https://jitpack.io")
       }
   }
}
```

## Example :

Add the following code on your application. It is necessary to add below code in the activity lifecycle as we need to capture the current Activity screen.

```sh
class MyApp : Application() {

   override fun onCreate() {
       super.onCreate()
       registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
           override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
           }

           override fun onActivityStarted(activity: Activity) {
           }

           override fun onActivityResumed(activity: Activity) {
               AppRemarkService.initialize(activity)
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
```

Follow this step to add App Remarks using shakeGesture with the default theme of "Add Remark" screen.

```sh
AppRemarkService.initialize(activity)
```

Follow this step to add App Remarks using shakeGesture with the custom theme of "Add Remark" screen.

Here users can customize the theme of "Add Remark" screen according to their app theme by passing "options" inform of Map, which contains key-value pair of user's theme data.

Users have to pass given keys into "options". Using "options", this SDK will set the theme of "Add Remark" screen.

>Make sure keys are same as below.

| Key                     | DataType | Value                       | Description                    |
|:------------------------| :------- | :-------------------------- | :----------------------------- |
| `pageBackgroundColor`   | `String` | `"#E8F1FF"`               | Set page background color      |
| `appBarBackgroundColor` | `String` | `"#E8F1FF"`               | Set appbar background color    |
| `appBarTitleText`       | `String` | `"Add Remark"`              | Set appbar title text          |
| `appBarTitleColor`      | `String` | `"#000000"`               | Set appbar title color         |
| `remarkTypeLabelText`   | `String` | `"Remark Type"`             | Set remark type label text     |
| `descriptionLabelText`  | `String` | `"Description"`             | Set description label text     |
| `descriptionHintText`   | `String` | `"Add description here..."` | Set description hint text      |
| `descriptionMaxLength`  | `int`    | `255`                       | Set description max length     |
| `buttonText`            | `String` | `"Submit"`                  | Set button text                |
| `buttonTextColor`       | `String` | `"#FFFFFF"`               | Set button text color          |
| `buttonBackgroundColor` | `String` | `"#007AFF"`               | Set button background color    |
| `labelColor`            | `String` | `"#000000"`               | Set textfield label color      |
| `hintColor`             | `String` | `"#B1B1B3"`               | Set textfield hint color       |
| `inputTextColor`        | `String` | `"#000000"`               | Set textfield input text color |


```sh
val options = mutableMapOf("pageBackgroundColor" to "#FFFFC5")

AppRemarkService.initialize(
    activity,
    options = options
)
```

"shakeGestureEnable" is set to true by default, allowing the device to capture your current screen when it shakes. If it is false, the device shake's auto-capture screen will be disabled.

```sh
AppRemarkService.initialize(
    activity,
    shakeGestureEnable = false
)
```

Follow this step to open AppRemark screen manually,

```sh
AppRemarkService.addRemark(this)
```

Follow this step to send your customize payload, which you want to save in order to monitor your app.

Users have to pass "extraPayload" inform of Map, which contains key-value pair of user's additional meta-data.

```sh
   AppRemarkService.setAdditionalMetaData( mapOf(
    "userId" to "********",
    "userEmail" to "test@gmail.com"))
```
Note: Call the setAdditionalMetaData method whenever you want to add or update details