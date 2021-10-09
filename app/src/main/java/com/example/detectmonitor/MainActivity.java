package com.example.detectmonitor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private Vibrator vibrator;
    private int vibrator_list[] = {0, 0, 0};

    private Switch sw_bg_action;

    private DetectControl dc_class;

    private SqliteHelper sqlite_helper;
    private SQLiteDatabase database;
    private String table_name = "Device_Record";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dc_class = new DetectControl(this);

        // vibrator init
        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        // get data info
        SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        vibrator_list[0] = data.getInt("vibrator_time", 3);
        vibrator_list[1] = data.getInt("vibrator_start", 400);
        vibrator_list[2] = data.getInt("vibrator_stop", 100);

        // set btn listener
        findViewById(R.id.btn_detect).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_log).setOnClickListener(this);

        // switch background action
        sw_bg_action = findViewById(R.id.sw_bg_action);
        boolean check_notification = false;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {  // Notification bar check
            if (notification.getId() == 2) {
                check_notification = true;
                break;
            }
        }
        sw_bg_action.setChecked(check_notification);
        sw_bg_action.setOnCheckedChangeListener(this);

        // remove actionbar
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }

        sqlite_helper = new SqliteHelper(this, "Device_Log", null, 1, table_name);
        database = sqlite_helper.getWritableDatabase();
    }

    // switch check change function
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton.isChecked()){ // Open
            Toast.makeText(MainActivity.this, "Opening...", Toast.LENGTH_SHORT).show();
            Intent test = new Intent(MainActivity.this, BackgroundService.class);
            startService(test);
        }else{                          // Close
            Toast.makeText(MainActivity.this, "Closeing...", Toast.LENGTH_SHORT).show();
            Intent test = new Intent(MainActivity.this, BackgroundService.class);
            stopService(test);
        }
    }

    // btn click function
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_detect:   // Detect 偵測
                // 取消背景執行
                dc_class.Notification_control((byte) 1, "");
                Intent test = new Intent(MainActivity.this, BackgroundService.class);
                stopService(test);
                sw_bg_action.setChecked(false);

                // 開始偵測裝置
                Toast.makeText(this, "偵測中...", Toast.LENGTH_SHORT).show();
                dc_class.check_device_has_use(null);
                String temp_str = "相機";
                String device_name ="";
                if(dc_class.Camera_is_open) {
                    temp_str += "是開啟的\n";
                    device_name += "相機";
                } else temp_str += "是關閉的\n";
                if(dc_class.Micphone_is_open) {
                    temp_str += "麥克風是開啟的";
                    if(device_name.equals(""))
                        device_name += "麥克風";
                    else
                        device_name += "、麥克風";
                } else temp_str += "麥克風是關閉的";

                // 顯示通知
                dc_class.TimeAfter = true;
                dc_class.Notification_control((byte) 0, temp_str);

                // 震動
                long temp[] = new long[vibrator_list[0] * 2];
                for(int i = 0; i < temp.length; i += 2){
                    temp[i] = vibrator_list[2]; // stop
                    temp[i + 1] = vibrator_list[1]; // start
                }
                vibrator.vibrate(temp, -1);

                if(!device_name.equals("")){
                    sqlite_helper.insert_data(database, device_name);
                }
                break;
            case R.id.btn_clear:    // Clear 清除通知欄
                Toast.makeText(this, "清除通知欄", Toast.LENGTH_SHORT).show();
                dc_class.Notification_control((byte) 1, "");
                break;
            case R.id.btn_setting:  // Setting 設定
                Intent intent_setting = new Intent();
                intent_setting.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent_setting);
                break;
            case R.id.btn_log:      // Log 紀錄
                Intent intent_log = new Intent();
                intent_log.setClass(MainActivity.this, LogActivity.class);
                startActivity(intent_log);
                break;
            default:
                break;
        }
    }

    // app close event
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        dc_class.Notification_control((byte) 1, "");
        database.close();
        super.onDestroy();
    }
}