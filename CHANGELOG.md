## 1.2.0

* Introduced `shakeSensitivity` param in `initialize` method to set how hard the device must be shaken (`VERY_LOW`, `LOW`, `MEDIUM`, `HIGH`, `VERY_HIGH`). Default is `MEDIUM`, same as previous versions.
* Fixed the remark screen opening by mistake. A shake now only counts if the device keeps moving after the first jolt.

## 1.1.4

* Fixes and minor improvement

## 1.1.3

* Introduced `onRemarkResponse` feedback listener in `initialize` method to process success and failure responses.

## 1.1.2

* Fix UI overlay issue for react-native-sdk

## 1.1.1

* Dependency upgrade

## 1.1.0

* Dependency upgrade

## 0.0.8

* Dependency upgrade
* Minor improvement

## 0.0.7
**Breaking Changes:**
* Dependency upgrade
* Added additional device information (theme, permissions, locale, etc.).
* Remove `extraPayload` params in `addRemark()` methods and create separate `setAdditionalMetaData()` method for adding/updating the additional details.


## 0.0.6

* Remove gson dependency for the pro-guard fix.

## 0.0.5

* Remove device info from module and used from core library and optimized used of options param.

## 0.0.4

* Logger added

## 0.0.3

* Downgraded Kotlin version in order to to support older projects

## 0.0.2

* Bitmap draw on surface support for app-remark flutter SDK

## 0.0.1

* Initial Release
