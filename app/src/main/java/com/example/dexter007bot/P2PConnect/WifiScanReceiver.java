package com.example.dexter007bot.P2PConnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;

import com.example.dexter007bot.Ip;
import com.example.dexter007bot.MainActivity;
import com.example.dexter007bot.Service.P2PConnectService;
import com.example.dexter007bot.Service.P2PNearbyService;

import org.alicebot.ab.utils.NetworkUtils;

import java.net.InetAddress;
import java.util.logging.Handler;

import static com.example.dexter007bot.MainActivity.TAG;
import static com.example.dexter007bot.P2PConnect.P2pConnect.P2P_CONNECT_TAG;
import static com.example.dexter007bot.Service.P2PNearbyService.myPeerDetails;
import static com.example.dexter007bot.Service.P2PNearbyService.wifiManager;

public class WifiScanReceiver extends BroadcastReceiver {
    //keep broadcasting for some time
    private static final String WIFI_TAG = "WifiScanReceiver";
    private String prevHost = new String();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action =intent.getAction();
        Log.d(WIFI_TAG, action);
        if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Log.d(TAG, "Network State: "+info );
            if(NetworkInfo.State.CONNECTED == info.getState()){
                MainActivity.btnWifi.setBackgroundColor(Color.BLUE);
                WifiInfo info2 = wifiManager.getConnectionInfo();
                if (info2.getSSID() != prevHost){
                    String myIpAddress = Ip.getDottedDecimalIP(Ip.ipadd());
                    String ownerIPAddress= Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
                    Log.d(WIFI_TAG, "myIP:"+myIpAddress+" ownerIP:"+ownerIPAddress);
                    new Thread(new BroadcastThread(ownerIPAddress,myIpAddress,4040)).start();
                }
            }else if(NetworkInfo.State.DISCONNECTED == info.getState()) {
                int color=Color.GRAY;
                if(myPeerDetails.isGroupOwner()) color= Color.GREEN;
                MainActivity.btnWifi.setBackgroundColor(color);
            }
        }
        else if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            P2PNearbyService.wifiScanList = wifiManager.getScanResults();
            String[] wifis = new String[P2PNearbyService.wifiScanList.size()];
            prevHost = new String();
            for (int i = 0; i < P2PNearbyService.wifiScanList.size(); i++) {
                wifis[i] = String.valueOf(P2PNearbyService.wifiScanList.get(i).SSID);
                Log.d(WIFI_TAG, "Available_Networks : "+ wifis[i]);
            }
        }
    }
}
