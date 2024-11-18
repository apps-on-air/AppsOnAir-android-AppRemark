package com.appsonair.appremark.services

import android.content.Context
import android.graphics.Color

class AppRemarkService {

    companion object {
        //keys
        private const val pageBackgroundColor = "pageBackgroundColor"
        private const val appbarBackgroundColor = "appbarBackgroundColor"
        private const val appbarTitleText = "appbarTitleText"
        private const val appbarTitleColor = "appbarTitleColor"
        private const val remarkTypeLabelText = "remarkTypeLabelText"
        private const val descriptionLabelText = "descriptionLabelText"
        private const val descriptionHintText = "descriptionHintText"
        private const val descriptionMaxLength = "descriptionMaxLength"
        private const val buttonText = "buttonText"
        private const val buttonTextColor = "buttonTextColor"
        private const val buttonBackgroundColor = "buttonBackgroundColor"
        private const val labelColor = "labelColor"
        private const val hintColor = "hintColor"
        private const val inputTextColor = "inputTextColor"

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
            pageBackgroundColor to PAGE_BACKGROUND_COLOR,
            appbarBackgroundColor to APP_BAR_BACKGROUND_COLOR,
            appbarTitleText to APP_BAR_TITLE_TEXT,
            appbarTitleColor to APP_BAR_TITLE_COLOR,
            remarkTypeLabelText to REMARK_TYPE_LABEL_TEXT,
            descriptionLabelText to DESCRIPTION_LABEL_TEXT,
            descriptionHintText to DESCRIPTION_HINT_TEXT,
            descriptionMaxLength to DESCRIPTION_MAX_LENGTH,
            buttonText to BUTTON_TEXT,
            buttonTextColor to BUTTON_TEXT_COLOR,
            buttonBackgroundColor to BUTTON_BACKGROUND_COLOR,
            labelColor to LABEL_COLOR,
            hintColor to HINT_COLOR,
            inputTextColor to INPUT_TEXT_COLOR
        )

        var shakeGestureEnable: Boolean = true
        var extraPayload: Map<String, Any> = emptyMap()
        var options: Map<String, Any> = OPTIONS.toMutableMap()

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
            val contextStr = context.toString()
            if (!contextStr.startsWith("com.appsonair.appremark")) {
                val mutableMap = options.toMutableMap()
                mutableMap[pageBackgroundColor] =
                    if (pageBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[pageBackgroundColor].toString() else PAGE_BACKGROUND_COLOR
                mutableMap[appbarBackgroundColor] =
                    if (appbarBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[appbarBackgroundColor].toString() else APP_BAR_BACKGROUND_COLOR
                mutableMap[appbarTitleText] = if (appbarTitleText.isValidString(mutableMap))
                    mutableMap[appbarTitleText].toString() else APP_BAR_TITLE_TEXT
                mutableMap[appbarTitleColor] = if (appbarTitleColor.isValidStringColor(mutableMap))
                    mutableMap[appbarTitleColor].toString() else APP_BAR_TITLE_COLOR
                mutableMap[remarkTypeLabelText] = if (remarkTypeLabelText.isValidString(mutableMap))
                    mutableMap[remarkTypeLabelText].toString() else REMARK_TYPE_LABEL_TEXT
                mutableMap[descriptionLabelText] =
                    if (descriptionLabelText.isValidString(mutableMap))
                        mutableMap[descriptionLabelText].toString() else DESCRIPTION_LABEL_TEXT
                mutableMap[descriptionHintText] = if (descriptionHintText.isValidString(mutableMap))
                    mutableMap[descriptionHintText].toString() else DESCRIPTION_HINT_TEXT
                mutableMap[descriptionMaxLength] = if (descriptionMaxLength.isValidInt(mutableMap))
                    mutableMap[descriptionMaxLength].toString().toInt() else DESCRIPTION_MAX_LENGTH
                mutableMap[buttonText] = if (buttonText.isValidString(mutableMap))
                    mutableMap[buttonText].toString() else BUTTON_TEXT
                mutableMap[buttonTextColor] = if (buttonTextColor.isValidStringColor(mutableMap))
                    mutableMap[buttonTextColor].toString() else BUTTON_TEXT_COLOR
                mutableMap[buttonBackgroundColor] =
                    if (buttonBackgroundColor.isValidStringColor(mutableMap))
                        mutableMap[buttonBackgroundColor].toString() else BUTTON_BACKGROUND_COLOR
                mutableMap[labelColor] = if (labelColor.isValidStringColor(mutableMap))
                    mutableMap[labelColor].toString() else LABEL_COLOR
                mutableMap[hintColor] = if (hintColor.isValidStringColor(mutableMap))
                    mutableMap[hintColor].toString() else HINT_COLOR
                mutableMap[inputTextColor] = if (inputTextColor.isValidStringColor(mutableMap))
                    mutableMap[inputTextColor].toString() else INPUT_TEXT_COLOR
                Companion.options = mutableMap
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
