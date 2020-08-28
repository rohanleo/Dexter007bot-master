package com.example.dexter007bot.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dexter007bot.Ip;
import com.example.dexter007bot.MainActivity;
import com.example.dexter007bot.Model.PeerDetails;
import com.example.dexter007bot.P2PConnect.BatteryBroadcastReceiver;
import com.example.dexter007bot.P2PConnect.BroadcastThread;
import com.example.dexter007bot.P2PConnect.ListenThread;
import com.example.dexter007bot.P2PConnect.P2pConnect;
import com.example.dexter007bot.P2PConnect.SearchingDB;
import com.example.dexter007bot.P2PConnect.WebServer;
import com.example.dexter007bot.P2PConnect.WifiDirectBroadcastReceiver;
import com.example.dexter007bot.P2PConnect.WifiScanReceiver;
import com.example.dexter007bot.P2PConnect.client;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.example.dexter007bot.LoginActivity.logger;
import static com.example.dexter007bot.MainActivity.TAG;
import static com.example.dexter007bot.Service.P2PConnectService.DEBUG_TAG;
import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class P2PNearbyService extends Service implements WifiP2pManager.GroupInfoListener{

    private static final String NEARBY_SERVICE_TAG = "NEARBY";
    public List<PeerDetails> peerDetailsList;
    public List<WifiP2pDevice> p2pDevicesList;
    public static PeerDetails myPeerDetails;
    public P2pConnect p2pConnect;

    //wifi
    public boolean wifiState;
    public static WifiManager wifiManager;
    public static List<ScanResult> wifiScanList;
    public WifiScanReceiver wifiReceiver;
    public IntentFilter wifiIntentFilter;

    //Nearby
    List<String> connectedPeers, echoingPeers;
    private String nickName;
    private String serviceID;
    private Strategy strategy = Strategy.P2P_CLUSTER;
    private Handler nearby_handler;
    DiscoveryOptions discoveryOptions;
    AdvertisingOptions advertisingOptions;
    ConnectionsClient nearbyClient;
    private static final int requestedRefresh = 900000;

    //p2p
    public IntentFilter mIntentFilter;
    public WifiP2pManager mManager;
    public WifiP2pManager.Channel mChannel;
    public BroadcastReceiver mReceiver;

    //battery Logger
    IntentFilter batteryFilter;
    BroadcastReceiver batteryBroadcastReceiver;

    private final IBinder p2PNearbyServiceBinder = new P2PNearbyServiceBinder();
    private String macAddress;
    private MainActivity mActivity;
    private SearchingDB searchingDB;

    //Handlers
    private Handler p2pConnectHandler;
    //public Handler peerUpdateHandler;
    public Handler discoveryUpdateHandler;
    public Handler searchingDisarmDBHandler;

    ListenThread listenThread;
    WebServer webServer;

    public P2PNearbyService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return p2PNearbyServiceBinder;
    }

    public class P2PNearbyServiceBinder extends Binder {
        public P2PNearbyService getService() {
            // Return this instance of SyncService so activity can call public methods
            return P2PNearbyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "P2PNearbyService Started..", Toast.LENGTH_SHORT).show();
        p2pDevicesList = new ArrayList<>();
        peerDetailsList = new ArrayList<>();
        myPeerDetails = new PeerDetails();
        connectedPeers= new ArrayList<>();
        echoingPeers = new ArrayList<>();
        setMacAddress();
        wifiInit();
        wifiP2pInit();
        batteryLoggerInit();
        nearbyInit();
        startServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(p2pConnect == null){
            Log.d(P2pConnect.P2P_CONNECT_TAG, "p2pConnect Started...");
            p2pConnectHandler = new Handler();
            p2pConnect = new P2pConnect(p2pConnectHandler, this, WifiP2pDevice.UNAVAILABLE);
        }
        if (searchingDB == null) {
            Log.d(SearchingDB.DISARM_DB_TAG, "Searching DisarmDB Started...");
            searchingDisarmDBHandler = new Handler();
            searchingDB = new SearchingDB(searchingDisarmDBHandler, this);
        }
        nearby_handler= new Handler();
        startBroadcasting();
        nearby_handler.post(new Runnable() {
            @Override
            public void run() {
                nearbyClient.startDiscovery(serviceID,endpointDiscoveryCallback,discoveryOptions);
                nearby_handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nearbyClient.stopDiscovery();
                    }
                },3000);
                nearby_handler.postDelayed(this,15000);
            }
        });
        nearby_handler.post(new Runnable() {
            @Override
            public void run() {
                echoingPeers = new ArrayList<>();
                peerDetailsList = new ArrayList<>();
                nearby_handler.postDelayed(this,requestedRefresh);
            }
        });
        discoveryUpdateHandler = new Handler();
        discoveryUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                startDiscovery();
                updatePeerDetailsList();
                discoveryUpdateHandler.postDelayed(this,120000);
            }
        });
        return START_STICKY;
    }

    public void onUpdateStatus(){
        mManager.requestGroupInfo(mChannel,this);
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
    private void startServer(){
        webServer = new WebServer(getApplicationContext());
        try {
            webServer.start();
            Log.d(NEARBY_SERVICE_TAG,"Server of this device has started");
            logger.write("Server of this device has started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void wifiInit(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiState = wifiManager.isWifiEnabled();
        if(!wifiState){
            wifiManager.setWifiEnabled(true);
        }
        wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiReceiver = new WifiScanReceiver();
        registerReceiver(wifiReceiver,wifiIntentFilter);
        wifiScanList = new ArrayList<>();
        wifiManager.startScan();
    }

    private void wifiP2pInit(){
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, mActivity, myPeerListListener);
        registerReceiver(mReceiver,mIntentFilter);
    }

    WifiP2pManager.PeerListListener myPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (peerList.getDeviceList().isEmpty()) {
                Log.d(DEBUG_TAG, "No device found");
            } else {
                Log.d(DEBUG_TAG, "Peers Found");
                Collection<WifiP2pDevice> peerDevices = peerList.getDeviceList();
                p2pDevicesList.clear();
                p2pDevicesList.addAll(peerDevices);
            }
        }
    };

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

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
        if(wifiP2pGroup==null) return;
        //String groupName = "";
        //String[] names = wifiP2pGroup.getNetworkName().split("-");
        //groupName = names[names.length - 1];
        Log.d(TAG, "IP: "+Ip.getDottedDecimalIP(Ip.ipadd()));
            if (wifiP2pGroup.isGroupOwner()) {
                myPeerDetails.setGroupOwner(true);
                myPeerDetails.setWifiName(wifiP2pGroup.getNetworkName());
                myPeerDetails.setPassword(wifiP2pGroup.getPassphrase());
                myPeerDetails.setConnectedPeers(wifiP2pGroup.getClientList().size());
                Log.d("GOTest", "SSID:" + wifiP2pGroup.getNetworkName());
                Log.d("GOTest", "Pass:" + wifiP2pGroup.getPassphrase());

                //sendPeerDetails(myPeerDetails.toString());
            }
            Log.d("XOB", "MY device:" + myPeerDetails.toString());
    }

    public void createGroup() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(P2pConnect.P2P_CONNECT_TAG, "Group created");
                //method to start handler for broadcasting my peerDetails
                //broadcast only if Im GO
                //sendPeerDetails();
                try {
                    listenThread = new ListenThread();
                    listenThread.start();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                MainActivity.btnWifi.setBackgroundColor(Color.GREEN);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(P2pConnect.P2P_CONNECT_TAG, "Failed to create a group");
                onUpdateStatus();
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
                    myPeerDetails= new PeerDetails();
                    if(listenThread!=null){
                        if(listenThread.isAlive()){
                            listenThread.closeThread();
                            //listenThread.interrupt();
                        }
                    }
                    int color= Color.BLUE;
                    if(wifiManager.getConnectionInfo() == null) color= Color.GRAY;
                    MainActivity.btnWifi.setBackgroundColor(color);
                }
            });
        }
        //method to stop handler for broadcasting my peerDetails to be added
        //onUpdateStatus();
    }

    private void startBroadcasting () {
        nearby_handler.post(new Runnable() {
            @Override
            public void run() {
                if(myPeerDetails.isGroupOwner()) {
                    Log.d(TAG, "Broadcasting: "+myPeerDetails.toString());
                    pingAll(myPeerDetails.toString(),null);
                }
                nearby_handler.postDelayed(this,5000);
            }
        });
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

    private void batteryLoggerInit(){
        batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryBroadcastReceiver = new BatteryBroadcastReceiver();
        registerReceiver(batteryBroadcastReceiver,batteryFilter);
    }

    private void nearbyInit(){
        serviceID = getPackageName();
        nickName = UUID.randomUUID().toString();
        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(strategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(strategy).build();
        nearbyClient = Nearby.getConnectionsClient(getApplicationContext());
        nearbyClient.startAdvertising(nickName,serviceID,connectionLifecycleCallback,advertisingOptions);
        Log.d(TAG, "nearbyInit: NEARBY Started");
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endPointID, ConnectionInfo connectionInfo) {
            if(!connectedPeers.contains(endPointID) && connectedPeers.size()<4)
                Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endPointID, payloadCallback);
        }
        @Override
        public void onConnectionResult(String endPointID, ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()){
                case ConnectionsStatusCodes.STATUS_OK:
                    pingAll("ECHO"+"_"+endPointID+"_"+nickName, NULL);
                    connectedPeers.add(endPointID);
                    Log.d(NEARBY_SERVICE_TAG, "NearbyConnected:"+endPointID);
                    break;

                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    //CONNECTION REJECTED
                    //logs_adapter.add("REJECTED: "+endPointID);
                    break;

                case ConnectionsStatusCodes.STATUS_ERROR:
                    //CONNECTION ERROR
                    //logs_adapter.add("ERROR: "+endPointID);
                    break;

                default:
            }
        }

        @Override
        public void onDisconnected(String endPointID) {
            connectedPeers.remove(endPointID);
            Log.d(NEARBY_SERVICE_TAG, "onDisconnected: "+endPointID);
        }
    };

    private void pingAll(String message,String endpointId){
        for(String s:connectedPeers)
            if(!s.equals(endpointId))
                sendPayload(s, message);
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(final String endPointID, DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d(NEARBY_SERVICE_TAG, "NearbyEndpointFound:"+endPointID);
            if(!echoingPeers.contains(endPointID) && !connectedPeers.contains(endPointID) && connectedPeers.size()<4) {
                Nearby.getConnectionsClient(getApplicationContext()).requestConnection(nickName, endPointID, connectionLifecycleCallback);
            }
        }

        @Override
        public void onEndpointLost(String endPointID) {
        }
    };

    private void sendPayload(final String endPointID, final String message) {
        Payload mPayload = Payload.fromBytes(message.getBytes());
        Nearby.getConnectionsClient(this).sendPayload(endPointID,mPayload)
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sendPayload(endPointID,message);
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Sent:"+message);
            }
        });
    }
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(final String endPointID, Payload payload) {
            final byte[] received = payload.asBytes();
            String receivedMsg = new String(received);
            if(receivedMsg.contains("ECHO")) {
                Log.d(TAG, "onPayloadReceived: ECHO"+endPointID);
                if(receivedMsg.contains(nickName)) {
                    nearbyClient.disconnectFromEndpoint(endPointID);
                    echoingPeers.add(endPointID);
                } else{
                    pingAll(receivedMsg,endPointID);
                }
            }
            else{
                Log.d(TAG, "Received: PeerDetails:"+receivedMsg);
                PeerDetails newPeer = PeerDetails.getPeerDetailsObject(receivedMsg);
                Log.d(NEARBY_SERVICE_TAG, "WifiName:" + newPeer.getWifiName());
                if (isPeerDetailsAvailable(newPeer.getWifiName()) == -1 && !peerDetailsList.contains(newPeer)) {
                    peerDetailsList.add(newPeer);
                }
                pingAll(receivedMsg, endPointID);
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endPointID, PayloadTransferUpdate payloadTransferUpdate) {
            if(payloadTransferUpdate.getStatus()==PayloadTransferUpdate.Status.SUCCESS){
            }
        }
    };

    private void stopNearby(){
        nearbyClient.stopAdvertising();
        nearbyClient.stopAllEndpoints();
        nearbyClient.stopDiscovery();
    }


    @Override
    public void onDestroy() {
        if (p2pConnect != null)
            p2pConnect.stop();
        if (searchingDB != null)
            searchingDB.stop();
        discoveryUpdateHandler.removeCallbacksAndMessages(null);
//        if (peerUpdateHandler != null)
//            peerUpdateHandler.removeCallbacksAndMessages(null);
        mManager.stopPeerDiscovery(mChannel, null);
        nearby_handler.removeCallbacksAndMessages(null);
        unregisterReceiver(mReceiver);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(batteryBroadcastReceiver);
        removeGroup();
        stopNearby();
        webServer.stop();
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }
}
