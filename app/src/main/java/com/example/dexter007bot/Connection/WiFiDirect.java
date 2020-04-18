package com.example.dexter007bot.Connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.example.dexter007bot.MainActivity;

import java.io.IOException;

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
                    //Toast.makeText(mActivity,"Server Started",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                peerConnection.Discover();
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                //Toast.makeText(context, "WiFi Disabled", Toast.LENGTH_SHORT).show();
            }
        }

        else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                //Toast.makeText(mActivity,"Discovery Stopped",Toast.LENGTH_SHORT).show();
            }
            else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                //Toast.makeText(mActivity,"Discovery Started",Toast.LENGTH_SHORT).show();
            }
        }

        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(mManager!=null) {
                mManager.requestPeers(mChannel, peerConnection.peerListListener);
            }
        }

        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(mManager==null) return;
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, peerConnection.connectionInfoListener);
            }
            else{
                //Toast.makeText(mActivity,"Disconnected",Toast.LENGTH_SHORT).show();
            }
            WifiP2pGroup connects = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
            if(connects!=null)
                peerConnection.connectedPeers = connects.getClientList();
        }

        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            /*mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(mActivity,"Removing from group",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reason) {
                }
            });*/
        }
    }

}