package com.example.dexter007bot.P2PConnect;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static com.example.dexter007bot.MainActivity.TAG;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BroadcastThread implements Runnable {

    private String IP;
    private String data;
    private int PORT;
    private DatagramSocket socket;
    private DatagramPacket info;
    byte[] b = null;

    public BroadcastThread(String IP, String data, int PORT) {
        this.IP = IP;
        this.data = data;
        this.PORT = PORT;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            b = data.getBytes(UTF_8);
            info = new DatagramPacket(b,b.length, InetAddress.getByName(IP),PORT);
            socket.send(info);
            new Thread(new client(IP)).start();
            Log.d(TAG, "IP Sent to host"+IP);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(socket!=null)
                socket.close();
        }
    }
}
