package com.example.him.wifime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by him on 3/11/2018.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    List<WifiP2pDevice> deviceLists;
    List<WifiP2pConfig> configLIst;
    WifiP2pDevice wifiP2pDevice;
    WifiP2pManager.PeerListListener myPeerListListener;
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mActivity, "Wifi direct enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity, "Wifi direct not enabled", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                deviceLists=new ArrayList<>();
                configLIst=new ArrayList<>();
                mManager.requestPeers(mChannel, myPeerListListener);
                myPeerListListener=new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        int i;
                        deviceLists.clear();
                        deviceLists.addAll(peers.getDeviceList());
                        mActivity.displaypeers(peers);
                        deviceLists.addAll(peers.getDeviceList());
                        Toast.makeText(mActivity, "Count devicelist"+peers.getDeviceList().size(), Toast.LENGTH_SHORT).show();
                        for(i=0;i<peers.getDeviceList().size();i++){
                            WifiP2pConfig wifiP2pConfig=new WifiP2pConfig();
                            wifiP2pConfig.deviceAddress=deviceLists.get(i).deviceAddress;
                            configLIst.add(wifiP2pConfig);
                            connect(i);
                        }
                        Toast.makeText(mActivity, "Count peers "+deviceLists.size(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(mActivity, "Config count "+configLIst.size(), Toast.LENGTH_SHORT).show();
                    }
                };
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }


    public void connect(int pos){

        WifiP2pConfig wifiP2pConfig=configLIst.get(pos);
        wifiP2pDevice=deviceLists.get(pos);

        mManager.connect(mChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT).show();
                transfer();

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(mActivity, "Not connected "+reason, Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void transfer(){
        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                InetAddress groupOwnerAddress=info.groupOwnerAddress;
                if(info.groupFormed){
                    if(info.isGroupOwner){
                        Toast.makeText(mActivity, "Group owner:"+groupOwnerAddress, Toast.LENGTH_SHORT).show();
                        mActivity.proceed(groupOwnerAddress,true);

                    }
                    if(!info.isGroupOwner){
                        Toast.makeText(mActivity, "Not Group owner:"+groupOwnerAddress, Toast.LENGTH_SHORT).show();
                        mActivity.proceed(groupOwnerAddress,false);
                    }
                }
            }
        });

    }
}