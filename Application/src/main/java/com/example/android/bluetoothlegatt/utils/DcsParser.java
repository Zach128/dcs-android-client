package com.example.android.bluetoothlegatt.utils;

import android.util.Log;

import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.MasterAudioController;

/**
 * Created by Cortesza on 28/02/2018.
 */

public class DcsParser {

    public static final String TAG = "DcsParser";

    public static void parseInstructions(String instructions, String arguments) {

        if(instructions.equals("ms")) {
            MasterAudioController mac = MasterAudioController.getInstance();
            mac.muteAllAudio();
            Log.d(TAG, "Successfully muted audio");
        } else if(instructions.equals("ums")){
            MasterAudioController mac = MasterAudioController.getInstance();
            mac.unmuteAllAudio();
            Log.d(TAG, "Successfully unmuted audio");
        }
    }

}
