package com.example.antonio.smarthome;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringDef;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import static android.content.ContentValues.TAG;
import static java.lang.annotation.RetentionPolicy.SOURCE;

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

    //TODO: MOVE ENUM to MainActivity
    @Retention(SOURCE)
    @StringDef({
            TURN_ON,
            TURN_OFF,
            TEST
    })
    public @interface CommandName {};
    public static final String TURN_ON = "TurnOn";
    public static final String TURN_OFF = "TurnOff";
    public static final String TEST = "TestPacket";

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
                    Log.e(TAG, "RUN FLAG = " + Boolean.toString(run_flag) + " PORT: " + srv_port);
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

                        Log.e(TAG, "RECEIVE PACKET : " + strIPaddress + ":" + port + " " + udp_data);
                        String output = "Request from: " + strIPaddress + ":" + port + " Data:" + udp_data;
                        updateOutput(output + "\n");//Update TextView in UI
                        //////////////////////////////
//                        if(udp_data.equals("TestPacket")) {
//                            int test_port = 48656;
//                            String sendMsg = " ";
//                            Log.e(TAG, " Received TestPacket SEND UDP PACKAGE BACK test_port = "
//                                             + Integer.toString(test_port) + " Destination IP " + strIPaddress);
//
//                            sendMsg = CheckDeviceAvailability(strIPaddress); //Check connection
//                            //String HOME_PC_IP= "192.168.0.102"; //Home PC
//                           // sendMsg = CheckDeviceAvailability(HOME_PC_IP); //Check connection
//
//                            try {
//                                //new SendUDPdata("192.168.0.107", test_port, "HELLO SERVER").execute();
//                                new SendUDPdata(strIPaddress, test_port, sendMsg).execute();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            updateOutput("Send Response to " + strIPaddress + ":" + port + " " + sendMsg + "\n");
//                        }
//                                                /* Handle request*/
//                        new ActionTask().execute(udp_data);

                        String strStatus, strAsynCommand;
                        Log.e(TAG, " Received TestPacket SEND UDP PACKAGE BACK test_port = "
                                + Integer.toString(MainActivity.CLIENT_SRV_PORT) + " Destination IP " + strIPaddress);

                        String HOME_PC_IP= "192.168.0.102"; //Home PC
                        strStatus = CheckDeviceAvailability(HOME_PC_IP); //Check connection
                        //strStatus = CheckDeviceAvailability(strIPaddress); //Check connection
                        if (strStatus.equals(udp_data)) {
                            Log.e(TAG, "EQUALS Connection strStatus:" + strStatus + " Recieved udp_data " + udp_data);
                            strAsynCommand = "DO_NOTHING";
                        } else {
                            strAsynCommand = udp_data;
                        }

                        Log.e(TAG, "BEFORE AsynTask : " + strAsynCommand);

                        //////////////////////////////
                        /* Handle request*/
                        new ActionTask().execute(strAsynCommand);
                        //TODO: MOVE TO AsynTask
                        try {
                            //new SendUDPdata("192.168.0.107", test_port, "HELLO SERVER").execute();
                            new SendUDPdata(strIPaddress, MainActivity.CLIENT_SRV_PORT, strAsynCommand).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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


    public String CheckDeviceAvailability(String strIpAdrees) {
        String str = "";
        int pktCount = 2;
        int rcvEchoPkt = 0;
        try {
            Process process = Runtime.getRuntime().exec(
                    "/system/bin/ping -c " + Integer.toString(pktCount) + " " + strIpAdrees);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int i;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((i = reader.read(buffer)) > 0) {
                output.append(buffer, 0, i);
                rcvEchoPkt++;
            }
            reader.close();

            Log.d(TAG, "MY rcvEchoPkt: " + Integer.toString(rcvEchoPkt));

            if (rcvEchoPkt == pktCount) {
                Log.d(TAG, "MY GOOD rcvEchoPkt: " + Integer.toString(rcvEchoPkt));
                str = TURN_ON;
            } else {
                str = TURN_OFF;
            }

            // body.append(output.toString()+"\n");
            String out_str = output.toString();
            Log.d(TAG, "MY:" + out_str);
        } catch (IOException e) {
            // body.append("Error\n");
            e.printStackTrace();
        }
        return str;
    }
}
