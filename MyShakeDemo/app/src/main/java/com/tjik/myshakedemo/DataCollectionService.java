package com.tjik.myshakedemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class DataCollectionService extends Service {

    static final String TAG = "DataCollectionService";
    Timer timer;
    TimerTask timerTask;
    public int counter = 0;

    public DataCollectionService(Context applicationContext) {
        super();
        Log.d(TAG, "I am from Data Collection Service");
    }

    public DataCollectionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        StartTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ondestroy!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void StartTimer(){
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "run: timer task running");
                counter++;
                if(counter == 10)
                    cancel();
            }
        };
        timer.schedule(timerTask, 1000,1000);
    }
}
