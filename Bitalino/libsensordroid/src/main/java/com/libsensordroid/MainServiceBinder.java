package com.libsensordroid;

import android.os.Binder;
import android.util.Log;

import java.util.Arrays;

public class MainServiceBinder extends Binder {
    private static final String TAG = "MainServiceBinder";

    /*
    public void putFrame(String sender, byte[] frame) {
        Log.d(TAG, "Got frame from " + sender + ": " + Arrays.toString(frame));
    }
    */
}
