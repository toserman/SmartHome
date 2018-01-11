package com.example.antonio.smarthome;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import static android.content.ContentValues.TAG;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by anton on 1/10/18.
 */
//new SendUDPdata(SERVER_IP,PORT,getCommandName()).execute();
public class ActionTask extends AsyncTask<String, Void, Integer> {

    protected Integer doInBackground(String... str) {
        Log.d(TAG,"ActionTask doInBackground String = " + str[0]);
        if(str[0].equals("TurnOn")) {
            Log.d(TAG, "ActionTask doInBackground STRING EQUALS ");
            turnOnPC();
        }
        return 100;
    }

    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Log.d("MY","ActionTask WAS FINISHED result = " + result.toString());

    }

    public void turnOnPC() {
        //IMPORTANT !!! NEED TO USE NO1 and COM1. CLOSET RED LED VALUE = TRUE
        //DIRECTION_OUT_INITIALLY_LOW = CONNECTED , SET TRUE=DISCONNECTED
        String GPIO_PIN_NAME = "BCM17"; // Physical Pin #11 on Raspberry Pi3
        Gpio mRelayGpio;
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mRelayGpio = service.openGpio(GPIO_PIN_NAME);
            mRelayGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            SystemClock.sleep(1000);
            mRelayGpio.setValue(TRUE);
            mRelayGpio.close();//NEED FOR NEXT LOOP
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

