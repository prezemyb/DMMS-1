package com.bitalino.sensordroid.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bitalino.sensordroid.MainService;

public class StopReceiver extends BroadcastReceiver {
    private static final String TAG = "StopReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got stop intent");

        Intent service = new Intent(context, MainService.class);
        service.putExtra("ACTION", MainService.STOP_ACTION);
        context.startService(service);
    }
}
