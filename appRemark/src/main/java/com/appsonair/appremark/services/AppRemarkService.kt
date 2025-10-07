package com.appsonair.appremark.services

import android.graphics.Color
import com.appsonair.appremark.R
import com.appsonair.core.services.CoreService
import android.util.Log
import android.content.Context

class AppRemarkService {

    internal object Properties {
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
            Properties.pageBackgroundColor to PAGE_BACKGROUND_COLOR,
            Properties.appBarBackgroundColor to APP_BAR_BACKGROUND_COLOR,
            Properties.appBarTitleText to APP_BAR_TITLE_TEXT,
            Properties.appBarTitleColor to APP_BAR_TITLE_COLOR,
            Properties.remarkTypeLabelText to REMARK_TYPE_LABEL_TEXT,
            Properties.descriptionLabelText to DESCRIPTION_LABEL_TEXT,
            Properties.descriptionHintText to DESCRIPTION_HINT_TEXT,
            Properties.descriptionMaxLength to DESCRIPTION_MAX_LENGTH,
            Properties.buttonText to BUTTON_TEXT,
            Properties.buttonTextColor to BUTTON_TEXT_COLOR,
            Properties.buttonBackgroundColor to BUTTON_BACKGROUND_COLOR,
            Properties.labelColor to LABEL_COLOR,
            Properties.hintColor to HINT_COLOR,
            Properties.inputTextColor to INPUT_TEXT_COLOR
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
                mutableMap[Properties.pageBackgroundColor] =
                    if (Properties.pageBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[Properties.pageBackgroundColor].toString() else PAGE_BACKGROUND_COLOR
                mutableMap[Properties.appBarBackgroundColor] =
                    if (Properties.appBarBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[Properties.appBarBackgroundColor].toString() else APP_BAR_BACKGROUND_COLOR
                mutableMap[Properties.appBarTitleText] = if (Properties.appBarTitleText.isValidString(mutableMap))
                    mutableMap[Properties.appBarTitleText].toString() else APP_BAR_TITLE_TEXT
                mutableMap[Properties.appBarTitleColor] = if (Properties.appBarTitleColor.isValidStringColor(mutableMap))
                    mutableMap[Properties.appBarTitleColor].toString() else APP_BAR_TITLE_COLOR
                mutableMap[Properties.remarkTypeLabelText] = if (Properties.remarkTypeLabelText.isValidString(mutableMap))
                    mutableMap[Properties.remarkTypeLabelText].toString() else REMARK_TYPE_LABEL_TEXT
                mutableMap[Properties.descriptionLabelText] =
                    if (Properties.descriptionLabelText.isValidString(mutableMap))
                        mutableMap[Properties.descriptionLabelText].toString() else DESCRIPTION_LABEL_TEXT
                mutableMap[Properties.descriptionHintText] = if (Properties.descriptionHintText.isValidString(mutableMap))
                    mutableMap[Properties.descriptionHintText].toString() else DESCRIPTION_HINT_TEXT
                mutableMap[Properties.descriptionMaxLength] = if (Properties.descriptionMaxLength.isValidInt(mutableMap))
                    mutableMap[Properties.descriptionMaxLength].toString().toInt() else DESCRIPTION_MAX_LENGTH
                mutableMap[Properties.buttonText] = if (Properties.buttonText.isValidString(mutableMap))
                    mutableMap[Properties.buttonText].toString() else BUTTON_TEXT
                mutableMap[Properties.buttonTextColor] = if (Properties.buttonTextColor.isValidStringColor(mutableMap))
                    mutableMap[Properties.buttonTextColor].toString() else BUTTON_TEXT_COLOR
                mutableMap[Properties.buttonBackgroundColor] =
                    if (Properties.buttonBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[Properties.buttonBackgroundColor].toString() else BUTTON_BACKGROUND_COLOR
                mutableMap[Properties.labelColor] = if (Properties.labelColor.isValidStringColor(mutableMap))
                    mutableMap[Properties.labelColor].toString() else LABEL_COLOR
                mutableMap[Properties.hintColor] = if (Properties.hintColor.isValidStringColor(mutableMap))
                    mutableMap[Properties.hintColor].toString() else HINT_COLOR
                mutableMap[Properties.inputTextColor] = if (Properties.inputTextColor.isValidStringColor(mutableMap))
                    mutableMap[Properties.inputTextColor].toString() else INPUT_TEXT_COLOR
                Companion.options = mutableMap.mapKeys {it.key.lowercase()}.toMutableMap()
                Companion.shakeGestureEnable = shakeGestureEnable
                if (Companion.shakeGestureEnable) {
                    ShakeDetectorService.shakeDetect(context)
                }
            }
        }

        @JvmStatic
        fun setAdditionalMetaData(
          extraPayload: Map<String, Any>
        ) {
             Companion.extraPayload = extraPayload
        }

        @JvmStatic
        fun addRemark(
            context: Context
        ) {
            ShakeDetectorService.addRemarkManually(context)
        }
    }
}
