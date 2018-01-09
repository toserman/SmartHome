package com.example.antonio.smarthome;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
                        Message msg = hd.obtainMessage();
                        msg.obj = udp_data;
                        hd.sendMessage(msg);

                        Log.e(TAG, "RECEIVE PACKET : " + strIPaddress + ":" + port + " " + udp_data);
                        String output = new String("Request from: " + strIPaddress + ":" + port + " " + udp_data);
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
