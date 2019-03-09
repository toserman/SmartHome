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

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MqttCallback {

    TextView tview_log;
    Button btn_start, btn_stop, btn_clear_textview;
    ServerUDPthread serverThread;
    private final String TAG = "MY";//;//MainActivity.class.getName();
    static final int UDP_PORT = 48656;
    static final int CLIENT_SRV_PORT = 48656;
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


        try {
            //MqttClient client = new MqttClient("tcp://192.168.1.7:1883", "AndroidThingSub", new MemoryPersistence());
           // MqttClient client = new MqttClient("tcp://192.168.0.105:1883", "AndroidThingSub", new MemoryPersistence());
            MqttClient client = new MqttClient("tcp://test.mosquitto.org:1883", "AndroidThingSub", new MemoryPersistence());
            client.setCallback(this);
            client.connect();
            String topic = "MQTT Examples";
            Log.e("TAG","MY START MQtt Subscribe:" + topic);
            tview_log.setText("MY START MQtt Subscribe:" + topic);
            client.subscribe(topic);

//            MqttMessage message = new MqttMessage("Hello, I am Android Mqtt Client.".getBytes());
//            message.setQos(1);
//            message.setRetained(false);
//
//            client.publish("messages", message);
//            Log.e(TAG, "Message published");


//                    MQTTClient_message pubmsg = MQTTClient_message_initializer;
//                    MQTTClient_deliveryToken token;
//
//                    MQTTClient_create(&client, ADDRESS, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, NULL);
//                     pubmsg.payload = PAYLOAD;
//                    pubmsg.payloadlen = strlen(PAYLOAD);
//                    pubmsg.qos = QOS;
//                    pubmsg.retained = 0;
//                    MQTTClient_publishMessage(client, TOPIC, &pubmsg, &token);
//                    MqttMessage msg = new MqttMessage();
//                    msg.setPayload("Hello IoT");
//                    client.publish(topic,"HELLO IoT",1,true);

        } catch (MqttException e) {
            e.printStackTrace();
        }

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
        tview_log.setText("SERVER STARTED IP:" + getIpAddress() + " PORT: " + UDP_PORT + "\n");
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
    @Override
    protected void onResume() {
        Log.e("TAG", "STATE onResume" );
        super.onResume();
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
                        ip += inetAddress.getHostAddress();
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

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "MY connectionLost....");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        Log.e(TAG, "MY " + payload);
        tview_log.setText("Recieve: " + payload);
//        switch (payload) {
//            case "ON":
//                Log.d(TAG, "LED ON");
//                ledPin.setValue(true);
//                break;
//            case "OFF":
//                Log.d(TAG, "LED OFF");
//                ledPin.setValue(false);
//                break;
//            default:
//                Log.d(TAG, "Message not supported!");
//                break;
//        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.e(TAG, "MY deliveryComplete....");
    }


}
