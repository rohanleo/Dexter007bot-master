package com.example.dexter007bot.P2PConnect;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.example.dexter007bot.Service.P2PConnectService;
import com.example.dexter007bot.Service.P2PNearbyService;

import java.util.List;

import static com.example.dexter007bot.LoginActivity.logger;


public class SearchingDB implements Runnable {
    private Handler handler;
    //private P2PConnectService connectService;
    private P2PNearbyService connectService;
    private int timerDBSearch = 5000;
    public int minDBLevel = 2;
    private String connectedSSID;
    public final static String DISARM_DB_TAG = "Searching_Disarm_DB";
    public static String dbAPName = "DisarmHotspotDB";

    public SearchingDB(Handler handler, P2PNearbyService connectService) {
        this.handler = handler;
        this.connectService = connectService;
        this.handler.post(this);
    }

    @Override
    public void run() {
        connectedSSID = P2PNearbyService.wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        if (connectedSSID.contains(dbAPName)) {
            Log.d(DISARM_DB_TAG, "Connected to DisarmDB");
            List<ScanResult> allScanResults = P2PNearbyService.wifiManager.getScanResults();
            int level = findDBSignalLevel(allScanResults);
            if (level < minDBLevel)
                checkForDB();
        } else {
            boolean connectedToGO = false;
            if (connectService.isPeerDetailsAvailable(connectedSSID) != -1) {
                connectedToGO = true;
            }
            if (connectedToGO) {
                Log.d(DISARM_DB_TAG, "Connected to GO");
            } else {
                Log.d(DISARM_DB_TAG, "Connected to:" + connectedSSID);
            }
            checkForDB();
        }
        P2PNearbyService.wifiManager.startScan();
        handler.postDelayed(this, timerDBSearch);
    }

    private void checkForDB() {
        Log.d(DISARM_DB_TAG, "Searching DB");
        List<ScanResult> allScanResults = P2PNearbyService.wifiManager.getScanResults();
        if (allScanResults.toString().contains(dbAPName)) {
            logger.write("DB found");
            // compare signal level
            int level = findDBSignalLevel(allScanResults);
            if (level < minDBLevel) {
                if (connectedSSID.contains("DB")) {
                    if (P2PNearbyService.wifiManager.disconnect()) {
                        logger.write("DB Disconnected as Level = " + level);
                        Log.d(DISARM_DB_TAG, "DB Disconnected as Level = " + level);
                    }
                } else {
                    logger.write("Not connecting DB low signal");
                    Log.d(DISARM_DB_TAG, "Not connecting DB low signal");
                }
            } else {
                Log.d(DISARM_DB_TAG, "Connecting DisarmDB");
                logger.write("Connecting DB");
                String lastConnected = connectedSSID;
                String ssid = dbAPName;
                WifiConfiguration wc = new WifiConfiguration();
                String pass = "password123";
                wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                //wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wc.preSharedKey = "\"" + pass + "\"";
                wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                //Log.v(DCService.TAG4, "Connected to DB");
                if (P2PNearbyService.wifiManager.pingSupplicant()) {
                    if (!P2PNearbyService.wifiManager.getConnectionInfo().getSSID().contains("DB")) {
                        P2PNearbyService.wifiManager.disconnect();
                        P2PNearbyService.wifiManager.disableNetwork(P2PNearbyService.wifiManager.getConnectionInfo().getNetworkId());
                    }
                }
                int res = P2PNearbyService.wifiManager.addNetwork(wc);
                boolean b = P2PNearbyService.wifiManager.enableNetwork(res, true);
                Log.v("DB:", "Res:" + res + ",b:" + b);
                if (res != -1) {
                    Log.d(DISARM_DB_TAG, " DB Connected");
                    logger.write("Disconnected from: " + lastConnected);
                    logger.write("DB connected");
                } else {
                    Log.d(DISARM_DB_TAG, " DB not Connected");
                    logger.write("DB not connected");
                }
            }
        } else {
            Log.d(DISARM_DB_TAG, "DisarmHotspotDB not found");
        }
    }

    public void stop() {
        handler.removeCallbacks(this);
    }

    public int findDBSignalLevel(List<ScanResult> allScanResults) {
        for (ScanResult scanResult : allScanResults) {
            if (scanResult.SSID.equals(dbAPName)) {
                int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
                Log.d(DISARM_DB_TAG, scanResult.SSID + " Level:" + level);
                return level;
            }
        }
        return 0;
    }
}
