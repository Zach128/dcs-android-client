package com.example.android.bluetoothlegatt.utils;

import android.util.Log;

import com.example.android.bluetoothlegatt.com.example.android.bluetoothlegatt.contextcontrollers.MasterAudioController;

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

    public static final Set<String> SUPPORTED_INSTRUCTIONS = new HashSet<String>();

    private LinkedList<String> mInstructions = null;
    private LinkedList<String> mArguments = null;

    public DcsParser() {
        mInstructions = new LinkedList<String>();
        mArguments = new LinkedList<String>();

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
            MasterAudioController mac = MasterAudioController.getInstance();
            mac.muteAllAudio();
            Log.d(TAG, "Successfully muted audio");
        } else if(instruction.equals("ums")){
            MasterAudioController mac = MasterAudioController.getInstance();
            mac.unmuteAllAudio();
            Log.d(TAG, "Successfully unmuted audio");
        }
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

}
