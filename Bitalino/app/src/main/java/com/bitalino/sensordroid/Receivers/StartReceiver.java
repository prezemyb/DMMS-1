package com.bitalino.sensordroid.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bitalino.sensordroid.MainService;

public class StartReceiver extends BroadcastReceiver {
    private static final String TAG = "StartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got intent. Connecting to service...");

        int driverId = -1;
        Bundle b = intent.getExtras();

        if(b!=null)
        {
            int counter = 0;
            for (String elem : b.getStringArrayList("DRIVERS")){
                if (elem.equals(MainService.name)){
                    Log.d(TAG, " id found");
                    driverId = counter;
                    break;
                }
            }
        }

        if(driverId != -1) {
            Intent service = new Intent(context, MainService.class);
            service.putExtra("ACTION", MainService.START_ACTION);
            service.putExtra("DRIVER_ID", driverId);
            context.startService(service);
        }
    }
}
