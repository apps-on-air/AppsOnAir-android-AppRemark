package com.appsonair.appremark.services

import android.graphics.Color
import com.appsonair.appremark.R
import com.appsonair.core.services.CoreService
import android.util.Log
import android.content.Context

class AppRemarkService {

    internal object RemarkOptionsKey {
         const val pageBackgroundColor = "pagebackgroundcolor"
         const val appBarBackgroundColor = "appbarbackgroundcolor"
         const val appBarTitleText = "appbartitletext"
         const val appBarTitleColor = "appbartitlecolor"
         const val remarkTypeLabelText = "remarktypelabeltext"
         const val descriptionLabelText = "descriptionlabeltext"
         const val descriptionHintText = "descriptionhinttext"
         const val descriptionMaxLength = "descriptionmaxlength"
         const val buttonText = "buttontext"
         const val buttonTextColor = "buttontextcolor"
         const val buttonBackgroundColor = "buttonbackgroundcolor"
         const val labelColor = "labelcolor"
         const val hintColor = "hintcolor"
         const val inputTextColor = "inputtextcolor"

       fun getOptions(): Map<String, Any> {
            return options
        }

        fun getExtraPayload(): Map<String, Any> {
            return extraPayload
        }
    }


    companion object {
        private const val TAG: String = "RemarkActivity"
        //values
        private const val PAGE_BACKGROUND_COLOR = "#E8F1FF"
        private const val APP_BAR_BACKGROUND_COLOR = "#E8F1FF"
        private const val APP_BAR_TITLE_TEXT = "Add Remark"
        private const val APP_BAR_TITLE_COLOR = "#000000"
        private const val REMARK_TYPE_LABEL_TEXT = "Remark Type"
        private const val DESCRIPTION_LABEL_TEXT = "Description"
        private const val DESCRIPTION_HINT_TEXT = "Add description here…"
        private const val DESCRIPTION_MAX_LENGTH = 255
        private const val BUTTON_TEXT = "Submit"
        private const val BUTTON_TEXT_COLOR = "#FFFFFF"
        private const val BUTTON_BACKGROUND_COLOR = "#007AFF"
        private const val LABEL_COLOR = "#000000"
        private const val HINT_COLOR = "#B1B1B3"
        private const val INPUT_TEXT_COLOR = "#000000"

        private val OPTIONS: Map<String, Any> = mutableMapOf(
            RemarkOptionsKey.pageBackgroundColor to PAGE_BACKGROUND_COLOR,
            RemarkOptionsKey.appBarBackgroundColor to APP_BAR_BACKGROUND_COLOR,
            RemarkOptionsKey. appBarTitleText to APP_BAR_TITLE_TEXT,
            RemarkOptionsKey.appBarTitleColor to APP_BAR_TITLE_COLOR,
            RemarkOptionsKey.remarkTypeLabelText to REMARK_TYPE_LABEL_TEXT,
            RemarkOptionsKey.descriptionLabelText to DESCRIPTION_LABEL_TEXT,
            RemarkOptionsKey.descriptionHintText to DESCRIPTION_HINT_TEXT,
            RemarkOptionsKey.descriptionMaxLength to DESCRIPTION_MAX_LENGTH,
            RemarkOptionsKey.buttonText to BUTTON_TEXT,
            RemarkOptionsKey. buttonTextColor to BUTTON_TEXT_COLOR,
            RemarkOptionsKey. buttonBackgroundColor to BUTTON_BACKGROUND_COLOR,
            RemarkOptionsKey.labelColor to LABEL_COLOR,
            RemarkOptionsKey.hintColor to HINT_COLOR,
            RemarkOptionsKey.inputTextColor to INPUT_TEXT_COLOR
        )

        private var shakeGestureEnable: Boolean = true
        private var extraPayload: Map<String, Any> = emptyMap()
        private var options: Map<String, Any> = OPTIONS.mapKeys {it.key.lowercase()}.toMutableMap()

        private fun isValidColorHex(colorHex: String): Boolean {
            return try {
                Color.parseColor(colorHex)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        private fun String.isValidStringColor(mutableMap: Map<String, Any>): Boolean {
            return try {
                mutableMap.containsKey(this) &&
                        mutableMap[this].toString().isNotEmpty() &&
                        isValidColorHex(mutableMap[this].toString())
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        private fun String.isValidString(mutableMap: Map<String, Any>): Boolean {
            return try {
                mutableMap.containsKey(this) &&
                        mutableMap[this].toString().isNotEmpty()
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        private fun String.isValidInt(mutableMap: Map<String, Any>): Boolean {
            return try {
                mutableMap.containsKey(this) &&
                        mutableMap[this].toString().toInt() > 0
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        @JvmStatic
        @JvmOverloads
        fun initialize(
            context: Context,
            shakeGestureEnable: Boolean = true,
            options: Map<String, Any> = OPTIONS.toMutableMap(),
        ) {
            val appId = CoreService.getAppId(context)
            if (appId.isEmpty()) {
                Log.d(TAG, "AppId: ${context.getString(R.string.error_something_wrong)}")
            }
            val contextStr = context.toString()
            if (!contextStr.startsWith("com.appsonair.appremark")) {
              val mutableMap = options.mapKeys {it.key.lowercase()}.toMutableMap()
                mutableMap[RemarkOptionsKey.pageBackgroundColor] =
                    if (RemarkOptionsKey.pageBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[RemarkOptionsKey.pageBackgroundColor].toString() else PAGE_BACKGROUND_COLOR
                mutableMap[RemarkOptionsKey.appBarBackgroundColor] =
                    if (RemarkOptionsKey.appBarBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[RemarkOptionsKey.appBarBackgroundColor].toString() else APP_BAR_BACKGROUND_COLOR
                mutableMap[RemarkOptionsKey.appBarTitleText] = if (RemarkOptionsKey.appBarTitleText.isValidString(mutableMap))
                    mutableMap[RemarkOptionsKey.appBarTitleText].toString() else APP_BAR_TITLE_TEXT
                mutableMap[RemarkOptionsKey.appBarTitleColor] = if (RemarkOptionsKey.appBarTitleColor.isValidStringColor(mutableMap))
                    mutableMap[RemarkOptionsKey.appBarTitleColor].toString() else APP_BAR_TITLE_COLOR
                mutableMap[RemarkOptionsKey.remarkTypeLabelText] = if (RemarkOptionsKey.remarkTypeLabelText.isValidString(mutableMap))
                    mutableMap[RemarkOptionsKey.remarkTypeLabelText].toString() else REMARK_TYPE_LABEL_TEXT
                mutableMap[RemarkOptionsKey.descriptionLabelText] =
                    if (RemarkOptionsKey.descriptionLabelText.isValidString(mutableMap))
                        mutableMap[RemarkOptionsKey.descriptionLabelText].toString() else DESCRIPTION_LABEL_TEXT
                mutableMap[RemarkOptionsKey.descriptionHintText] = if (RemarkOptionsKey.descriptionHintText.isValidString(mutableMap))
                    mutableMap[RemarkOptionsKey.descriptionHintText].toString() else DESCRIPTION_HINT_TEXT
                mutableMap[RemarkOptionsKey.descriptionMaxLength] = if (RemarkOptionsKey.descriptionMaxLength.isValidInt(mutableMap))
                    mutableMap[RemarkOptionsKey.descriptionMaxLength].toString().toInt() else DESCRIPTION_MAX_LENGTH
                mutableMap[RemarkOptionsKey.buttonText] = if (RemarkOptionsKey.buttonText.isValidString(mutableMap))
                    mutableMap[RemarkOptionsKey.buttonText].toString() else BUTTON_TEXT
                mutableMap[RemarkOptionsKey.buttonTextColor] = if (RemarkOptionsKey.buttonTextColor.isValidStringColor(mutableMap))
                    mutableMap[RemarkOptionsKey.buttonTextColor].toString() else BUTTON_TEXT_COLOR
                mutableMap[RemarkOptionsKey.buttonBackgroundColor] =
                    if (RemarkOptionsKey.buttonBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[RemarkOptionsKey.buttonBackgroundColor].toString() else BUTTON_BACKGROUND_COLOR
                mutableMap[RemarkOptionsKey.labelColor] = if (RemarkOptionsKey.labelColor.isValidStringColor(mutableMap))
                    mutableMap[RemarkOptionsKey.labelColor].toString() else LABEL_COLOR
                mutableMap[RemarkOptionsKey.hintColor] = if (RemarkOptionsKey.hintColor.isValidStringColor(mutableMap))
                    mutableMap[RemarkOptionsKey.hintColor].toString() else HINT_COLOR
                mutableMap[RemarkOptionsKey.inputTextColor] = if (RemarkOptionsKey.inputTextColor.isValidStringColor(mutableMap))
                    mutableMap[RemarkOptionsKey.inputTextColor].toString() else INPUT_TEXT_COLOR
                Companion.options = mutableMap.mapKeys {it.key.lowercase()}.toMutableMap()
                Companion.shakeGestureEnable = shakeGestureEnable
                if (Companion.shakeGestureEnable) {
                    ShakeDetectorService.shakeDetect(context)
                }
            }
        }

        @JvmStatic
        @JvmOverloads
        fun addRemark(
            context: Context,
            extraPayload: Map<String, Any> = emptyMap(),
        ) {
            Companion.extraPayload = extraPayload
            ShakeDetectorService.addRemarkManually(context)
        }
    }
}
