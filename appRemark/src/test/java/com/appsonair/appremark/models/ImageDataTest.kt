package com.appsonair.appremark.models

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageDataTest {

    @Test
    fun builder_setsAllFields() {
        val uri = Uri.parse("file:///cache/screenshot.jpg")

        val data = ImageData.Builder()
            .setImageUri(uri)
            .setFileName("screenshot.jpg")
            .setFileType("image/jpeg")
            .setFileSize(1.25)
            .build()

        assertEquals(uri, data.imageUri)
        assertEquals("screenshot.jpg", data.fileName)
        assertEquals("image/jpeg", data.fileType)
        assertEquals(1.25, data.fileSize, 0.0)
    }

    @Test
    fun builder_withoutValues_usesDefaults() {
        val data = ImageData.Builder().build()

        assertNull(data.imageUri)
        assertNull(data.fileName)
        assertNull(data.fileType)
        assertEquals(0.0, data.fileSize, 0.0)
    }

    @Test
    fun builder_lastSetterWins() {
        val data = ImageData.Builder()
            .setFileName("a.png")
            .setFileName("b.png")
            .build()

        assertEquals("b.png", data.fileName)
    }

    @Test
    fun builder_canBuildIndependentInstances() {
        val builder = ImageData.Builder().setFileName("first.png")
        val first = builder.build()
        val second = builder.setFileName("second.png").build()

        assertEquals("first.png", first.fileName)
        assertEquals("second.png", second.fileName)
    }
}
