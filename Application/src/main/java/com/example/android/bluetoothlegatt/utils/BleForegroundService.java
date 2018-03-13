package com.example.android.bluetoothlegatt.utils;

/**
 * Created by Cortesza on 01/03/2018.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.activities.LauncherActivity;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.models.SampleGattAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handle running the BluetoothLeService from a foreground app
 */
public class BleForegroundService extends Service {

    private static final String TAG = BleForegroundService.class.getSimpleName();

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.foreground.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.foreground.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.foreground.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.foreground.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.foreground.EXTRA_DATA";

    public final static UUID UUID_DEVICE_CONTROL_SERVICE =
            UUID.fromString("1116d1c0-f7a4-499c-a7ac-e6a4d49b9e9e");

    public final static UUID UUID_CONTROL_INSTRUCTION_CHARACTERISTIC =
            UUID.fromString("11161234-f7a4-499c-a7ac-e6a4d49b9e9e");

    public final static UUID UUID_INSTRUCTION_ARGUMENT_CHARACTERISTIC =
            UUID.fromString("11164321-f7a4-499c-a7ac-e6a4d49b9e9e");

    private static final UUID UUID_DEVICE_CONTROL_SERVICE_MASK =
            UUID.fromString("00001111-0000-0000-0000-000000000000");

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int SCAN_PERIOD = 10000;

    private static final int NOTIFICATION_ID = 1337;
    public static int mStartId = 0;

    private boolean mScanning = false;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private List<ScanFilter> mScanFilters;
    private ScanSettings mScanSettings;
    private List<String> mVisitedDevices;

    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    private int mStartMode;

    private NotificationManager mNotificationManager;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private Handler mHandler = new Handler();

    //region *****HANDLER*****
    public class LocalBinder extends Binder {
        public BleForegroundService getService() {
            return BleForegroundService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    //endregion

    //region *****GATT_CALLBACK_HANDLER*****
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        List<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                //Add device to list of visited devices
                mVisitedDevices.add(mBluetoothGatt.getDevice().getAddress());

                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            //If services are successfully discovered
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                //Get the DCS service and get all characteristics from it
                BluetoothGattService bleService = mBluetoothGatt.getService(UUID_DEVICE_CONTROL_SERVICE);
                Log.d(TAG, "Discovered service: " + bleService.getUuid());

                if(bleService != null) {
                    characteristics = bleService.getCharacteristics();
                    Log.d(TAG, "INFO: Reading service characteristics");
                    requestCharacteristics(mBluetoothGatt);
                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * Recursive method for reading characterists stored in the {@code characteristics} list
         * @param gatt The GATT object to use for reading
         */
        public void requestCharacteristics(BluetoothGatt gatt) {
            gatt.readCharacteristic(characteristics.get(characteristics.size()-1));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            //If successfull, process the characteristics
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "CHAR: Successfully read characteristic " + characteristic.getStringValue(0));
                processCharacteristic(ACTION_DATA_AVAILABLE, characteristic);
            }

            characteristics.remove(characteristics.size() - 1);
            if(characteristics.size() > 0) {
                requestCharacteristics(gatt);
            } else {
                disconnect();
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            processCharacteristic(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    //endregion

    /**
     * Intent wrapper for broadcasting status updates.
     * Used for broadcasting updates on device connections and service discovery.
     *
     * @param action
     */
    private void broadcastUpdate(final String action) {

        if(action.equals(ACTION_GATT_SERVICES_DISCOVERED)) {

        }

        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Broadcasting " + action, Toast.LENGTH_LONG).show();
            }
        });

        //final Intent intent = new Intent(action);
        //sendBroadcast(intent);
    }

    //region *****CHAR_PROCESSING*****

    /**
     * Intent wrapper for broadcasting status updates.
     * Used for broadcasting updates on characteristic reading and hence should be used for data parsing.
     *
     * @param action
     * @param characteristic
     */
    private void processCharacteristic(final String action,
                                       final BluetoothGattCharacteristic characteristic) {
        //final Intent intent = new Intent(action);

        //Use these if statements for data parsing
        if (UUID_CONTROL_INSTRUCTION_CHARACTERISTIC.equals(characteristic.getUuid())) {
            String data = characteristic.getStringValue(0);
            Log.d(TAG, "Received instructions: " + data);
        } else if (UUID_INSTRUCTION_ARGUMENT_CHARACTERISTIC.equals(characteristic.getUuid())) {
            String data = characteristic.getStringValue(0);
            Log.d(TAG, "Received instruction arguments: " + data);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                //intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }

        //sendBroadcast(intent);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // Enable notifications for specific characteristics
        if (UUID_CONTROL_INSTRUCTION_CHARACTERISTIC.equals(characteristic.getUuid()) ||
                UUID_INSTRUCTION_ARGUMENT_CHARACTERISTIC.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
    //endregion

    public boolean initialize() {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mVisitedDevices = new ArrayList<String>();
        mScanFilters = new ArrayList<ScanFilter>();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID_DEVICE_CONTROL_SERVICE), new ParcelUuid(UUID_DEVICE_CONTROL_SERVICE_MASK))
                .build();
        mScanFilters.add(scanFilter);

        mScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

        return true;
    }

    /**
     * The main logic for the service consisting of scanning and retrieving data from connected devices.
     */
    private void beginMainCycle() {

        final boolean keepAliveCompleted = false;
        final Runnable keepAliveTask = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    Toast.makeText(getApplicationContext(), "INFO_FOREGROUND: Staying alive", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "INFO_FOREGROUND: Keeping alive");
                    notify();
                }
            }
        };

        scanLeDevice(true);

        Log.d(TAG, "INFO: Stopping this service");
        close();
        stopSelf();

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;

                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanFilters, mScanSettings, mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG, "Disconnecting from device");
        mBluetoothGatt.disconnect();
    }

    public void close() {

        if (mBluetoothGatt == null) {
            return;
        }
        disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;

        mVisitedDevices.clear();

    }

    //region *****FOREGROUND_SERVICE_HANDLING*****

    public void runInForeground() {
        Intent notificationIntent = new Intent(this, BluetoothLeService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = buildForegroundNotification(this, pendingIntent);

        startForeground(NOTIFICATION_ID, notification);

    }

    private Notification buildForegroundNotification(Context context, PendingIntent pendingIntent) {
        Notification notification = null;


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
        Intent intent = new Intent(context.getApplicationContext(), LauncherActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        return mBuilder.build();

    }

    //endregion

    //region *****LIFECYCLE_METHODS*****
    @Override
    public void onCreate() {

        //Log.i(TAG, "Initialising foreground service");
        if (initialize()) {
            Log.d(TAG, "INFO: Service successfully initialised");
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        runInForeground();

        beginMainCycle();

        mStartId = startId;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "INFO: Destroying service");

        // Remove the notification when the service is stopped
        mNotificationManager.cancel(NOTIFICATION_ID);

        close();

    }

    //endregion

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            final BluetoothDevice device = result.getDevice();

            if(mVisitedDevices.indexOf(device.getAddress()) == -1) {
                List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();

                Log.d(TAG, "Found new device " + device.getAddress() + " with DCS service: " + uuids.get(0).getUuid().toString());
                if(UUID_DEVICE_CONTROL_SERVICE.equals(uuids.get(0).getUuid())) {
                    connect(device.getAddress());
                }
                Log.d(TAG, "INFO: received null device in onLeScan()");
            }

        }
    };

}
