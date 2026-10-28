package com.appsonair.appremark.utils

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.io.ByteArrayInputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileUtilsTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var resolver: ContentResolver
    private lateinit var context: Context
    private val contentUri = Uri.parse("content://media/external/images/media/42")

    @Before
    fun setUp() {
        resolver = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { context.contentResolver } returns resolver
        every { context.cacheDir } returns tmp.root
    }

    private fun cursorWith(name: String?, size: Long?): MatrixCursor {
        val cursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE))
        cursor.addRow(arrayOf<Any?>(name, size))
        return cursor
    }

    private fun emptyCursor() = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE))

    // getFileSize

    @Test
    fun fileSize_contentUri_readsSizeColumnInMegabytes() {
        val cursor = cursorWith("photo.jpg", 3L * 1024 * 1024)
        every { resolver.query(contentUri, null, null, null, null) } returns cursor

        assertEquals(3.0, FileUtils.getFileSize(context, contentUri), 0.0)
        assert(cursor.isClosed)
    }

    @Test
    fun fileSize_isRoundedToTwoDecimals() {
        every { resolver.query(contentUri, null, null, null, null) } returns cursorWith("p", 1_234_567L)

        // 1_234_567 B = 1.17737... MB
        assertEquals(1.18, FileUtils.getFileSize(context, contentUri), 0.0)
    }

    @Test
    fun fileSize_contentUri_nullCursor_returnsZero() {
        every { resolver.query(contentUri, null, null, null, null) } returns null

        assertEquals(0.0, FileUtils.getFileSize(context, contentUri), 0.0)
    }

    @Test
    fun fileSize_contentUri_emptyCursor_returnsZeroAndCloses() {
        val cursor = emptyCursor()
        every { resolver.query(contentUri, null, null, null, null) } returns cursor

        assertEquals(0.0, FileUtils.getFileSize(context, contentUri), 0.0)
        assert(cursor.isClosed)
    }

    @Test
    fun fileSize_contentUri_queryThrows_returnsZero() {
        every { resolver.query(contentUri, null, null, null, null) } throws SecurityException("denied")

        assertEquals(0.0, FileUtils.getFileSize(context, contentUri), 0.0)
    }

    @Test
    fun fileSize_fileUri_readsFileLength() {
        val file = tmp.newFile("shot.jpg").apply { writeBytes(ByteArray(512 * 1024)) }

        assertEquals(0.5, FileUtils.getFileSize(context, Uri.fromFile(file)), 0.0)
    }

    @Test
    fun fileSize_fileUri_missingFile_returnsZero() {
        val uri = Uri.fromFile(File(tmp.root, "missing.jpg"))

        assertEquals(0.0, FileUtils.getFileSize(context, uri), 0.0)
    }

    @Test
    fun fileSize_otherScheme_returnsZero() {
        assertEquals(0.0, FileUtils.getFileSize(context, Uri.parse("https://example.com/a.png")), 0.0)
        verify(exactly = 0) { resolver.query(any(), any(), any(), any(), any()) }
    }

    // getFileName

    @Test
    fun fileName_contentUri_readsDisplayName() {
        val cursor = cursorWith("holiday.png", 10L)
        every { resolver.query(contentUri, null, null, null, null) } returns cursor

        assertEquals("holiday.png", FileUtils.getFileName(context, contentUri))
        assert(cursor.isClosed)
    }

    @Test
    fun fileName_contentUri_noRow_fallsBackToLastPathSegment() {
        every { resolver.query(contentUri, null, null, null, null) } returns emptyCursor()

        assertEquals("42", FileUtils.getFileName(context, contentUri))
    }

    @Test
    fun fileName_contentUri_nullCursor_fallsBackToPath() {
        every { resolver.query(contentUri, null, null, null, null) } returns null

        assertEquals("42", FileUtils.getFileName(context, contentUri))
    }

    @Test
    fun fileName_contentUri_nullDisplayName_fallsBackToPath() {
        every { resolver.query(contentUri, null, null, null, null) } returns cursorWith(null, 1L)

        assertEquals("42", FileUtils.getFileName(context, contentUri))
    }

    @Test
    fun fileName_fileUri_returnsLastSegment() {
        assertEquals("shot.jpg", FileUtils.getFileName(context, Uri.parse("file:///data/cache/shot.jpg")))
    }

    @Test
    fun fileName_pathWithoutSlash_returnedAsIs() {
        assertEquals("plain.jpg", FileUtils.getFileName(context, Uri.parse("plain.jpg")))
    }

    @Test(expected = NullPointerException::class)
    fun fileName_uriWithoutPath_throws() {
        FileUtils.getFileName(context, Uri.parse("mailto:someone@example.com"))
    }

    // getFileType

    @Test
    fun fileType_contentUri_usesResolverType() {
        every { resolver.getType(contentUri) } returns "image/webp"

        assertEquals("image/webp", FileUtils.getFileType(context, contentUri))
    }

    @Test
    fun fileType_contentUri_unknownType_returnsNull() {
        every { resolver.getType(contentUri) } returns null

        assertNull(FileUtils.getFileType(context, contentUri))
    }

    @Test
    fun fileType_fileUri_usesExtensionCaseInsensitively() {
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypeMapping("jpg", "image/jpeg")
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypeMapping("png", "image/png")

        assertEquals("image/jpeg", FileUtils.getFileType(context, Uri.parse("file:///cache/a.jpg")))
        assertEquals("image/png", FileUtils.getFileType(context, Uri.parse("file:///cache/B.PNG")))
        verify(exactly = 0) { resolver.getType(any()) }
    }

    @Test
    fun fileType_fileUri_unknownExtension_returnsNull() {
        assertNull(FileUtils.getFileType(context, Uri.parse("file:///cache/a.unknownext")))
        assertNull(FileUtils.getFileType(context, Uri.parse("file:///cache/noextension")))
    }

    // convertMBToBytes

    @Test
    fun convertMBToBytes_convertsAndTruncates() {
        assertEquals(0L, FileUtils.convertMBToBytes(0.0))
        assertEquals(1_048_576L, FileUtils.convertMBToBytes(1.0))
        assertEquals(524_288L, FileUtils.convertMBToBytes(0.5))
        assertEquals(1_237_319L, FileUtils.convertMBToBytes(1.18))
        assertEquals(-1_048_576L, FileUtils.convertMBToBytes(-1.0))
    }

    @Test
    fun convertMBToBytes_roundTripsWithFileSizePrecision() {
        // getFileSize rounds to 2 decimals, so the bytes sent to the API are an approximation
        val bytes = 1_234_567L
        val mb = Math.round(bytes / (1024.0 * 1024.0) * 100.0) / 100.0
        val diff = Math.abs(FileUtils.convertMBToBytes(mb) - bytes)
        assert(diff <= 0.005 * 1024 * 1024)
    }

    // getFileFromUri

    @Test
    fun fileFromUri_fileScheme_returnsSameFileWithoutCopy() {
        val file = tmp.newFile("original.jpg")

        val result = FileUtils.getFileFromUri(context, Uri.fromFile(file))

        assertEquals(file.absolutePath, result.absolutePath)
        verify(exactly = 0) { resolver.openInputStream(any()) }
    }

    @Test
    fun fileFromUri_contentScheme_copiesStreamIntoCache() {
        val payload = ByteArray(5000) { (it % 251).toByte() }
        every { resolver.query(contentUri, null, null, null, null) } returns cursorWith("gallery.jpg", 5000L)
        every { resolver.openInputStream(contentUri) } returns ByteArrayInputStream(payload)

        val result = FileUtils.getFileFromUri(context, contentUri)

        assertEquals(File(tmp.root, "gallery.jpg"), result)
        assertArrayEquals(payload, result.readBytes())
    }

    @Test
    fun fileFromUri_contentScheme_streamFailure_returnsEmptyFile() {
        every { resolver.query(contentUri, null, null, null, null) } returns cursorWith("broken.jpg", 1L)
        every { resolver.openInputStream(contentUri) } returns null

        val result = FileUtils.getFileFromUri(context, contentUri)

        // the output file is created before the null stream is read, so an empty file is left behind
        assertEquals(File(tmp.root, "broken.jpg"), result)
        assertEquals(0L, result.length())
    }

    // getFileNameFromUri

    @Test
    fun fileNameFromUri_contentUri_readsDisplayName() {
        val cursor = cursorWith("doc.png", 1L)
        every { resolver.query(contentUri, null, null, null, null) } returns cursor

        assertEquals("doc.png", FileUtils.getFileNameFromUri(context, contentUri))
        assert(cursor.isClosed)
    }

    @Test
    fun fileNameFromUri_contentUri_noRow_usesLastPathSegment() {
        every { resolver.query(contentUri, null, null, null, null) } returns emptyCursor()

        assertEquals("42", FileUtils.getFileNameFromUri(context, contentUri))
    }

    @Test
    fun fileNameFromUri_nonContentUri_usesLastPathSegment() {
        assertEquals("x.jpg", FileUtils.getFileNameFromUri(context, Uri.parse("file:///a/b/x.jpg")))
        verify(exactly = 0) { resolver.query(any(), any(), any(), any(), any()) }
    }

    @Test(expected = NullPointerException::class)
    fun fileNameFromUri_nullCursor_throws() {
        // unlike getFileName, this variant closes the cursor without a null check
        every { resolver.query(contentUri, null, null, null, null) } returns null

        FileUtils.getFileNameFromUri(context, contentUri)
    }
}
