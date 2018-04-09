package com.example.him.wifime;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TransferActivity extends AppCompatActivity {
    InetAddress address;
    Intent intent;
    String hostaddress;
    Boolean hostOrNot;
    TextView title,serverPreExecute,received;
    Button send;
    ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        title=findViewById(R.id.titleText);
        serverPreExecute=findViewById(R.id.serverPreExecute);
        received=findViewById(R.id.received);
        send=findViewById(R.id.send);
        intent=getIntent();
        serverThread=new ServerThread();
        if(intent.getBooleanExtra("Connected",false)){
            hostaddress=intent.getStringExtra("HostAddress");
            Toast.makeText(this, "HostAddress: "+hostaddress, Toast.LENGTH_SHORT).show();
            try {
                address=InetAddress.getByName(hostaddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            hostOrNot=intent.getBooleanExtra("IsHost",false);
            Toast.makeText(this, "IsHost:"+hostOrNot, Toast.LENGTH_SHORT).show();
            if(hostOrNot) {
                serverThread.execute();
                title.setText("Host");
                Log.d("Thread", "onCreate: Server started");
            }else {

                title.setText("Client");
                send.setVisibility(View.VISIBLE);
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                    }
                });
                Intent serviceIntent = new Intent(TransferActivity.this, ClientThread.class);
                serviceIntent.setAction(ClientThread.ACTION_SEND_FILE);
                serviceIntent.putExtra(ClientThread.EXTRAS_GROUP_OWNER_ADDRESS, hostaddress);
                serviceIntent.putExtra(ClientThread.EXTRAS_GROUP_OWNER_PORT, 8999);
                TransferActivity.this.startService(serviceIntent);
                Log.d("Thread", "onCreate: Client started");
            }

        }
    }

    public class ServerThread extends AsyncTask<Void,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            serverPreExecute.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            ServerSocket serverSocket=null;
            Socket client=null;
            DataInputStream inputstream = null;
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                serverSocket = new ServerSocket(8999);
                client = serverSocket.accept();


                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                inputstream = new DataInputStream(client.getInputStream());
                String str = inputstream.readUTF();
                serverSocket.close();
                return str;
            } catch (IOException e) {
                Log.e("TAG", e.getMessage());
                return null;
            } finally {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e) {
                        Log.e("TAG", e.getMessage());
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        Log.e("TAG", e.getMessage());
                    }
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e("TAG", e.getMessage());
                    }
                }
            }
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            received.setVisibility(View.VISIBLE);
            if (s != null) {
                Toast.makeText(TransferActivity.this, "Text is :"+s, Toast.LENGTH_SHORT).show();
                received.setText(s);
            }else{
                Toast.makeText(TransferActivity.this, "No data transferred", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static class ClientThread extends IntentService {
        private static final int SOCKET_TIMEOUT = 5000;
        public static final String ACTION_SEND_FILE = "com.example.him.wifime.SEND_FILE";
        public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
        public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         *
         */

        public ClientThread() {
            super("ClientThread");
        }
        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            Context context = getApplicationContext();
            if (intent.getAction().equals(ACTION_SEND_FILE)) {
                String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
                Toast.makeText(context, "host:"+host, Toast.LENGTH_SHORT).show();
                Log.d("IntentService", "onHandleIntent: "+"inside service");
                Socket socket = new Socket();
                int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
                Toast.makeText(context, "Port:"+port, Toast.LENGTH_SHORT).show();
                DataOutputStream stream = null;
                try {
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                    stream = new DataOutputStream(socket.getOutputStream());
                    stream.writeUTF("data");
                    stream.flush();
                    Toast.makeText(context, "Data written", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("TAG", e.getMessage());
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }
    }
}
