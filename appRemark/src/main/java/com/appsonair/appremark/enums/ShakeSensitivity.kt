package com.appsonair.appremark.enums

// threshold is the smoothed acceleration change (m/s²) a shake must exceed; lower means easier to trigger
enum class ShakeSensitivity(@JvmField internal val threshold: Float) {
    VERY_LOW(32f), LOW(26f), MEDIUM(20f), HIGH(16f), VERY_HIGH(12f)
}
