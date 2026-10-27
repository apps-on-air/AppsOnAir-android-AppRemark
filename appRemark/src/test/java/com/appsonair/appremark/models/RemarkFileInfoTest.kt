package com.appsonair.appremark.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemarkFileInfoTest {

    @Test
    fun builder_setsAllFields() {
        val info = RemarkFileInfo.Builder()
            .setKey("https://cdn.example.com/remarks/1.jpg")
            .setFileType("image/jpeg")
            .setFileSize(0.42)
            .build()

        assertEquals("https://cdn.example.com/remarks/1.jpg", info.key)
        assertEquals("image/jpeg", info.fileType)
        assertEquals(0.42, info.fileSize, 0.0)
    }

    @Test
    fun builder_withoutValues_usesDefaults() {
        val info = RemarkFileInfo.Builder().build()

        assertNull(info.key)
        assertNull(info.fileType)
        assertEquals(0.0, info.fileSize, 0.0)
    }

    @Test
    fun builtInstance_isUnaffectedByLaterBuilderChanges() {
        val builder = RemarkFileInfo.Builder().setKey("a")
        val info = builder.build()
        builder.setKey("b")

        assertEquals("a", info.key)
    }
}
