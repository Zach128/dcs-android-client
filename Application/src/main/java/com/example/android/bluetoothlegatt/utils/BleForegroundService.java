package com.example.android.bluetoothlegatt.utils;

/**
 * Created by Cortesza on 01/03/2018.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.android.bluetoothlegatt.R;

/**
 * Handle running the BluetoothLeService from a foreground app
 */
public class BleForegroundService{


    public static Notification buildForegroundNotification(Context context, PendingIntent pendingIntent) {
        Notification notification = null;

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "DCS parser",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Device Control Service Channel");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "default")
                .setSmallIcon(R.drawable.ic_launcher) // notification icon
                .setContentTitle("DCS controller") // title for notification
                .setContentText("Looking for devices")// message for notification
                .setContentIntent(pendingIntent) //Set the Intent to call on initialisation
                .setPriority(Notification.PRIORITY_HIGH);
        Intent intent = new Intent(context.getApplicationContext(), BluetoothLeService.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        return mBuilder.build();

    }


}
