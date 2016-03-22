package com.bitalino.sensordroid.Handlers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.bitalino.sensordroid.MainActivity;
import com.sensordroid.IMainServiceConnection;
import com.sensordroid.librarydriver.BITalinoDevice;
import com.sensordroid.librarydriver.BITalinoException;
import com.sensordroid.librarydriver.BITalinoFrame;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
*/

public class ConnectionHandler implements Runnable {
    private static final String TAG = ConnectionHandler.class.toString();

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final IMainServiceConnection binder;

    // Get these values from shared preferences
    private static int[] typeList;
    private static int[] channelList;
    private static String remoteDevice = "98:D3:31:B2:BB:A5";
    private static final int FRAMES_TO_READ = 1;
    private static int SAMPLING_FREQ = 1;
    private static int driverId;
    private static boolean interrupted;

    private BluetoothSocket mSocket;
    private BITalinoDevice bitalino;

    public ConnectionHandler(final IMainServiceConnection binder, String name, int id, Context context) {
        this.driverId = id;
        this.binder = binder;

        this.bitalino = null;
        this.mSocket = null;
        this.interrupted = false;

        // Get types of channels from shared preferences
        String savedString = context.getSharedPreferences(MainActivity.sharedKey,
                Context.MODE_PRIVATE).getString(MainActivity.channelKey, "0,0,0,0,0,0");
        StringTokenizer st = new StringTokenizer(savedString, ",");

        // Create list of the data types
        int active_channels = 0;
        typeList = new int[MainActivity.NUM_CHANNELS];
        for (int i = 0; i < typeList.length; i++) {
            typeList[i] = Integer.parseInt(st.nextToken());
            if (typeList[i] != 0){
                active_channels++;
            }
        }

        // Create a list of the active channels
        channelList = new int[active_channels];
        int index = 0;
        for(int i = 0; i < typeList.length; i++) {
            if (typeList[i]!= 0) {
                channelList[index++] = i;
            }
        }
        executor.submit(new MetadataHandler(binder, name, id, context, typeList));

        // Get the sampling frequency from the shared preferences.
        int tmpFreq = context.getSharedPreferences(MainActivity.sharedKey,
                Context.MODE_PRIVATE).getInt(MainActivity.frequencyKey, 0);
        if (tmpFreq < 17) {
            SAMPLING_FREQ = 1;
        } else if (tmpFreq < 50) {
            SAMPLING_FREQ = 10;
        } else if (tmpFreq< 83) {
            SAMPLING_FREQ = 100;
        } else {
            SAMPLING_FREQ = 1000;
        }

        remoteDevice = context.getSharedPreferences(MainActivity.sharedKey,
                Context.MODE_PRIVATE).getString(MainActivity.macKey, "98:D3:31:B2:BB:A5");
    }


    @Override
    public void run() {
        int sleepTime = 1000;
        while (!interrupted) {
            if (Thread.currentThread().isInterrupted()) {
                interrupted = true;
                return;
            }
            if (connect()) {
                Log.d("Run()", "connection successfull");

                sleepTime = 1000;
                // Start acquisition of predefined channels
                collectData();
            }
            resetConnection();
            try {
                Log.d("ConnectionThread", "Sleeping for: " + sleepTime + " milliseconds");
                Thread.sleep(sleepTime);
                if (sleepTime < 30000)
                    sleepTime = sleepTime * 2;
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                return;
            }
        }
    }

    /*
        Collects data from the bitalino until the thread is interrupted.
     */
    private void collectData(){
        try {
            bitalino.start();

            while (!interrupted) {
                if (Thread.currentThread().isInterrupted()) {
                    interrupted = true;
                    break;
                }
                final BITalinoFrame[] frames;
                frames = bitalino.read(FRAMES_TO_READ);

                for (final BITalinoFrame frame : frames) {
                    // Pass the BITalinoFrame to a worker thread
                    executor.submit(new FrameHandler(binder, frame, driverId, typeList, channelList));
                }
            }
        } catch (BITalinoException be) {
            be.printStackTrace();
            return;
        }
    }

    /*
        Connects to the bitalino via bluetooth
    */
    public boolean connect(){
        // Connect to the bluetooth device
        final BluetoothAdapter blueAdapt = BluetoothAdapter.getDefaultAdapter();
        final BluetoothDevice dev = blueAdapt.getRemoteDevice(remoteDevice);


        if (!blueAdapt.isEnabled()){
            Log.d(TAG, "Enabling bluetooth");
            blueAdapt.enable();
        }
        ParcelUuid[] uuidParcel = dev.getUuids();

        boolean connected = false;
        for (ParcelUuid uuid : uuidParcel) {
            BluetoothSocket tmp;
            try {
                tmp = dev.createInsecureRfcommSocketToServiceRecord(uuid.getUuid());
            } catch (IOException ioe){
                ioe.printStackTrace();
                continue;
            }
            mSocket = tmp;

            blueAdapt.cancelDiscovery();
            try {
                mSocket.connect();
                connected = true;
                break;
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }

        if (Thread.currentThread().isInterrupted()){
            interrupted = true;
            return false;
        }
        if(!connected){
            // Could not connect to the bluetooth device.
            return false;
        }

        Log.d(TAG, "Connecting to BITalino");

        // Creating a new bitalino device.
        try {
            bitalino = new BITalinoDevice(SAMPLING_FREQ, channelList);
            bitalino.open(mSocket.getInputStream(), mSocket.getOutputStream());
        } catch (BITalinoException be) {
            be.printStackTrace();
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }

        // Connection successful
        return true;

    }

    /*
        Close the bitalino and the bluetooth connection
     */

    private void resetConnection(){
        Log.d("debug", "entering resetconnection");
        if(bitalino != null){
            try {
                bitalino.stop();
                bitalino = null;
                Log.d(TAG, "Bitalino is stopped");
            } catch (BITalinoException e) {
                e.printStackTrace();
            }
        }

        if (mSocket != null){
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
