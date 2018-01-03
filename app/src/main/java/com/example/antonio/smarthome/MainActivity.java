package com.example.antonio.smarthome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tview_log;
    Button btn_start, btn_stop;
    UdpServerThread udpServerThread;
    private static final String TAG = "MY";//MainActivity.class.getName();
    static final int UDP_PORT = 48656;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button)findViewById(R.id.srv_start);
        btn_stop = (Button)findViewById(R.id.srv_stop);
        tview_log = (TextView)findViewById(R.id.output);

        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

       // tview_log.setText(getIpAddress()); /// CRASH !!!!!
       // tview_log.setText(Integer.toString(UDP_PORT));

    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.srv_start:
                Log.e(TAG,"Button: SERVER START");
                tview_log.setText(getIpAddress());
                break;
            case R.id.srv_stop:
                Log.e(TAG,"Button: SERVER STOP");
                break;
        }
    }

//    @Override
    protected void onStart() {
        udpServerThread = new UdpServerThread(UDP_PORT);
        udpServerThread.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(udpServerThread != null){
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }
        super.onStop();
    }

    private void updateState(final String state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tview_log.setText(state);
            }
        });
    }

    private void updatePrompt(final String prompt){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tview_log.append(prompt);
            }
        });
    }


    private class UdpServerThread extends Thread {

        int serverPort;
        DatagramSocket socket;

        boolean running;

        public UdpServerThread(int serverPort) {
            super();
            this.serverPort = serverPort;
        }

        public void setRunning(boolean running){
            this.running = running;
        }

        @Override
        public void run() {

            running = true;
            Log.e(TAG, "RUN");
            try {
                updateState("Starting UDP Server IP:" + getIpAddress() + " PORT:" + Integer.toString(this.serverPort) + "\n");

//                socket = new DatagramSocket(serverPort);
                if (socket == null) {
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.setBroadcast(true);
                    socket.bind(new InetSocketAddress(this.serverPort));
                }

                while(running){
                    byte[] buf = new byte[256];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    Log.e(TAG, "BEFORE RECEIVE !!");
                    socket.receive(packet);     //this code block the program flow

                    // send the response to the client at "address" and "port"
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();

                    Log.e(TAG, "RECEIVE PACKET : " + address);
                    String udp_data = new String(buf);
                    updatePrompt("Request from: " + address + ":" + port + " " + udp_data +"\n");

                    String dString = new Date().toString() + "\n"
                            + "Your address " + address.toString() + ":" + String.valueOf(port);
                    buf = dString.getBytes();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);

                }

                Log.e(TAG, "UDP Server ended");

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(socket != null){
                    socket.close();
                    Log.e(TAG, "socket.close()");
                }
            }
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }


}
