package com.example.antonio.smarthome;

/**
 * Created by anton on 1/19/18.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SendUDPdata extends AsyncTask<Void, Void, Integer> {

    private int udpPort;
    private String SendMsg;
    private DatagramSocket DgrmSocket;
    private String ipAddress;

    public SendUDPdata(String inpIpaddress, int port, String Message) {
        this.udpPort = port;
        this.SendMsg = Message;
        this.ipAddress = inpIpaddress;
    }

    protected Integer doInBackground(Void... arg0) {
        try {
            DatagramPacket dp = null;
            InetAddress IPAddress = InetAddress.getByName(ipAddress);
            Log.d("MY: ", "Send to " + IPAddress + " Command: " + SendMsg );
            dp = new DatagramPacket(SendMsg.getBytes(), SendMsg.length(), IPAddress, udpPort);

            DgrmSocket = new DatagramSocket(null);
            DgrmSocket.setReuseAddress(true);
            DgrmSocket.setBroadcast(true);
            DgrmSocket.bind(new InetSocketAddress(this.udpPort));

            DgrmSocket.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 100;
    }

    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Log.d("MY","ASYNC TASK WAS FINISHED !!! Message sent : " + this.SendMsg +""
                +" result =  " + result.toString());

    }
}
