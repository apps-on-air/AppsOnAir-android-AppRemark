package com.appsonair.appremark.enums

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeSensitivityTest {

    @Test
    fun thresholds_matchDocumentedValues() {
        assertEquals(32f, ShakeSensitivity.VERY_LOW.threshold)
        assertEquals(26f, ShakeSensitivity.LOW.threshold)
        assertEquals(20f, ShakeSensitivity.MEDIUM.threshold)
        assertEquals(16f, ShakeSensitivity.HIGH.threshold)
        assertEquals(12f, ShakeSensitivity.VERY_HIGH.threshold)
    }

    @Test
    fun higherSensitivity_meansLowerThreshold() {
        val thresholds = ShakeSensitivity.values().map { it.threshold }
        for (i in 1 until thresholds.size) {
            assertTrue(
                "${ShakeSensitivity.values()[i]} should be easier to trigger than ${ShakeSensitivity.values()[i - 1]}",
                thresholds[i] < thresholds[i - 1]
            )
        }
    }

    @Test
    fun valueOf_resolvesEveryLevel() {
        assertEquals(5, ShakeSensitivity.values().size)
        ShakeSensitivity.values().forEach { assertEquals(it, ShakeSensitivity.valueOf(it.name)) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun valueOf_unknownName_throws() {
        ShakeSensitivity.valueOf("EXTREME")
    }
}
