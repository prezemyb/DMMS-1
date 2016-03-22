package com.sensordroid.Activities;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sensordroid.MainService;
import com.sensordroid.R;

import java.util.ArrayList;

import static com.sensordroid.MainService.PROVIDER_RESULT;

public class MainActivity extends Activity {
    private final static String START_ACTION = "com.sensordroid.START";
    private final static String STOP_ACTION = "com.sensordroid.STOP";

    private final ArrayList<String> list = new ArrayList<>();
    private final static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView = (TextView)findViewById(R.id.textCount);
        textView.getRootView().setBackgroundColor(Color.parseColor("#FF6A6A"));

        //Add the selected drivers to the driver-list
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b!=null)
        {
            for (String elem : b.getStringArrayList("DRIVERS")){
                list.add(elem);
            }
        }

        /*
            Register start button
         */
        Button buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.getRootView().setBackgroundColor(Color.parseColor("#a4c639"));
                Log.d("ListDriverActivity", "Broadcasting start-intent...");

                Intent start = new Intent(START_ACTION);
                start.putStringArrayListExtra("DRIVERS", list);
                start.putExtra("SERVICE_ACTION", "com.sensordroid.service.START_SERVICE");
                start.putExtra("SERVICE_PACKAGE", "com.sensordroid");
                start.putExtra("SERVICE_NAME", "com.sensordroid.MainService");
                sendBroadcast(start);
            }
        });

        /*
            Register stop button
         */
        Button buttonStop = (Button)findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.getRootView().setBackgroundColor(Color.parseColor("#FF6A6A"));

                Log.d("ListDriverActivity", "Broadcasting stop-intent...");
                Intent stop = new Intent(STOP_ACTION);
                sendBroadcast(stop);
            }
        });

        // Bind to service
        bindService(new Intent(this, MainService.class), mConnection, Service.BIND_AUTO_CREATE);
    }

    /*
        Updates TextField with the current sent-count
     */
    private static TextView textView;
    BroadcastReceiver countReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = intent.getIntExtra("COUNT", 0);
            textView.setText("" + count);
        }
    };



    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(countReceiver,
                new IntentFilter(PROVIDER_RESULT)
        );
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(countReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("onDestroy", "Destroyed");
        Intent stop = new Intent(STOP_ACTION);
        sendBroadcast(stop);
        unbindService(mConnection);
        super.onDestroy();
    }
}
