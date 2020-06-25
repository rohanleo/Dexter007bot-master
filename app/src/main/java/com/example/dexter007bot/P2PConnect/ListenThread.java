package com.example.dexter007bot.P2PConnect;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ListenThread implements Runnable {

    DatagramSocket socket;
    DatagramPacket info;

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(4040);
            socket.setBroadcast(true);
            byte[] b= new byte[1024];
            info = new DatagramPacket(b,b.length);
            socket.receive(info);
            String IP = new String(info.getData(),0,info.getLength(), UTF_8);
            Log.d("Received IP",IP);
            new Thread(new client(IP)).start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket!=null)
                socket.close();
        }
    }
}

