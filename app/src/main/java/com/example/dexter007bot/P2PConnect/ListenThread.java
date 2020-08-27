package com.example.dexter007bot.P2PConnect;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ListenThread extends Thread {
    private static final String TAG = "ListenThread";
    DatagramSocket socket;
    DatagramPacket info;
    int port;
    boolean status;
    List<String> receivedIp;

    public ListenThread() throws SocketException {
        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        socket.bind(new InetSocketAddress(4040));
        port = socket.getLocalPort();
        status=true;
        receivedIp= new ArrayList<>();
    }

    @Override
    public void run() {
        Log.e(TAG, "Listen Thread Started at PORT: "+port);
        while(status) {
            Log.d(TAG, "Listen Thread running..");
            byte[] b = new byte[1024];
            info = new DatagramPacket(b, b.length);
            try {
                socket.receive(info);
                String IP = new String(info.getData(), 0, info.getLength(), UTF_8);
                Log.d("Received IP", IP);
                if(!receivedIp.contains(IP)){
                    receivedIp.add(IP);
                    new Thread(new client(IP)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void closeThread(){
        if(socket!=null) socket.close();
        status=false;
        Log.e(TAG, "Listen Thread Stopped!");
    }
}

