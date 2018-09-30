package com.tjik.myshakedemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataCollection extends AppCompatActivity {

    String myId = "";
    FirebaseDatabase database;
    DatabaseReference usersRef;
    SharedPreferences defaultPref;
    Intent serviceIntent;
    DataCollectionService dataCollectionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        Initialize();
        CheckRegistration();
        StartDataCollectionService();
    }

    void Initialize() {
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dataCollectionService = new DataCollectionService(this);
        serviceIntent = new Intent(this, dataCollectionService.getClass());
    }

    void CheckRegistration(){
        if(defaultPref.contains("myId")) {
            myId = defaultPref.getString("myId", "");
        }
        else{
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long count = dataSnapshot.getChildrenCount();
                    myId = "user" + count;
                    usersRef.removeEventListener(this);
                    usersRef.child(myId).setValue(myId);
                    SharedPreferences.Editor editor = defaultPref.edit();
                    editor.putString("myId", myId);
                    editor.commit();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

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

    void StartDataCollectionService(){
        if(!isServiceRunning(dataCollectionService.getClass())){
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        super.onDestroy();

    }
}
