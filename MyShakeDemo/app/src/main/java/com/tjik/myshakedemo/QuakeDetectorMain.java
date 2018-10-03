package com.tjik.myshakedemo;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tjik.myshakedemo.services.DataCollectionService;

import java.util.Timer;
import java.util.TimerTask;

public class QuakeDetectorMain extends AppCompatActivity {

    String myId = "";
    FirebaseDatabase database;
    DatabaseReference usersRef;
    SharedPreferences defaultPref;
    Intent serviceIntent;
    DataCollectionService dataCollectionService;
    TextView xDataText, yDataText, zDataText, magDataText;

    TimerTask timerTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_detector_main);
        Initialize();
        CheckRegistrationOnFirebase();
        StartDataCollectionService();
        StartTimerToUpdateTexts();
    }

    void StartTimerToUpdateTexts() {
        Timer timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();
            }
        };
        timer.schedule(timerTask, 1000,1000);
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            UpdateTextFields();
        }
    };

    public void UpdateTextFields() {
        xDataText.setText(defaultPref.getString("fft_x",""));
        yDataText.setText(defaultPref.getString("fft_y",""));
        zDataText.setText(defaultPref.getString("fft_z",""));
        magDataText.setText(defaultPref.getString("fft_magnitude",""));
    }

    void Initialize() {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dataCollectionService = new DataCollectionService(this);
        serviceIntent = new Intent(this, dataCollectionService.getClass());
        xDataText = (TextView) findViewById(R.id.x_data);
        yDataText = (TextView) findViewById(R.id.y_data);
        zDataText = (TextView) findViewById(R.id.z_data);
        magDataText = (TextView) findViewById(R.id.mag_data);
        if(!defaultPref.contains("fft_x")){
            AddInitialPreferenceFields();
        }else{
            UpdateTextFields();
        }
    }

    void AddInitialPreferenceFields() {
        SharedPreferences.Editor editor = defaultPref.edit();
        editor.putString("fft_x", "0");
        editor.putString("fft_y", "0");
        editor.putString("fft_z", "0");
        editor.putString("fft_magnitude", "0");
        editor.commit();
    }

    void CheckRegistrationOnFirebase(){
        if(defaultPref.contains("myId")) {
            myId = defaultPref.getString("myId", "");
        }
        else{
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    RegisterNewUserOnFirebase(dataSnapshot);
                    SaveMyIdInPreference();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

                void RegisterNewUserOnFirebase(DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();
                    myId = "user" + count;
                    usersRef.removeEventListener(this);
                    usersRef.child(myId).setValue(myId);
                }

                void SaveMyIdInPreference() {
                    SharedPreferences.Editor editor = defaultPref.edit();
                    editor.putString("myId", myId);
                    editor.commit();
                }
            });
        }
    }

    boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void StartDataCollectionService(){
        if(!isServiceRunning(dataCollectionService.getClass())){
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        timerTask.cancel();
        stopService(serviceIntent);
        super.onDestroy();

    }
}
