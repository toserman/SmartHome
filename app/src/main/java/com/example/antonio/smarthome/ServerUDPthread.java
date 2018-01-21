package com.example.antonio.smarthome;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import static android.content.ContentValues.TAG;

/**
 * Created by toserman on 1/6/18.
 */

public class ServerUDPthread extends Thread {
    private int srv_port;
    private final int UDP_SIZE = 65507;//Max size practical size
    DatagramSocket socket;
    volatile boolean run_flag;
    TextView txt_output;
    Context context;
    Handler hd;

    public ServerUDPthread (int port, Context con, Handler inpHd){
        this.context = con;
        this.srv_port = port;
        this.hd = inpHd;
    }

    public void setRunning(boolean flag) {
        this.run_flag = flag;
    }
    /*Update TextView in main thread*/
    private void updateOutput(final String text) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                txt_output.append(text);
            }
        });
    }

    public void run() {
            try {
                txt_output = ((Activity)context).findViewById(R.id.output);

                if (socket == null) {
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.setBroadcast(true);
                    socket.bind(new InetSocketAddress(this.srv_port));
                }
                    Log.e(TAG, "RUN FLAG = " + Boolean.toString(run_flag) + " PORT: " +srv_port);
                    while(this.run_flag) {
                        byte[] buf = new byte[UDP_SIZE];
                        // receive request
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        Log.e(TAG, "BEFORE RECEIVE !!");
                        socket.receive(packet); //this code block the program flow

                        InetAddress address = packet.getAddress();
                        String strIPaddress = address.getHostAddress();//without '/' at the start

                        int port = packet.getPort();

                        String udp_data = new String(buf,0,packet.getLength());
                        Log.e(TAG,"DATA:" + udp_data);
                        /* For sending message to MainActivity */
                        Message msg = hd.obtainMessage();
                        msg.obj = udp_data;
                        hd.sendMessage(msg);

                        //////////////////////////////
                        if(udp_data.equals("TestPacket")) {
                            int test_port = 48656;
                            Log.e(TAG, " Received TestPacket SEND UDP PACKAGE BACK test_port = "
                                             + Integer.toString(test_port) + " Destination IP " + strIPaddress);

//                            try {
//                                DatagramPacket dp = null;
//                                 DatagramSocket DgrmSocket;
//                                InetAddress IPAddress = InetAddress.getByName("192.168.0.107");
//                                Log.d("MY: ", "Send to " + IPAddress + " Command: " + udp_data );
//                                dp = new DatagramPacket(udp_data.getBytes(),
//                                                         udp_data.length(), IPAddress,test_port);
//
//                                DgrmSocket = new DatagramSocket(null);
//                                DgrmSocket.setReuseAddress(true);
//                                DgrmSocket.setBroadcast(true);
//                                DgrmSocket.bind(new InetSocketAddress(48655));
//
//                                DgrmSocket.send(dp);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            try {
                                new SendUDPdata("192.168.0.107", test_port, "HELLO SERVER").execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            updateOutput("SEND RESPONSE" + "\n");
                            Log.e(TAG, "Send PACKEET end : " + "\n");
                        }
                        //////////////////////////////
                        Log.e(TAG, "RECEIVE PACKET : " + strIPaddress + ":" + port + " " + udp_data);
                        String output = new String("Request from: " + strIPaddress + ":" + port + " " + udp_data);
                        new ActionTask().execute(udp_data);
                        updateOutput(output + "\n");

                    }
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
