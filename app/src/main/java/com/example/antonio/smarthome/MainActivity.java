package com.example.antonio.smarthome;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tview_log;
    Button btn_start, btn_stop, btn_clear_textview;
    ServerUDPthread serverThread;
    private final String TAG = "MY";//;//MainActivity.class.getName();
    static final int UDP_PORT = 48656;
    public Handler hdThread; //Handler for receiving msg from Server Thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.srv_start);
        btn_stop = findViewById(R.id.srv_stop);
        btn_clear_textview = findViewById(R.id.clear_textview);
        tview_log = findViewById(R.id.output);
        tview_log.setMovementMethod(new ScrollingMovementMethod());

        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_clear_textview.setOnClickListener(this);

        //TODO: CAN BE LEAKAGE !! INVESTIGATE !!!
        hdThread = new Handler() {
            public void handleMessage(Message msg) {
                final String msgRcv = (String)msg.obj;
                Log.e("TAG","RECEIVE MESSAGE FROM THREAD :" + msgRcv);
                super.handleMessage(msg);
            }
        };

     }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.srv_start:
                Log.e(TAG,"Button: SERVER START");
                tview_log.setText("SERVER STARTED IP:" + getIpAddress());
                if (serverThread == null) {
                    serverThread = new ServerUDPthread(UDP_PORT, MainActivity.this,hdThread);
                    serverThread.setRunning(true);
                    serverThread.start();
                    tview_log.setText("SERVER STARTED");
                }
                //tview_log.setText(getIpAddress());
                break;
            case R.id.srv_stop:
                //TODO: Need debug for good STOP
                Log.e(TAG,"Button: SERVER STOP");
                if(serverThread != null){
                    serverThread.setRunning(false);
                    serverThread.interrupt();
                    serverThread = null;
                    tview_log.setText("SERVER STOPPED");
                }
                break;
            case R.id.clear_textview:
                Log.e(TAG,"Button: CLEAR TextView");
                tview_log.setText("");
                break;
        }
    }

//    @Override
    protected void onStart() {
        serverThread = new ServerUDPthread(UDP_PORT,MainActivity.this,hdThread);
        serverThread.setRunning(true);
        serverThread.start();
        tview_log.setText("SERVER STARTED IP:" + getIpAddress());
        super.onStart();
    }

    @Override
    protected void onStop() {
          if(serverThread != null){
            serverThread.setRunning(false);
            serverThread = null;
          }
        super.onStop();
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
