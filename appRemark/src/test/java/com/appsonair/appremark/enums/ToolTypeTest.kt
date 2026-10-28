package com.appsonair.appremark.enums

import org.junit.Assert.assertEquals
import org.junit.Test

class ToolTypeTest {

    @Test
    fun toolType_containsAllEditorTools() {
        assertEquals(
            listOf("SHAPE", "TEXT", "ERASER", "UNDO", "REDO", "EMOJI", "GALLERY"),
            ToolType.values().map { it.name }
        )
    }

    @Test
    fun appRemarkStatus_namesAreSentToListener() {
        // RemarkActivity reports status.name() to the host app, so these strings are public contract
        assertEquals(listOf("SUCCESS", "FAILURE"), AppRemarkStatus.values().map { it.name })
    }
}
