package com.example.detectmonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {

    protected DetectControl dc_class;
    private Vibrator vibrator;

    private Timer timer;
    private boolean close_event = false;
    private String msg_str = "";

    private boolean camera_open_before = false;
    private boolean micphone_open_before = false;
    private int vibrator_list[] = {0, 0, 0};
    private boolean bg_action;

    // app preferences init
    private SharedPreferences data;
    private SharedPreferences.Editor data_editor;

    private SqliteHelper sqlite_helper;
    private SQLiteDatabase database;
    private String table_name = "Device_Record";

    @Override
    public void onCreate() {
        super.onCreate();
        dc_class = new DetectControl(BackgroundService.this);
        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        // get data
        data = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        data_editor = data.edit();
        vibrator_list[0] = data.getInt("vibrator_time", 3);
        vibrator_list[1] = data.getInt("vibrator_start", 400);
        vibrator_list[2] = data.getInt("vibrator_stop", 100);
        bg_action = data.getBoolean("bg_action", false);

        sqlite_helper = new SqliteHelper(this, "Device_Log", null, 1, table_name);
        database = sqlite_helper.getWritableDatabase();
    }

    private Handler mHandler = new Handler();
    private Runnable runable = new Runnable() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            dc_class.check_device_has_use(mHandler);
            if(camera_open_before != dc_class.Camera_is_open || micphone_open_before != dc_class.Micphone_is_open){
                String device_name = "";
                if(dc_class.Camera_is_open) {
                    msg_str = "偵測到'相機'已被啟用";
                    device_name = "相機";
                } else {
                    msg_str = "";
                }
                if(dc_class.Micphone_is_open) {
                    msg_str = "偵測到'麥克風'已被啟用";
                    device_name = "麥克風";
                }
                if(!msg_str.equals("")) {
                    dc_class.TimeAfter = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        dc_class.Notification_control((byte) 0, msg_str);
                    long temp[] = new long[vibrator_list[0] * 2];
                    for(int i = 0; i < temp.length; i += 2){
                        temp[i] = vibrator_list[2]; // stop
                        temp[i + 1] = vibrator_list[1]; // start
                    }
                    vibrator.vibrate(temp, -1);

                    sqlite_helper.insert_data(database, device_name);
                }
                camera_open_before = dc_class.Camera_is_open;
                micphone_open_before = dc_class.Micphone_is_open;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // notification background process
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("DetectCHID", "DetectMonitor", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(getApplication(), "DetectCHID")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon)
                .setTicker("啟動背景偵測")
                .setContentTitle("裝置偵測")
                .setContentText("背景裝置偵測中...")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        startForeground(2, notification);

        data_editor.putBoolean("bg_action", true);
        data_editor.apply();

        TimerTask action = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                //Log.d("BackgroundService", "Running " + (count++));
                mHandler.post(runable);

                if(close_event){
                    mHandler.removeCallbacks(runable);
                    data_editor.putBoolean("bg_action", false);
                    data_editor.apply();
                    timer.cancel();
                    stopSelf();
                    stopForeground(true);
                }
            }
        };

        timer = new Timer();
        timer.schedule(action, 1000, 3000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
        close_event = true;
    }
}
