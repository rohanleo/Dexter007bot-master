package com.example.dexter007bot.P2PConnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dexter007bot.Service.P2PConnectService;

public class WifiScanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        P2PConnectService.wifiScanList = P2PConnectService.wifiManager.getScanResults();
        String[] wifis = new String[P2PConnectService.wifiScanList.size()];

        for (int i = 0; i < P2PConnectService.wifiScanList.size(); i++) {
            wifis[i] = String.valueOf(P2PConnectService.wifiScanList.get(i).SSID);
            Log.d("Available_Networks : ", wifis[i]);
        }
    }
}
