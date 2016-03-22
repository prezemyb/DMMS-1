package com.sensordroid.Handlers;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by sveinpg on 28.01.16.
 */
public class DispatchHandler implements Runnable {
    // Cuz we can
    //private final ByteBuffer frame;
    private final String frame;
    private final int port;
    private DatagramSocket sock = null;
    private InetAddress local = null;

    public DispatchHandler(final String frame, final String ip, final int port){
        this.frame = frame;
        this.port = port;
        try {
            this.sock = new DatagramSocket();
            this.local = InetAddress.getByName(ip);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Send stuff
        //Log.d("DISPATCH", "" + Thread.currentThread().getId());
        try {
            int msg_len = frame.length();
            byte[] msg = frame.getBytes();
            DatagramPacket p = new DatagramPacket(msg, msg_len, this.local, this.port);

            this.sock.send(p);
            this.sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
