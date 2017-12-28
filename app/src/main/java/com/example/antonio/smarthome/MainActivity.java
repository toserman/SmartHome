package com.example.antonio.smarthome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tview_log;
    Button btn_start, btn_stop;
    private static final String TAG = MainActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button)findViewById(R.id.srv_start);
        btn_stop = (Button)findViewById(R.id.srv_stop);
        tview_log = (TextView)findViewById(R.id.output);

        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

    }
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.srv_start:
                Log.d(TAG,"Button: SERVER START");
                break;
            case R.id.srv_stop:
                Log.d(TAG,"Button: SERVER STOP");
                break;
        }

    }


}
