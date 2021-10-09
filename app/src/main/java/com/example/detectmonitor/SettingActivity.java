package com.example.detectmonitor;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{
    private ListView setting_list;

    private Vibrator vibrator;
    private EditText edit_vibrator_time, edit_vibrator_start, edit_vibrator_stop;
    private int vibrator_list[] = {0, 0, 0};
    private SharedPreferences share_data;
    private SharedPreferences.Editor share_data_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        setTitle("設定 Setting");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        edit_vibrator_time = (EditText) findViewById(R.id.edit_vibrator_time);
        edit_vibrator_time.setHint("輸入次數(e.g. 3)");
        edit_vibrator_start = (EditText) findViewById(R.id.edit_vibrator_start);
        edit_vibrator_start.setHint("輸入毫秒(e.g. 400)");
        edit_vibrator_stop = (EditText) findViewById(R.id.edit_vibrator_stop);
        edit_vibrator_stop.setHint("輸入毫秒(e.g. 100)");

        findViewById(R.id.btn_vibrator_time).setOnClickListener(this);
        findViewById(R.id.btn_vibrator_start).setOnClickListener(this);
        findViewById(R.id.btn_vibrator_stop).setOnClickListener(this);
        findViewById(R.id.test_vibrator).setOnClickListener(this);

        share_data = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        share_data_editor = share_data.edit();
        vibrator_list[0] = share_data.getInt("vibrator_time", 3);
        vibrator_list[1] = share_data.getInt("vibrator_start", 400);
        vibrator_list[2] = share_data.getInt("vibrator_stop", 100);

        // click TextView test vibrator event
        findViewById(R.id.test_vibrator).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        findViewById(R.id.test_vibrator).setBackgroundColor(Color.parseColor("#40E0D0"));
                        //System.out.println("down");
                        break;
                    case MotionEvent.ACTION_UP:
                        findViewById(R.id.test_vibrator).setBackgroundColor(Color.parseColor("#9FE2BF"));
                        //System.out.println("up");
                        break;
                }
                return SettingActivity.super.onTouchEvent(event);
            }
        });

    }

    // action bar return button function
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                SettingActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // btn click function
    @Override
    public void onClick(View v) {
        String temp_str = "";
        switch (v.getId()) {
            case R.id.test_vibrator:        // test vibrator
                long temp[] = new long[vibrator_list[0] * 2];
                for(int i = 0; i < temp.length; i += 2){
                    temp[i] = vibrator_list[2]; // stop
                    temp[i + 1] = vibrator_list[1]; // start
                }
                vibrator.vibrate(temp, -1);
                break;
            case R.id.btn_vibrator_time:    // vibrator count
                temp_str = edit_vibrator_time.getText().toString();
                if(!temp_str.equals("")) {
                    vibrator_list[0] = Integer.parseInt(temp_str);
                    System.out.println("0=>" + vibrator_list[0]);
                    share_data_editor.putInt("vibrator_time", vibrator_list[0]);
                    share_data_editor.apply();
                }
                break;
            case R.id.btn_vibrator_start:   // vibrator time
                temp_str = edit_vibrator_start.getText().toString();
                if(!temp_str.equals("")) {
                    vibrator_list[1] = Integer.parseInt(temp_str);
                    System.out.println("1=>" + vibrator_list[1]);
                    share_data_editor.putInt("vibrator_start", vibrator_list[1]);
                    share_data_editor.apply();
                }
                break;
            case R.id.btn_vibrator_stop:    // stop time
                temp_str = edit_vibrator_stop.getText().toString();
                if(!temp_str.equals("")){
                    vibrator_list[2] = Integer.parseInt(edit_vibrator_stop.getText().toString());
                    System.out.println("2=>" + vibrator_list[2]);
                    share_data_editor.putInt("vibrator_stop", vibrator_list[2]);
                    share_data_editor.apply();
                }
                break;
            default:
                break;
        }
    }
}