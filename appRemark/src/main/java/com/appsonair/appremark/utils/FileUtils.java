package com.appsonair.appremark.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class FileUtils {
    public static double getFileSize(Context context, Uri uri) {
        double size = 0;

        try {
            // For gallery
            if (Objects.equals(uri.getScheme(), "content")) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        size = cursor.getLong(sizeIndex); // Get the file size in bytes
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            // For screenshot
            else if (Objects.equals(uri.getScheme(), "file")) {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    size = file.length(); // Get the file size in bytes
                }
            }
            // Convert size to MB and round to 2 decimal places
            size = size / (1024.0 * 1024.0);
            size = Math.round(size * 100.0) / 100.0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return size;
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = Objects.requireNonNull(result).lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String getFileType(Context context, Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), "content")) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static File getFileFromUri(Context context, Uri uri) {
        File file = null;
        if (uri.getScheme().equals("file")) {
            file = new File(uri.getPath());
        } else {
            try {
                String fileName = getFileNameFromUri(context, uri);
                file = new File(context.getCacheDir(), fileName);

                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @SuppressLint("Range")
    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
