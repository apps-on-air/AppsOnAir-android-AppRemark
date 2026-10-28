package com.appsonair.appremark.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.storage.StorageManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class AppUtilsTest {

    @Test
    fun readableStorageSize_zeroOrNegative_returnsZero() {
        assertEquals("0", AppUtils.getReadableStorageSize(0))
        assertEquals("0", AppUtils.getReadableStorageSize(-1))
        assertEquals("0", AppUtils.getReadableStorageSize(Long.MIN_VALUE))
    }

    @Test
    fun readableStorageSize_bytes() {
        assertEquals("1 B", AppUtils.getReadableStorageSize(1))
        assertEquals("1,023 B", AppUtils.getReadableStorageSize(1023))
    }

    @Test
    fun readableStorageSize_scalesThroughUnits() {
        assertEquals("1 KB", AppUtils.getReadableStorageSize(1024))
        assertEquals("1.5 KB", AppUtils.getReadableStorageSize(1536))
        assertEquals("1 MB", AppUtils.getReadableStorageSize(1024L * 1024))
        assertEquals("1 GB", AppUtils.getReadableStorageSize(1024L * 1024 * 1024))
        assertEquals("1 TB", AppUtils.getReadableStorageSize(1024L * 1024 * 1024 * 1024))
    }

    @Test
    fun readableStorageSize_roundsToOneDecimal() {
        // 1.25 MB -> one fraction digit with half-even rounding
        assertEquals("1.2 MB", AppUtils.getReadableStorageSize((1.25 * 1024 * 1024).toLong()))
        assertEquals("2.7 GB", AppUtils.getReadableStorageSize((2.66 * 1024 * 1024 * 1024).toLong()))
    }

    @Test(expected = ArrayIndexOutOfBoundsException::class)
    fun readableStorageSize_beyondTerabytes_hasNoUnit() {
        AppUtils.getReadableStorageSize(1024L * 1024 * 1024 * 1024 * 1024)
    }

    @Test
    fun availableMemory_queriesActivityManager() {
        val activityManager = mockk<ActivityManager>()
        val captured = slot<ActivityManager.MemoryInfo>()
        every { activityManager.getMemoryInfo(capture(captured)) } answers {
            captured.captured.availMem = 512L
            captured.captured.totalMem = 2048L
        }
        val activity = mockk<Activity>()
        every { activity.getSystemService(Context.ACTIVITY_SERVICE) } returns activityManager

        val info = AppUtils.getAvailableMemory(activity)

        assertSame(captured.captured, info)
        assertEquals(512L, info.availMem)
        assertEquals(2048L, info.totalMem)
    }

    private fun storageContext(total: Long, free: Long, fail: Boolean = false): Context {
        val uuid = UUID.randomUUID()
        val storageManager = mockk<StorageManager>()
        every { storageManager.getUuidForPath(any()) } returns uuid
        val statsManager = mockk<StorageStatsManager>()
        if (fail) {
            every { statsManager.getTotalBytes(uuid) } throws IOException("boom")
        } else {
            every { statsManager.getTotalBytes(uuid) } returns total
            every { statsManager.getFreeBytes(uuid) } returns free
        }
        val context = mockk<Context>()
        every { context.getSystemService(Context.STORAGE_SERVICE) } returns storageManager
        every { context.getSystemService(Context.STORAGE_STATS_SERVICE) } returns statsManager
        return context
    }

    @Test
    fun totalStorageSize_returnsTotalBytes() {
        assertEquals(1000L, AppUtils.getTotalStorageSize(storageContext(1000L, 300L), true))
    }

    @Test
    fun totalStorageSize_returnsUsedBytesWhenNotTotal() {
        assertEquals(700L, AppUtils.getTotalStorageSize(storageContext(1000L, 300L), false))
    }

    @Test
    fun totalStorageSize_ioError_returnsZero() {
        assertEquals(0L, AppUtils.getTotalStorageSize(storageContext(0, 0, fail = true), true))
    }

    @Test
    @Config(sdk = [25])
    fun totalStorageSize_beforeOreo_returnsZero() {
        // no mocks here: MockK proxies don't survive switching Robolectric SDK sandboxes
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0L, AppUtils.getTotalStorageSize(context, true))
        assertEquals(0L, AppUtils.getTotalStorageSize(context, false))
    }
}
