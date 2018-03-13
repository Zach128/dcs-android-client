package com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class WifiController {

    private static final String TAG = WifiController.class.getSimpleName();
    public static final String NO_PASS = "<n>";

    private WifiManager mWifiManager;

    public WifiController(Context context) {

        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    }

    public WifiConfiguration getConfig(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

        for(WifiConfiguration config : configs) {
            if(config.SSID.equals(ssid)) {
                return config;
            }
        }

        return null;

    }

    public void connectToNetwork (String ssid, String pass, String networkType) {

        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + ssid + "\"";

        Log.d(TAG, "WIFI: Configuring password for " + networkType + " type network");
        if(pass.equals(NO_PASS)) {
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if(networkType.equals("WEP")){
            configuration.wepKeys[0] = "\"" + pass + "\"";
            configuration.wepTxKeyIndex = 0;
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if(networkType.equals("WPA")){
            configuration.preSharedKey = "\"" + pass + "\"";
            configuration.status = WifiConfiguration.Status.ENABLED;
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        }

        enableWifi();
        int configId = mWifiManager.addNetwork(configuration);
        Log.d(TAG, ssid+" added");

        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(configId, true);
                if(mWifiManager.reassociate()) {
                    Log.d(TAG, "WIFI: Connecting to: " + configuration.SSID);
                } else {
                    Log.d(TAG, "WIFI: Something went wrong connecting to " + ssid);
                }
                break;
            }
        }

    }

    public void enableWifi() {
        if(!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public void disableWifi() {
        if(mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

}
