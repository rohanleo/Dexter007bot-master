package com.example.dexter007bot.Connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.appcompat.graphics.drawable.DrawableWrapper;

import com.example.dexter007bot.MainActivity;

import java.io.IOException;
import java.util.ArrayList;

public class WiFiDirect extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    private PeerConnection peerConnection;

    public  WiFiDirect(WifiP2pManager mManager,WifiP2pManager.Channel mChannel,MainActivity mainActivity)
    {
        this.mManager=mManager;
        this.mChannel=mChannel;
        this.mActivity=mainActivity;
        peerConnection= new PeerConnection(mManager,mChannel,mActivity);
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        String action=intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFi Enabled", Toast.LENGTH_SHORT).show();
                WebServer webServer = new WebServer();
                try {
                    webServer.start();
                    MainActivity.logger.write("WiFi Enabled : Server Started");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                peerConnection.Discover();
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
            }
        }

        else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
            }
            else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
            }
        }

        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(mManager!=null) {
                mManager.requestPeers(mChannel, peerConnection.peerListListener);
            }
        }

        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) return;

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, peerConnection.connectionInfoListener);
                /*WifiP2pGroup connects = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
                if (connects != null)
                    peerConnection.connectedPeers = connects.getClientList();

                for (WifiP2pDevice device : peerConnection.connectedPeers) {
                    mActivity.logger.write(device.deviceName + ": " + device.deviceAddress);
                }*/
            }
            else {
                mActivity.btnWifi.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                MainActivity.logger.write("No Connections");
                peerConnection.connectedPeers = new ArrayList<>();
            }
        }

        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
        }
    }

}