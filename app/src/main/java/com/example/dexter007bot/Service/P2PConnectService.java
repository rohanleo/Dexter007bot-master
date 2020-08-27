package com.example.dexter007bot.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.dexter007bot.Ip;
import com.example.dexter007bot.MainActivity;
import com.example.dexter007bot.Model.PeerDetails;
import com.example.dexter007bot.P2PConnect.BatteryBroadcastReceiver;
import com.example.dexter007bot.P2PConnect.BroadcastThread;
import com.example.dexter007bot.P2PConnect.ListenThread;
import com.example.dexter007bot.P2PConnect.P2pConnect;
import com.example.dexter007bot.P2PConnect.SearchingDB;
import com.example.dexter007bot.P2PConnect.WifiDirectBroadcastReceiver;
import com.example.dexter007bot.P2PConnect.WifiScanReceiver;
import com.example.dexter007bot.P2PConnect.client;
import com.example.dexter007bot.R;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import io.chirp.connect.ChirpConnect;
import io.chirp.connect.interfaces.ConnectEventListener;
import io.chirp.connect.models.ChirpError;

public class P2PConnectService extends Service implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.GroupInfoListener {
    public List<PeerDetails> peerDetailsList;
    public List<WifiP2pDevice> p2pDevicesList;
    public PeerDetails myPeerDetails;
    public P2pConnect p2pConnect;
    public SearchingDB searchingDB;


    public boolean sendingData;
    public boolean receivingData;
    private ChirpConnect chirpConnect;
    public int decodeFailed;
    public static final int DECODE_FAILED_THRESHOLD = 3;

    public HandlerThread runThread;
    public Handler runHandler;
    public Handler p2pConnectHandler;
    public Handler peerUpdateHandler;
    public Handler discoveryUpdateHandler;
    public Handler searchingDisarmDBHandler;

    public static final String DEBUG_TAG = "mainConnect";
    public static final String CHIRP_TAG = "Chirp_Connect";
    public static final String GO_DETAIL_BROAD_TAG = "send_go_detail";

    //wifi
    public boolean wifiState;
    public static WifiManager wifiManager;
    public static List<ScanResult> wifiScanList;
    public WifiScanReceiver wifiReceiver;
    public IntentFilter wifiIntentFilter;

    //p2p
    public IntentFilter mIntentFilter;
    public WifiP2pManager mManager;
    public WifiP2pManager.Channel mChannel;
    public BroadcastReceiver mReceiver;

    //battery Logger
    IntentFilter batteryFilter;
    BroadcastReceiver batteryBroadcastReceiver;

    //Broadcast Go Detail
    public HandlerThread sendGODetailThread;
    public Handler sendGODetailHandler;
    private final IBinder p2pServiceBinder = new P2PConnectServiceBinder();

    private String macAddress;
    private MainActivity activity;

    public P2PConnectService(MainActivity activity) {
        this.activity = activity;
    }

    public P2PConnectService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return p2pServiceBinder;
    }

    public class P2PConnectServiceBinder extends Binder {
        public P2PConnectService getService() {
            // Return this instance of SyncService so activity can call public methods
            return P2PConnectService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(activity, "P2pConnectService Started..", Toast.LENGTH_SHORT).show();
        p2pDevicesList = new ArrayList<>();
        peerDetailsList = new ArrayList<>();
        myPeerDetails = new PeerDetails();
        //initialize views
        init();

        //switch wifi on
        //Initialize wifi
        wifiInit();

        //Initialize wifi direct
        wifiP2pInit();

        //start chirp
        chirpInit();

        //set volume as max
        setMaxVolume();

        //initialize battery logger
        batteryLoggerInit();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //start p2pConnect
        if(p2pConnect == null){
            Log.d(P2pConnect.P2P_CONNECT_TAG, "p2pConnect Started...");
            p2pConnectHandler = new Handler();
            //p2pConnect = new P2pConnect(p2pConnectHandler, this, WifiP2pDevice.UNAVAILABLE);
        }

        //start searchingDisarmDB
        if (searchingDB == null) {
            Log.d(SearchingDB.DISARM_DB_TAG, "Searching DisarmDB Started...");
            searchingDisarmDBHandler = new Handler();
            //searchingDB = new SearchingDB(searchingDisarmDBHandler, this);
        }

        //handler to update list adapter and ui views
        peerUpdateHandler = new Handler();
        peerUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                onUpdateStatus();
                peerUpdateHandler.postDelayed(this,3000);
            }
        });

        //update view
        mManager.requestConnectionInfo(mChannel,this);

        discoveryUpdateHandler = new Handler();
        discoveryUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                startDiscovery();
                updatePeerDetailsList();
                discoveryUpdateHandler.postDelayed(this,120000);
            }
        });

        //start GO details Broadcast
        startGOPeerDetailBroadcast();
        return START_STICKY;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        String myIpAddress = "";
        String ownerIPAddress = "";
        try {
            myIpAddress = Ip.getDottedDecimalIP(Ip.ipadd());
            ownerIPAddress = Ip.getDottedDecimalIP(wifiP2pInfo.groupOwnerAddress.getAddress());
        } catch (Exception ignored) {
        }
        Log.d(P2pConnect.P2P_CONNECT_TAG, "group formed:" + String.valueOf(wifiP2pInfo.groupFormed) + " My Ip assigned:" + myIpAddress +
                " Owner I/P:" + ownerIPAddress + " I'm owner:" + wifiP2pInfo.isGroupOwner);

        if (wifiP2pInfo.isGroupOwner) {
            myPeerDetails.setGroupOwner(true);
            //new Thread(new ListenThread()).start();
        } else {
            myPeerDetails.setGroupOwner(false);
            myPeerDetails.setWifiName("");
            myPeerDetails.setPassword("");
            myPeerDetails.setConnectedPeers(0);

            new Thread(new BroadcastThread(ownerIPAddress,myIpAddress,4040)).start();
            new Thread(new client(ownerIPAddress)).start();
        }

        mManager.requestGroupInfo(mChannel,this);
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
    String groupName = "";
        try {
            String[] names = wifiP2pGroup.getNetworkName().split("-");
            groupName = names[names.length - 1];
            if (wifiP2pGroup.isGroupOwner()) {
                myPeerDetails.setWifiName(wifiP2pGroup.getNetworkName());
                myPeerDetails.setPassword(wifiP2pGroup.getPassphrase());
                myPeerDetails.setConnectedPeers(wifiP2pGroup.getClientList().size());
                Log.d("GOTest", "SSID:" + wifiP2pGroup.getNetworkName());
                Log.d("GOTest", "Pass:" + wifiP2pGroup.getPassphrase());
            }
        } catch (Exception ignored) {
        }
        Log.d("XOB", "MY device:" + myPeerDetails.toString());
    }

    private void init() {
        setMacAddress();
    }

    private void setMacAddress() {
        macAddress = "";
        macAddress = Ip.getMacAddr();
        final HandlerThread htd = new HandlerThread("MacAdd");
        htd.start();
        final Handler h = new Handler(htd.getLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                macAddress = Ip.getMacAddr();
                if (macAddress.equals("")) {
                    Log.d(DEBUG_TAG, "No mac address detected");
                    h.postDelayed(this, 1000);
                } else {
                    htd.quit();
                }
            }
        });
    }

    private void wifiInit(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiState = wifiManager.isWifiEnabled();
        if(!wifiState){
            wifiManager.setWifiEnabled(true);
        }
        wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiReceiver = new WifiScanReceiver();
        registerReceiver(wifiReceiver,wifiIntentFilter);
        wifiScanList = new ArrayList<>();
        wifiManager.startScan();
    }

    WifiP2pManager.PeerListListener myPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (peerList.getDeviceList().isEmpty()) {
                Log.d(DEBUG_TAG, "No device found");
                startDiscovery();
            } else {
                Log.d(DEBUG_TAG, "Peers Found");
                Collection<WifiP2pDevice> peerDevices = peerList.getDeviceList();
                p2pDevicesList.clear();
                p2pDevicesList.addAll(peerDevices);
            }
        }
    };

    private void wifiP2pInit(){
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, activity, myPeerListListener);
        registerReceiver(mReceiver,mIntentFilter);
    }

    private void batteryLoggerInit(){
        batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryBroadcastReceiver = new BatteryBroadcastReceiver();
        registerReceiver(batteryBroadcastReceiver,batteryFilter);
    }

    private void updatePeerDetailsList() {
        ArrayList<PeerDetails> newPeerList = new ArrayList<>();
        for (PeerDetails peerDetails : peerDetailsList) {
            for (ScanResult result : wifiScanList) {
                Log.d("Available_Networks", peerDetails.getWifiName() + " compared to:" + String.valueOf(result.SSID));
                if (peerDetails.getWifiName().equalsIgnoreCase(String.valueOf(result.SSID))) {
                    newPeerList.add(peerDetails);
                    Log.d("Available_Networks", "Both are equal");
                }
            }
        }
        peerDetailsList.clear();
        peerDetailsList.addAll(newPeerList);
    }

    private void startDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(P2pConnect.P2P_CONNECT_TAG, "Discovery started");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(P2pConnect.P2P_CONNECT_TAG, "Discovery Failed");
            }
        });
    }

    private void startBroadcastingPeerDetails() {
        runThread = new HandlerThread("Run");
        runThread.start();
        runHandler = new Handler(runThread.getLooper());
        runHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(CHIRP_TAG, "Sending Broadcast...");
                String peerDetailsString = myPeerDetails.toString();
                if (sendingData)
                    Log.d(GO_DETAIL_BROAD_TAG, "Cant send now..Sending now!!");
                else
                    sendPeerDetails(peerDetailsString);
                //update peer details
                onUpdateStatus();
                int delay = getRandomNumberInRange(4, 8) * 1000;
                Log.d(CHIRP_TAG, "The random delay is:" + delay / 1000 + " seconds");
                runHandler.postDelayed(this, delay);
            }
        }, 5000);
    }

    private void stopBroadcastingPeerDetails() {
        if (runHandler != null)
            runHandler.removeCallbacksAndMessages(null);
        if (runThread != null)
            runThread.quit();
    }

    private void startGOPeerDetailBroadcast() {
        sendGODetailThread = new HandlerThread("Send_GO_DETAIL");
        sendGODetailThread.start();
        sendGODetailHandler = new Handler(sendGODetailThread.getLooper());
        sendGODetailHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(GO_DETAIL_BROAD_TAG, "Sending GO details...");
                //check if I'm connected to GO
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssidName = wifiInfo.getSSID().replace("\"", "");
                int connectedPeerIndex = isPeerDetailsAvailable(ssidName);
                if (connectedPeerIndex != -1 && !myPeerDetails.isGroupOwner()) {
                    String GODetailsString = peerDetailsList.get(connectedPeerIndex).toString();
                    Log.d(GO_DETAIL_BROAD_TAG, "Connected To GO:" + GODetailsString);
                    if (sendingData)
                        Log.d(GO_DETAIL_BROAD_TAG, "Cant send now..Sending now!!");
                    else
                        sendPeerDetails(GODetailsString);
                } else {
                    Log.d(GO_DETAIL_BROAD_TAG, "Not Connected To GO or I'm a GO");
                }
                int delay = getRandomNumberInRange(4, 8) * 1000;
                Log.d(CHIRP_TAG, "The random delay is:" + delay / 1000 + " seconds");
                sendGODetailHandler.postDelayed(this, delay);
            }
        }, 5000);
    }

    private void stopGOPeerDetailBroadcast() {
        if (sendGODetailHandler != null)
            sendGODetailHandler.removeCallbacksAndMessages(null);
        if (sendGODetailThread != null)
            sendGODetailThread.quit();
    }

    public void createGroup() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(P2pConnect.P2P_CONNECT_TAG, "Group created");
                //method to start handler for broadcasting my peerDetails
                //broadcast only if Im GO
                startBroadcastingPeerDetails();
                onUpdateStatus();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(P2pConnect.P2P_CONNECT_TAG, "Failed to create a group");
            }
        });
    }

    public void removeGroup() {
        if (mManager != null && mChannel != null) {
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.d(P2pConnect.P2P_CONNECT_TAG, "Removing group failed for Reason: " + reasonCode);
                }

                @Override
                public void onSuccess() {
                    Log.d(P2pConnect.P2P_CONNECT_TAG, "Removed from group successfully!!");
                }
            });
        }
        //method to stop handler for broadcasting my peerDetails
        stopBroadcastingPeerDetails();
        onUpdateStatus();
    }

    private void sendPeerDetails(String peerDetailsString) {
        Log.d(CHIRP_TAG, "Payload String:" + peerDetailsString);
        byte[] payload = peerDetailsString.getBytes(Charset.forName("UTF-8"));
        long maxSize = chirpConnect.maxPayloadLength();
        if (maxSize < payload.length) {
            Log.d(CHIRP_TAG, "Invalid Payload");
            Log.d(CHIRP_TAG, "Allowed Payload:" + maxSize + " Payload length:" + payload.length);
            return;
        }
        if (receivingData) {
            Log.d(CHIRP_TAG, "Can't Send, Receiving Data Now");
        } else {
            ChirpError error = chirpConnect.send(payload);
            if (error.getCode() > 0) {
                Log.d(CHIRP_TAG, "Error: " + error.getMessage());
            } else
                Log.d(CHIRP_TAG, "Data sent successfully!!");
        }
    }

    //check if peer device is available(has our app)
    public int isPeerDetailsAvailable(String deviceAddress) {
        if (peerDetailsList.isEmpty())
            return -1;
        int i = 0;
        for (PeerDetails peer : peerDetailsList) {
            if (peer.getWifiName().equals(deviceAddress)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void onUpdateStatus() {
        mManager.requestConnectionInfo(mChannel, this);
    }

    public void connectWifi(PeerDetails peerDetails) {
        WifiInfo info = wifiManager.getConnectionInfo();
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + peerDetails.getWifiName() + "\"";
        wc.preSharedKey = "\"" + peerDetails.getPassword() + "\"";
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        if (wifiManager.pingSupplicant()) {
            wifiManager.disconnect();
            wifiManager.disableNetwork(info.getNetworkId());
        }
        int res = wifiManager.addNetwork(wc);
        boolean b = wifiManager.enableNetwork(res, true);

    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        if (p2pConnect != null)
            p2pConnect.stop();
        if (searchingDB != null)
            searchingDB.stop();
        stopBroadcastingPeerDetails();
        stopGOPeerDetailBroadcast();
        discoveryUpdateHandler.removeCallbacksAndMessages(null);
        if (peerUpdateHandler != null)
            peerUpdateHandler.removeCallbacksAndMessages(null);
        mManager.stopPeerDiscovery(mChannel, null);
        unregisterReceiver(mReceiver);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(batteryBroadcastReceiver);
        removeGroup();
        stopChirpSdk();
        try {
            chirpConnect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    //CHIRP
    ConnectEventListener connectEventListener = new ConnectEventListener() {

        @Override
        public void onSending(byte[] data, int channel) {
            /**
             * onSending is called when a send event begins.
             * The data argument contains the payload being sent.
             */
            sendingData = true;
            String hexData = "null";
            if (data != null) {
                hexData = bytesToHex(data);
            }
            Log.d(CHIRP_TAG, "ConnectCallback: onSending: " + hexData + " on channel: " + channel);
        }

        @Override
        public void onSent(byte[] data, int channel) {
            /**
             * onSent is called when a send event has completed.
             * The data argument contains the payload that was sent.
             */
            String hexData = "null";
            if (data != null) {
                hexData = bytesToHex(data);
            }
            sendingData = false;
            Log.d(CHIRP_TAG, "ConnectCallback: onSent: " + hexData + " on channel: " + channel);
        }

        @Override
        public void onReceiving(int channel) {
            /**
             * onReceiving is called when a receive event begins.
             * No data has yet been received.
             */
            receivingData = true;
            Log.d(CHIRP_TAG, "ConnectCallback: onReceiving on channel: " + channel);
        }

        @Override
        public void onReceived(byte[] data, int channel) {
            /**
             * onReceived is called when a receive event has completed.
             * If the payload was decoded successfully, it is passed in data.
             * Otherwise, data is null.
             */
            if (data != null) {
                decodeFailed = 0;
                String identifier = new String(data);
                Log.d(CHIRP_TAG, "Received:" + identifier);
                PeerDetails newPeer = PeerDetails.getPeerDetailsObject(identifier);
                Log.d(CHIRP_TAG, "WifiName:" + newPeer.getWifiName());
                if (isPeerDetailsAvailable(newPeer.getWifiName()) == -1) {
                    peerDetailsList.add(newPeer);
                }
            } else {
                decodeFailed++;
                Log.d(CHIRP_TAG, "Decode failed:" + decodeFailed);
                if (decodeFailed == DECODE_FAILED_THRESHOLD) {
                    //if decode fails thrice in a row then restart chirp
                    stopChirpSdk();
                    chirpInit();
                }
            }
            receivingData = false;
        }

        @Override
        public void onStateChanged(int oldState, int newState) {
            /**
             * onStateChanged is called when the SDK changes state.
             */
            Log.v(CHIRP_TAG, "ConnectCallback: onStateChanged " + oldState + " -> " + newState);
        }

        @Override
        public void onSystemVolumeChanged(int oldVolume, int newVolume) {
            Log.v(CHIRP_TAG, "System volume has been changed, notify user to increase the volume when sending data");
        }
    };

    private void chirpInit(){
        //ultrasonic poly
        chirpConnect = new ChirpConnect(this, getResources().getString(R.string.CHIRP_APP_KEY), getResources().getString(R.string.CHIRP_APP_SECRET));
        ChirpError error = chirpConnect.setConfig(getResources().getString(R.string.CHIRP_APP_CONFIG));
        if (error.getCode() == 0) {
            Log.v("ChirpSDK: ", "Configured ChirpSDK");
        } else {
            Log.e("ChirpError: ", error.getMessage());
        }
        decodeFailed = 0;
        chirpConnect.setListener(connectEventListener);
        receivingData = false;
        sendingData = false;
        startChirpSdk();
    }

    public void startChirpSdk() {
        ChirpError error = chirpConnect.start();
        if (error.getCode() > 0) {
            Log.d(CHIRP_TAG, "Error starting:" + error.getMessage());
            return;
        }
        Log.d(CHIRP_TAG, "Chirp Started");
    }

    public void stopChirpSdk() {
        ChirpError error = chirpConnect.stop();
        if (error.getCode() > 0) {
            Log.d(CHIRP_TAG, "Error stopping:" + error.getMessage());
            return;
        }
        Log.d(CHIRP_TAG, "Chirp Stopped");
    }

    private void setMaxVolume() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
    }
}
