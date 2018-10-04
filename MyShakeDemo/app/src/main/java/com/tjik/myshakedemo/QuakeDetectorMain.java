package com.tjik.myshakedemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tjik.myshakedemo.custom_views.FFTView;
import com.tjik.myshakedemo.services.DetectorService;

import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.GPS_PROVIDER;

public class QuakeDetectorMain extends AppCompatActivity implements View.OnClickListener{

    public static boolean PROTECTION_ON = false;

    String myId = "";
    FirebaseDatabase database;
    DatabaseReference usersRef;
    SharedPreferences defaultPref;
    LocationManager locationManager;
    Intent serviceIntent;
    DetectorService detectorService;
    TextView fftThresholdText, fftCurrentText, statusText;
    FFTView fftViewMagnitude;
    Switch protectionSwitch;
    Button stopAlarmBtn;
    TimerTask timerTask;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_detector_main);
        Initialize();
        CheckRegistrationOnFirebase();
        InitializeLocationManager();
        SetInitialState();
        //StartDetectorService();
        StartTimerToUpdateTexts();

    }

    private void SetInitialState() {
        fftThresholdText.setText(detectorService.GetEarthquakeThreshold() + "");
        if(isServiceRunning(detectorService.getClass())){
            PROTECTION_ON = true;
            protectionSwitch.setChecked(true);
            protectionSwitch.setText(protectionSwitch.getTextOn());
            protectionSwitch.setTextColor(getResources().getColor(R.color.colorGreen));
        }else{
            PROTECTION_ON = false;
            protectionSwitch.setChecked(false);
            protectionSwitch.setText(protectionSwitch.getTextOff());
            protectionSwitch.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        // switch state
    }

    void InitializeLocationManager() {
        locationManager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(GPS_PROVIDER,0,0,detectorService);
        detectorService.onLocationChanged(null);
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
            UpdateUI();
        }
    };

    public void UpdateUI() {
        fftCurrentText.setText(detectorService.GetCurrentMagnitude() + "");
        fftViewMagnitude.SetFFTData(detectorService.GetFFTData());
    }

    void Initialize() {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        detectorService = DetectorService.GetInstance(this);
        serviceIntent = new Intent(this, detectorService.getClass());
        fftThresholdText = (TextView) findViewById(R.id.fft_threshold_text);
        fftCurrentText = (TextView) findViewById(R.id.fft_current_text);
        statusText = (TextView) findViewById(R.id.status_text);
        protectionSwitch = (Switch) findViewById(R.id.switch_protection);
        protectionSwitch.setOnClickListener(this);
        stopAlarmBtn = (Button) findViewById(R.id.stop_alarm_btn);
        stopAlarmBtn.setOnClickListener(this);
        fftViewMagnitude = (FFTView) findViewById(R.id.fft_view_magnitude);

        if(!defaultPref.contains("fft_x")){
            AddInitialPreferenceFields();
        }else{
            UpdateUI();
        }
    }

    void AddInitialPreferenceFields() {
        SharedPreferences.Editor editor = defaultPref.edit();
        editor.putBoolean("protection", false);
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
    void StartDetectorService(){
        if(!isServiceRunning(detectorService.getClass())){
            PROTECTION_ON = true;
            startService(serviceIntent);
        }
    }

    void StopDetectorServicePermanently(){
        PROTECTION_ON = false;
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        timerTask.cancel();
        stopService(serviceIntent);
        super.onDestroy();

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.switch_protection){
            if(protectionSwitch.isChecked()){
                Toast.makeText(getApplicationContext(), "Earthquale detection mode on", Toast.LENGTH_SHORT).show();
                protectionSwitch.setTextColor(getResources().getColor(R.color.colorGreen));
                protectionSwitch.setText(protectionSwitch.getTextOn());
                StartDetectorService();
            }else{
                Toast.makeText(getApplicationContext(), "Earthquale detection mode off", Toast.LENGTH_SHORT).show();
                protectionSwitch.setTextColor(getResources().getColor(R.color.colorAccent));
                protectionSwitch.setText(protectionSwitch.getTextOff());
                StopDetectorServicePermanently();
            }
        }else if(view.getId() == R.id.stop_alarm_btn){

        }
    }


}
