package com.example.dexter007bot.P2PConnect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.example.dexter007bot.Service.P2PNearbyService;

import java.io.IOException;

import static com.example.dexter007bot.LoginActivity.logger;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String WIFI_P2P_TAG = "WifiP2PBR";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Activity activity;
    private WifiP2pManager.PeerListListener peerListListener;
    private P2PNearbyService p2PNearbyService = new P2PNearbyService();
    static final String P2P_CONNECT_TAG = "p2pConnect";

    /**
     * @param manager  WifiP2pManager system service
     * @param channel  Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Activity activity, WifiP2pManager.PeerListListener peerListListener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.peerListListener = peerListListener;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(WIFI_P2P_TAG, action);
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) { return; }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                Log.d(WIFI_P2P_TAG, "Connected to p2p network. Requesting network details");
                manager.requestGroupInfo(channel,p2PNearbyService);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Log.d(WIFI_P2P_TAG, "Device status -" + getDeviceStatus(device.status));
            P2pConnect.DEVICE_STATUS = device.status;
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
//             Call WifiP2pManager.requestPeers() to get a list of current peers
            if (manager != null) {
                Log.d(WIFI_P2P_TAG, "Requesting Peers..");
                manager.requestPeers(channel, peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.v(WIFI_P2P_TAG, "Wifi Direct is Enabled");
                logger.write("Wifi Direct is Enabled");
            } else {
                // Wi-Fi P2P is not enabled
                Log.v(WIFI_P2P_TAG, "Wifi Direct is Disabled");
            }
        } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,0);
            if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
                //Log.d(WIFI_P2P_TAG, "WifiP2P Discovery Started..");
            }else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
                //Log.d(WIFI_P2P_TAG, "WifiP2P Discovery Stopped!!");
            }
        }
    }

    public static String getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}