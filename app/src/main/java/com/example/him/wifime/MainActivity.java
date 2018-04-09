package com.example.him.wifime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    ArrayAdapter<String> wifip2padapter;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        listView=findViewById(R.id.listView);
        wifip2padapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mReceiver=new WiFiDirectBroadcastReceiver(mManager,mChannel,this);
        listView.setAdapter(wifip2padapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    public void displaypeers(WifiP2pDeviceList wifiP2pDeviceList){
        wifip2padapter.clear();
        for(WifiP2pDevice peer:wifiP2pDeviceList.getDeviceList()){
            wifip2padapter.add(peer.deviceName+","+peer.deviceAddress);
        }

    }
    public void discoverPeer(View view){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Searching", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void proceed(InetAddress inetAddress,boolean isHost){
        Intent transfer =new Intent(MainActivity.this,TransferActivity.class);
        transfer.putExtra("HostAddress",inetAddress.getHostAddress());
        transfer.putExtra("IsHost",isHost);
        transfer.putExtra("Connected",true);
        startActivity(transfer);
        Toast.makeText(this, "Host:"+inetAddress.getHostAddress(), Toast.LENGTH_SHORT).show();
    }
}
