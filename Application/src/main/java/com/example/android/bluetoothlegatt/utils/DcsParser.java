package com.example.android.bluetoothlegatt.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.MasterAudioController;
import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.WifiController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Cortesza on 28/02/2018.
 */

public class DcsParser {

    public static final String TAG = "DcsParser";
    public static final String DELIMETER = ",";
    public static final String BEGIN_ARRAY = "[";
    public static final String END_ARRAY = "]";

    private Context mContext;

    private WifiController mWifiController;
    private MasterAudioController mAudioController;

    private LinkedList<String> mInstructions = null;
    private LinkedList<String> mArguments = null;

    public DcsParser(Context context) {

        mContext = context;

        mInstructions = new LinkedList<String>();
        mArguments = new LinkedList<String>();

        mAudioController = new MasterAudioController(context);
    }

    public void pushInstruction(String instruction) {
        mInstructions.push(instruction);
    }

    public void pushArgument(String argument) {
        mArguments.push(argument);
    }

    public String popInstruction() {
        if(mInstructions.size() > 0) {
            return mInstructions.pop();
        } else {
            return "";
        }
    }

    public String popArgument() {
        if(mArguments.size() > 0) {
            return mArguments.pop();
        } else {
            return "";
        }
    }

    public void processInstructions() {

        for(int i = 0; i < mInstructions.size(); i++) {
            processInstruction(mInstructions.pop());
        }

        mInstructions.clear();
        mArguments.clear();

    }

    private void processInstruction(String instruction) {

        if(instruction.equals("ms")) {
            mAudioController.muteAllAudio();
            Log.d(TAG, "Successfully muted audio");
        } else if(instruction.equals("ums")){
            mAudioController.unmuteAllAudio();
            Log.d(TAG, "Successfully unmuted audio");
        } else if(instruction.equals("actwf")) {
            mWifiController.enableWifi();
            Log.d(TAG, "Enabled Wi-fi");
        } else if(instruction.equals("sdwf")) {
            mWifiController.disableWifi();
            Log.d(TAG, "Disabled Wi-fi");
        } else if(instruction.equals("conwf")) {

            String networkType = popArgument();
            String pass = popArgument();
            String ssid = popArgument();

            Log.d(TAG, "Connecting to Wifi " + ssid);

            mWifiController.connectToNetwork(ssid, pass, networkType);
        } else if(instruction.equals("curl")) {
            String url = popArgument();
            Log.d(TAG, "Connecting to URL " + url);

            try {
                Intent i = new Intent("android.intent.action.MAIN");
                i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                i.addCategory("android.intent.category.LAUNCHER");
                i.setData(Uri.parse(url));
                mContext.startActivity(i);
            }
            catch(ActivityNotFoundException e) {
                // Chrome is not installed
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(i);
            }

        }

    }

    public void processTopInstruction() {
        processInstruction(mInstructions.pop());
    }

    public String instructionsToString() {
        StringBuilder builder = new StringBuilder(BEGIN_ARRAY);

        for(int i = mInstructions.size() - 1; i >= 0; i--) {
            builder.append(mInstructions.get(i));
            if(i > 0) {
                builder.append(DELIMETER);
            }
        }

        builder.append(END_ARRAY);

        return builder.toString();
    }

    public void stringToInstructions(String insString, String argString) {

        String insBuffer = insString.substring(insString.indexOf(BEGIN_ARRAY) + 1, insString.indexOf(END_ARRAY));
        String argBuffer = argString.substring(argString.indexOf(BEGIN_ARRAY) + 1, argString.indexOf(END_ARRAY));

        String[] insArray = insBuffer.split(DELIMETER);
        String[] argArray = argBuffer.split(DELIMETER);

        mInstructions = new LinkedList<String>(Arrays.asList(insArray));
        mArguments = new LinkedList<String>(Arrays.asList(argArray));

    }

    public String argumentsToString() {
        StringBuilder builder = new StringBuilder(BEGIN_ARRAY);

        for(int i = mArguments.size() - 1; i >= 0; i--) {
            builder.append(mArguments.get(i));
            if(i > 0) {
                builder.append(DELIMETER);
            }
        }

        builder.append(END_ARRAY);

        return builder.toString();
    }

    public DcsParser setWifiController(WifiController controller) {
        this.mWifiController = controller;
        return this;
    }

}