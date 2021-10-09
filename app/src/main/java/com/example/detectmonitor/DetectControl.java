package com.example.detectmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

public class DetectControl {
    private Context main_context;

    // camera init
    private CameraManager camera_manager;
    private CameraManager.AvailabilityCallback camera_call_back;
    protected boolean Camera_is_open = false;

    // audio init
    private AudioManager audio_manager;
    private AudioManager.AudioRecordingCallback audio_call_back;
    protected boolean Micphone_is_open = false;

    protected boolean TimeAfter = false;

    protected DetectControl(Context main_context) {
        this.main_context = main_context;
    }

    // Main Function
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void Notification_control(byte number, String str){
        switch (number) {
            case 0: // send msg
                Notification_Manager notification_manager = new Notification_Manager(main_context);
                notification_manager.TimeAfter = TimeAfter;
                Notification notification = notification_manager.notificationChannelBuild(str);
                notification_manager.getManager().notify(1, notification);
                break;
            case 1: // clear
                NotificationManager notification_cleaner = (NotificationManager) main_context.getSystemService(Context.NOTIFICATION_SERVICE);
                notification_cleaner.cancelAll();
            default:
                break;
        }
    }

    // Check Device Function
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void check_device_has_use(@Nullable Handler handler){
        // Camera Manager
        camera_manager = (CameraManager) main_context.getSystemService(Context.CAMERA_SERVICE);
        camera_manager.registerAvailabilityCallback(get_camera_callback(), handler);

        // Audio Manager
        audio_manager = (AudioManager) main_context.getSystemService(Context.AUDIO_SERVICE);
        audio_manager.registerAudioRecordingCallback(get_mic_callback(), handler);
    }

    // Camera Manager Function
    private CameraManager.AvailabilityCallback get_camera_callback() {
        camera_call_back = new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(String cameraId) {    // Camera doesn't have application is use 相機沒再使用
                super.onCameraAvailable(cameraId);
                Camera_is_open = false;
            }

            @Override
            public void onCameraUnavailable(String cameraId) {  // Camera does have application is use 相機使用中
                super.onCameraUnavailable(cameraId);
                Camera_is_open = true;
            }
        };
        return camera_call_back;
    }

    // Audio Manager Function
    @RequiresApi(api = Build.VERSION_CODES.N)
    private AudioManager.AudioRecordingCallback get_mic_callback() {
        audio_call_back = new AudioManager.AudioRecordingCallback() {
            @Override
            public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
                if(configs.size() > 0){ // Mic is recording 麥克風使用中
                    Micphone_is_open = true;
                }else{                  // Mic isn't recoding 麥克風沒在使用
                    Micphone_is_open = false;
                }
            }
        };
        return audio_call_back;
    }
}
