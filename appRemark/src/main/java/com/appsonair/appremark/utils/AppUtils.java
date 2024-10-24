package com.appsonair.appremark.utils;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.UUID;

public class AppUtils {
    public static ActivityManager.MemoryInfo getAvailableMemory(Activity activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    public static String getReadableStorageSize(long size) {
        if (size <= 0) return "0";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long getTotalStorageSize(Context context, boolean getTotalStorage) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                UUID uuid = storageManager.getUuidForPath(Environment.getDataDirectory());
                long totalBytes = storageStatsManager.getTotalBytes(uuid);
                long freeBytes = storageStatsManager.getFreeBytes(uuid);
                long usedBytes = totalBytes - freeBytes;
                return getTotalStorage ? totalBytes : usedBytes;
            } catch (IOException e) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }
}
