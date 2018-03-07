package com.example.android.bluetoothlegatt.utils;

import android.util.Log;

import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.MasterAudioController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Cortesza on 28/02/2018.
 */

public class DcsParser {

    public static final String TAG = "DcsParser";

    private LinkedList<String> mInstructions = null;
    private LinkedList<String> mArguments = null;

    public DcsParser() {
        mInstructions = new LinkedList<String>();
        mArguments = new LinkedList<String>();
    }

    public void pushInstructions(String[] instructions) {
        for(String s : instructions) {
            mInstructions.push(s);
        }
    }

    public void pushArguments(String[] arguments) {
        for(String s : arguments) {
            mArguments.push(s);
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
            MasterAudioController mac = MasterAudioController.getInstance();
            mac.muteAllAudio();
            Log.d(TAG, "Successfully muted audio");
        } else if(instruction.equals("ums")){
            MasterAudioController mac = MasterAudioController.getInstance();
            mac.unmuteAllAudio();
            Log.d(TAG, "Successfully unmuted audio");
        }
    }

}
