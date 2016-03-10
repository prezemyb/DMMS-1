package com.sensordroid.templatedriver.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sensordroid.templatedriver.MainService;

public class StartReceiver extends BroadcastReceiver {
    private static final String TAG = "StartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got intent. Checking list...");

        // Checking if driver is suppose to start
        int driverId = -1;
        Bundle bundle = intent.getExtras();

        if(bundle!=null)
        {
            int counter = 0;
            for (String elem : bundle.getStringArrayList("DRIVERS")){
                if (elem.equals(MainService.name)){
                    Log.d(TAG, " id found");
                    driverId = counter;
                    break;
                }
            }
        }

        if(driverId != -1) {
            // Sending start-intent to Service
            Intent service = new Intent(context, MainService.class);
            service.putExtra("ACTION", MainService.START_ACTION);
            service.putExtra("DRIVER_ID", driverId);
            context.startService(service);
        }
    }
}
