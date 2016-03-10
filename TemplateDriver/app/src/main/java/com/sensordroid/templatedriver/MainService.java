package com.sensordroid.templatedriver;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.sensordroid.IMainServiceConnection;
import com.sensordroid.templatedriver.Handlers.CommunicationHandler;

public class MainService extends Service {
    public static final String START_ACTION = "com.sensordroid.START";
    public static final String STOP_ACTION = "com.sensordroid.STOP";

    /*
        TODO: Change the name to the name of your driver
            - This name is the name used in the data packets and when the driver is listed in the main application
     */
    //public static final String name = "<YOUR DRIVER NAME GOES HERE>";
    public static final String name = R.string.app_name;

    private static int id;
    private static IMainServiceConnection binder;
    private MainServiceConnection serviceConnection;

    @Override
    public void onCreate(){
        this.serviceConnection = new MainServiceConnection();
        this.id = -1;
        this.binder = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
        Called when started by intent, as from StartReceiver and StopReceiver
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null) {
            String extraString = intent.getStringExtra("ACTION");
            if (extraString.compareTo(START_ACTION) == 0) {
                this.id = intent.getIntExtra("DRIVER_ID", 0);
                start();
            }
            if (extraString.compareTo(STOP_ACTION) == 0) {
                stop();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /*
        Start data acquisition by binding to the Collector application
     */
    public void start() {
        Log.d("bind service", getApplicationContext().toString());
        if(binder == null) {
            Intent service = new Intent("com.sensordroid.service.START_SERVICE");
            service.setComponent(new ComponentName("com.sensordroid", "com.sensordroid.MainService"));
            getApplicationContext().bindService(service, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    /*
        Stop data acquisition, interrupt thread and unbind
     */
    public void stop() {
        if(binder != null) {
            try {
                serviceConnection.interruptThread();
                getApplicationContext().unbindService(serviceConnection);
            } catch (IllegalArgumentException iae){
                iae.printStackTrace();
            }
            stopSelf();
        }
    }

    private class MainServiceConnection implements ServiceConnection {
        private Thread connectionThread;

        public void interruptThread() {
            /* Interrupt thread*/
            if (connectionThread != null) {
                connectionThread.interrupt();
                connectionThread = null;
            }
            binder = null;
        }

        /*
            Called when the service is bound successfully
        */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = IMainServiceConnection.Stub.asInterface(iBinder);

            // Starts the thread for communication with the device
            connectionThread = new Thread(new CommunicationHandler(binder, name, id));
            connectionThread.start();
        }

        /*
            Called if the service unbound unexpectedly
        */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            interruptThread();
        }
    }
}
