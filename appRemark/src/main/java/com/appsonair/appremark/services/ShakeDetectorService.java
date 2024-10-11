package com.appsonair.appremark.services;

import static android.content.Context.SENSOR_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.appsonair.appremark.R;
import com.appsonair.appremark.activities.EditImageActivity;
import com.appsonair.appremark.activities.RemarkActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ShakeDetectorService {

    private static final String TAG = "ShakeDetectorService";
    public static boolean isProcessing = false;
    private static SensorManager mSensorManager;
    private static SensorEventListener msSensorEventListener;
    private static float mAccel, mAccelCurrent, mAccelLast;

    public static void shakeDetect(Context context) {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(msSensorEventListener);
        }

        msSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.9f + delta;
                if (mAccel > 12 && !isProcessing) {
                    isProcessing = true;
                    try {
                        captureScreen(context);
                    } catch (Exception e) {
                        isProcessing = false;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        Objects.requireNonNull(mSensorManager).registerListener(msSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    private static void captureScreen(Context context) {
        final Activity activity = (Activity) context;
        if (activity != null) {
            View rootView = activity.getWindow().getDecorView().getRootView();
            if (rootView != null) {
                rootView.setDrawingCacheEnabled(true);
                Bitmap screenshotBitmap = Bitmap.createBitmap(rootView.getDrawingCache());
                rootView.setDrawingCacheEnabled(false);
                String screenshotPath = saveBitmapToFile(screenshotBitmap, context);
                File imageFile;
                if (screenshotPath != null) {
                    imageFile = new File(screenshotPath);
                    if (imageFile.exists()) {
                        Uri imageUri = Uri.fromFile(imageFile);
                        Intent intent = new Intent(context, EditImageActivity.class);
                        intent.setAction(Intent.ACTION_EDIT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.putExtra("IMAGE_PATH", imageUri);
                        context.startActivity(intent);
                    }
                }
            }
        }
    }

    public static void addRemarkManually(Context context) {
        Intent intent = new Intent(context, RemarkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private static String saveBitmapToFile(Bitmap bitmap, Context context) {
        try {
            File cacheDir = context.getCacheDir();
            String fileName = context.getString(R.string.app_name) + "_" + getCurrentDateTime() + ".jpg";
            File screenshotFile = new File(cacheDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(screenshotFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            return screenshotFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error while saving screenshot", e);
            return null;
        }
    }
}
