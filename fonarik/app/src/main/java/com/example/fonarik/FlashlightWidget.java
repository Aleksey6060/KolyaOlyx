package com.example.fonarik;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.widget.RemoteViews;

public class FlashlightWidget extends AppWidgetProvider {

    private static boolean isFlashlightOn = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, FlashlightWidget.class);
            intent.setAction("TOGGLE_FLASHLIGHT");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.flashlight_widget);
            views.setOnClickPendingIntent(R.id.btn_flashlight, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if ("TOGGLE_FLASHLIGHT".equals(intent.getAction())) {
            toggleFlashlight(context);
        }
    }

    private void toggleFlashlight(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            isFlashlightOn = !isFlashlightOn;
            cameraManager.setTorchMode(cameraId, isFlashlightOn);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}