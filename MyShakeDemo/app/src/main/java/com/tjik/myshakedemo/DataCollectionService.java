package com.tjik.myshakedemo;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class DataCollectionService extends Service implements SensorEventListener {

    static final String TAG = "DataCollectionService";
    static boolean timerStarted = false;
    Timer timer;
    TimerTask timerTask;
    SharedPreferences defaultPref;
    public int counter = 0;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    float[] gravity = {0,0,0};

    public DataCollectionService(Context applicationContext, SharedPreferences defaultPref) {
        super();
        this.defaultPref = defaultPref;
        mSensorManager = (SensorManager) applicationContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(!timerStarted) {
            mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, 2000);
            timerStarted = true;
        }
        Log.d(TAG, "I am from Data Collection Service " + defaultPref);
    }

    public DataCollectionService() {
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Intent notificationIntent = new Intent(this, DataCollection.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Notification")
                .setContentText("Content of notification")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
//        if(!timerStarted)
//            StartTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("DataCollection.RestartService");
        sendBroadcast(broadcastIntent);
        Log.d(TAG, "ondestroy!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void StartTimer(){
//        SharedPreferences.Editor editor = defaultPref.edit();
//        editor.putString("timerStarted", "true");
//        editor.commit();
        timerStarted = true;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "run: timer task running " + counter);
                counter++;
                if(counter == 100) {
                    timerStarted = false;
                    cancel();
                }
            }
        };
        timer.schedule(timerTask, 1000,1000);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    private void getAccelerometer(SensorEvent event) {

        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float x = (event.values[0] - gravity[0]) * 1f;
        float y = (event.values[1] - gravity[1]) * 1f;
        float z = (event.values[2] - gravity[2]) * 1f;
        Log.d(TAG, "getAccelerometer: " + x + " " + y + " " + z);

//        outputStream.write();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
