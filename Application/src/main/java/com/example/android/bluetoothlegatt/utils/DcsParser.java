package com.example.android.bluetoothlegatt.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.MasterAudioController;
import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.WifiController;

import java.util.ArrayList;
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

    private WifiController mWifiController;
    private MasterAudioController mAudioController;

    private LinkedList<String> mInstructions = null;
    private LinkedList<String> mArguments = null;

    public DcsParser(Context context) {
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
        } else if(instruction.equals("sdwf")) {
            mWifiController.disableWifi();
        } else if(instruction.equals("conwf")) {

            String networkType = popArgument();
            String pass = popArgument();
            String ssid = popArgument();

            Log.d(TAG, "ssid: " + ssid + " net type " + networkType + " pass " + pass);

            mWifiController.connectToNetwork(ssid, pass, networkType);
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