package com.example.dexter007bot.Connection;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dexter007bot.MainActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

import static com.example.dexter007bot.MainActivity.wifiManager;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PeerConnection {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    protected Collection<WifiP2pDevice> connectedPeers = new ArrayList<>();
    protected Collection<WifiP2pDevice> requestedPeers = new ArrayList<>();

    public PeerConnection(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mActivity) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }

    public void Discover() {
        if(!wifiManager.isWifiEnabled()) {
            Toast.makeText(mActivity, "Enable WiFi", Toast.LENGTH_SHORT).show();
            return;
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(int reason) {
                //Toast.makeText(mActivity,"Discovering Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }
    WifiP2pManager.PeerListListener peerListListener =new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            requestedPeers = peerList.getDeviceList();
            for (WifiP2pDevice device : requestedPeers) {
                if (!connectedPeers.contains(device))
                    connectPeers(device);
            }
        }
    };
    private void connectPeers(WifiP2pDevice current) {
        final WifiP2pDevice device = current;
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        //System.out.println(device.deviceName);
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Toast.makeText(mActivity, "Requested: " + device.deviceName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                //Toast.makeText(mActivity, "Requesting Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if(wifiP2pInfo.groupFormed){

                mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        if(group==null) return;
                        connectedPeers= group.getClientList();
                        mActivity.logger.write("Updated Peer List: ");
                        for (WifiP2pDevice device : connectedPeers) {
                            mActivity.logger.write(device.deviceName + ": " + device.deviceAddress);
                        }
                    }
                });

                if(wifiP2pInfo.isGroupOwner){
                    mActivity.logger.write("Connected as Host");
                    mActivity.btnWifi.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    new Thread(new PeerConnection.ListenThread()).start();
                    Discover();
                }

                else{
                    String IP = Ip.ipadd();
                    InetAddress host = wifiP2pInfo.groupOwnerAddress;
                    String HOST = host.getHostAddress();
                    mActivity.logger.write("Connected as Client");
                    mActivity.logger.write("Host IP : "+HOST);
                    mActivity.btnWifi.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
                    Thread broadcast = new Thread(new PeerConnection.BroadcastThread(HOST,IP,4040));
                    broadcast.start();
                    new Thread(new client(HOST)).start();
                }
            }
            else Discover();
        }
    };
    public class BroadcastThread implements Runnable{
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
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                b = data.getBytes(UTF_8);
                info = new DatagramPacket(b,b.length,InetAddress.getByName(IP),PORT);
                socket.send(info);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(socket!=null)
                socket.close();
            }
        }

    }

    public class ListenThread implements Runnable{
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
}