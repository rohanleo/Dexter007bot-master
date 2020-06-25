package com.example.dexter007bot.Model;

import android.util.Log;

import com.example.dexter007bot.P2PConnect.P2pConnect;

public class PeerDetails {
    private String wifiName;
    private String password;
    private boolean groupOwner;
    private int connectedPeers;

    public PeerDetails() {
        groupOwner = true;
    }

    public PeerDetails(String wifiName, String password) {
        this.wifiName = wifiName;
        this.password = password;
        groupOwner = true;
        connectedPeers = 0;
    }

    public String getWifiName() {
        return wifiName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(boolean groupOwner) {
        this.groupOwner = groupOwner;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectedPeers() {
        return connectedPeers;
    }

    public void setConnectedPeers(int connectedPeers) {
        this.connectedPeers = connectedPeers;
    }

    public static PeerDetails getPeerDetailsObject(String record) {
        if (record == null || record.isEmpty()) {
            Log.e(P2pConnect.P2P_CONNECT_TAG, "Record string is empty");
            return null;
        }
        Log.e(P2pConnect.P2P_CONNECT_TAG, "Record string:" + record);
        String[] recordValues = record.split("::");
        PeerDetails peer = new PeerDetails(recordValues[0],
                recordValues[1]);
        return peer;
    }

    @Override
    public String toString() {
        return wifiName + "::" + password;
    }
}
