package com.sensordroid;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.sensordroid.Activities.ConfigurationActivity;
import com.sensordroid.Handlers.DispatchFileHandler;
import com.sensordroid.Handlers.DispatchHandler;
import com.sensordroid.Handlers.DispatchTCPHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainService extends Service {
    private static final String TAG = "MainService";
    public static final String PROVIDER_RESULT = "com.sensordroid.UPDATE_COUNT";

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private LocalBroadcastManager broadcaster;

    // Update count variables
    private static boolean update = true;
    private static int count;

    // TCP variables
    public static String SERVER_IP;
    public static int SERVER_PORT;
    public static boolean tcp = true;
    private static Socket socket;
    private static OutputStream output;
    private static PrintWriter printWriter;

    // File variables
    public static boolean toFile = true;
    private static FileWriter fileOut;
    private static String filePath = "datasamples.txt";

    /*
        Implementation of the interface defined in MainServiceConnection.aidl
     */
    private final IMainServiceConnection.Stub binder = new IMainServiceConnection.Stub() {
        /*
            Receives string from a remote process and passes it a sender-thread
         */
        @Override
        public void putJson(String json) {
            //TODO: Rewrite method
            if (toFile) {
                executor.submit(new DispatchFileHandler(json, fileOut));
            } else if (tcp) {
                executor.submit(new DispatchTCPHandler(json, output, printWriter));
            } else {
                executor.submit(new DispatchHandler(json, SERVER_IP, SERVER_PORT));
            }

            if(update) {
                count++;
                updateCount();
            }
            /*
            long current_time = System.currentTimeMillis();
            if (current_time - last_broadcast > 1000) {
                // TODO: Not syncronized
                last_broadcast = current_time;
                updateCount(0);
            }
            */
            return;
        }
    };

    /*
        Updates TextField in MainActivity
            - The count is appended to make sure the count is correct even
              if the user change foreground activity
     */
    public void updateCount() {
        //count += incr;
        Intent intent = new Intent(PROVIDER_RESULT);
        intent.putExtra("COUNT", count);
        broadcaster.sendBroadcast(intent);
    }


    /*
        Return binder object to expose the implemented interface to remote processes.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Got connection from some dude.");
        return binder;
    }

    /*
        Initialize variables
     */
    public void onCreate(){
        super.onCreate();

        // Initialize variables
        count = 0;
        broadcaster = LocalBroadcastManager.getInstance(this);

        // Set the Service to the foreground to decrease chance of getting killed
        toForeground();

        // Collect value from shared preferences and set up TCP connection if tcp is selected
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                ConfigurationActivity.sharedKey, Context.MODE_PRIVATE);
        toFile = sharedPreferences.getBoolean(ConfigurationActivity.usefileKey, false);
        filePath = sharedPreferences.getString(ConfigurationActivity.fileNameKey, "datasamples.txt");
        tcp = sharedPreferences.getBoolean(ConfigurationActivity.tcpKey, true);
        SERVER_IP = sharedPreferences.getString(ConfigurationActivity.ipKey, "vor.ifi.uio.no");
        SERVER_PORT = sharedPreferences.getInt(ConfigurationActivity.portKey, 12345);
        update = sharedPreferences.getBoolean(ConfigurationActivity.updateCountKey, true);

        Log.d("ON CREATE", "update: " +update);
        Log.d("ON CREATE", "tcp: " + tcp);
        Log.d("ON CREATE", "toFile: " + toFile);

        if (toFile){
            openFile();
        }else if (tcp){
            new ConnectTCPTask().execute();
        }
    }

    public void openFile(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            try {
                File outfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filePath);

                fileOut = new FileWriter(outfile, true);
                //fileOut = new FileOutputStream(outfile);
                //outWriter = new PrintWriter(fileOut);
                Intent intent =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outfile));
                sendBroadcast(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Used to connect to via TCP the given IP/Port
        TODO: Error-message if not successful maybe against global variable
            - Save to file if not connected?
     */
    class ConnectTCPTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            Log.d("TCP-setup", "connecting to "+ SERVER_IP);
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                //socket.setTcpNoDelay(true);
                output = socket.getOutputStream();
                printWriter = new PrintWriter(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /*
        Sets the current service to the foreground
     */
    public void toForeground() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.stat_notify_chat);
        builder.setContentTitle("Collector");
        builder.setTicker("Forwarding");
        builder.setContentText("Forwarding data");

        Intent i = new Intent(this, MainService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        builder.setContentIntent(pi);

        final Notification note = builder.build();

        startForeground(android.os.Process.myPid(), note);
    }

    public void onDestroy(){
        Log.d("ON DESTROY", "Service destroyed");

        // Close tcp-connection
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (toFile && fileOut != null){
            try {
                //outWriter.close();
                fileOut.flush();
                fileOut.close();
                //TODO: Close file as well?
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Move the service back from the foreground
        stopForeground(true);
        super.onCreate();
    }
}
