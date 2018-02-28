package com.example.android.bluetoothlegatt.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Cortesza on 28/02/2018.
 */

public class DeviceControlService extends Service {

    private static final String TAG = DeviceControlService.class.getSimpleName();

    private final IBinder mBinder = new DcsBinder();

    public class DcsBinder extends Binder {
        public DeviceControlService getService() {
            return DeviceControlService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
