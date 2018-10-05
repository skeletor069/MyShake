package com.tjik.myshakedemo.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tjik.myshakedemo.QuakeDetectorMain;
import com.tjik.myshakedemo.R;
import com.tjik.myshakedemo.core.FFT;
import com.tjik.myshakedemo.core.ShakeOrigin;
import com.tjik.myshakedemo.core.ShakeSubscriber;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.sqrt;

public class DetectorService extends Service implements SensorEventListener, LocationListener {

    public enum DataType{
        TYPE_X, TYPE_Y, TYPE_Z, TYPE_MAGNITUDE
    }

    enum ActionState{
        NEUTRAL, INITIATOR, SECONDARY, ALARM_SET, ALARMED, ALARM_STOPPED
    }

    static DetectorService instance;
    static final String TAG = "DetectorService";
    static boolean timerStarted = false;

    FirebaseDatabase database;
    DatabaseReference shakeOriginRef;
    DatabaseReference shakeSubscriberRef;
    SharedPreferences defaultPref;
    SensorManager mSensorManager;
    ShakeOrigin earthquaeOrigin = null;
    Sensor mAccelerometer;
    MediaPlayer thePlayer;
    //long currentLongitude;
    //long currentLatitude;
    ActionState actionState = ActionState.NEUTRAL;

    float[] gravity = {0,0,0};
    double[] freqCounts;
    int sampleCount = 0;
    int windowSize = 64;
    float x,y,z;
    double[] magnitudeArr;
    double currentMagnitude = 0;
    String myId = "";
    boolean initiator = false;
    long currentLongitude = 100;
    long currentLatitude = 100;
    final double FFT_THRESHOLD_EARTHQUAKE = 800;
    final float MAXIMUM_RADIUS = 50000;
    final float NOTIFICATION_RADIUS = 500000;
    //String logText = "";


    public DetectorService(Context applicationContext) {
        super();
        database = FirebaseDatabase.getInstance();
        shakeOriginRef = database.getReference("shakeOrigin");
        shakeSubscriberRef = database.getReference("shakeSubscriber");
        defaultPref = PreferenceManager.getDefaultSharedPreferences(applicationContext.getApplicationContext());
        mSensorManager = (SensorManager) applicationContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnitudeArr = new double[windowSize];
        thePlayer = MediaPlayer.create(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        freqCounts = new double[windowSize];
        myId = defaultPref.getString("myId","");
        StartListeningForEarthquakeAlarm();
        if(!timerStarted) {
            mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, 2000);
            timerStarted = true;
        }
        Log.d(TAG, "I am from Data Collection Service " + defaultPref);
    }

    public DetectorService() {
    }

    public static DetectorService GetInstance(Context applicationContext){
        if(instance == null)
            instance = new DetectorService(applicationContext);
        return instance;
    }

    public double GetCurrentMagnitude(){
        return currentMagnitude;
    }
    public double GetEarthquakeThreshold(){ return FFT_THRESHOLD_EARTHQUAKE; }
    public double[] GetFFTData(){ return freqCounts; }
    public boolean isAlarmPlaying(){ return actionState == ActionState.ALARMED; }
    public boolean isInitiator(){ return initiator; }
    public boolean isSecondaryDetector(){ return actionState == ActionState.SECONDARY; }
    public void SetMyId(String myId){
        this.myId = myId;
    }

    public void StopAlarm(){
        thePlayer.stop();
        actionState = ActionState.ALARM_STOPPED;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Intent notificationIntent = new Intent(this, QuakeDetectorMain.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = GetNotificationObject(pendingIntent);
        startForeground(1, notification);
        Log.d(TAG, "on start ");
        return START_STICKY;
    }

    @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private Notification GetNotificationObject(PendingIntent pendingIntent) {
        return new Notification.Builder(this)
                    .setContentTitle("Earthquake Detection")
                    .setContentText("Protection is On")
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setContentIntent(pendingIntent)
                    .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("Detector.RestartService");
        sendBroadcast(broadcastIntent);
        Log.d(TAG, "ondestroy!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void UpdateData(DataType dataType, double data){
        currentMagnitude = data;
        if(actionState == ActionState.NEUTRAL && currentMagnitude >= FFT_THRESHOLD_EARTHQUAKE){
            //AppendLog("Shake detected");
            actionState = ActionState.INITIATOR;
            ActionAfterEarthquakeDetection();
        }

    }

    public void ActionAfterEarthquakeDetection(){
        shakeOriginRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    NearestShakeOrigin nearestShakeOrigin = GetNearestShakeOrigin(dataSnapshot);
                    Log.d(TAG, "onDataChange: " + nearestShakeOrigin.distanceToOrigin);
                    if(nearestShakeOrigin.distanceToOrigin < MAXIMUM_RADIUS){
                        RegisterAsShakeSubscriber(nearestShakeOrigin);
                    }else{
                        // the origins are too far. so create new shake origin
                        InitiateEarthquakeWarning();
                    }

                }else{
                    // there are no origins right now. Create new shake origin
                    //AppendLog("I am the first one");
                    //AppendLog("Sending location to server");
                    InitiateEarthquakeWarning();
                }
            }

            void InitiateEarthquakeWarning(){
                //logText = "Initiated earthquake warning. Waiting for other devices to detect earthquake to verify the detection.";
                initiator = true;
                CreateNewShakeOrigin();
                shakeOriginRef.removeEventListener(this);
                StartListeningAsInitiator();
            }

            void RegisterAsShakeSubscriber(NearestShakeOrigin nearestShakeOrigin){
                //logText = "Nearby earthquake origin found. Registering as a secondary detector. Waiting for alarm";
                initiator = false;
                CreateShakeSubscriber(nearestShakeOrigin);
                shakeOriginRef.removeEventListener(this);
            }

            void CreateShakeSubscriber(NearestShakeOrigin nearestShakeOrigin) {
                String originId = nearestShakeOrigin.shakeOrigin.originId;
                ShakeSubscriber shakeSubscriber = new ShakeSubscriber(originId, myId);
                shakeSubscriberRef.child(myId).setValue(shakeSubscriber);
                actionState = ActionState.SECONDARY;
            }

            void CreateNewShakeOrigin() {
                ShakeOrigin shakeOrigin = new ShakeOrigin(myId, currentLongitude, currentLatitude, false);
                shakeOriginRef.child(myId).setValue(shakeOrigin);
                actionState = ActionState.INITIATOR;
            }

            NearestShakeOrigin GetNearestShakeOrigin(DataSnapshot dataSnapshot){
                NearestShakeOrigin nearestOrigin = new NearestShakeOrigin();
                for(DataSnapshot originSnapshot:dataSnapshot.getChildren()){
                    ShakeOrigin temp = originSnapshot.getValue(ShakeOrigin.class);
                    long tempDist = temp.distanceFromOrigin(currentLongitude, currentLatitude);
                    if(tempDist < nearestOrigin.distanceToOrigin){
                        nearestOrigin.shakeOrigin = temp;
                        nearestOrigin.distanceToOrigin = tempDist;
                    }
                }
                return nearestOrigin;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void PlayAlarm(ActionState state){
        actionState = state;
        thePlayer.start();
    }

    void BroadcastStopWarningAfter120s(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                shakeOriginRef.child(myId).removeValue();
            }
        }, 120000);
    }

    void Reset(){
        earthquaeOrigin = null;
        initiator = false;
        actionState = ActionState.NEUTRAL;
        StartListeningForEarthquakeAlarm();
    }

    /*
    * Check if other phones also detected the same earthquake.
    * */
    public void StartListeningAsInitiator(){
        shakeSubscriberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    int subscriberCount = GetSubscriberCount(dataSnapshot);
                    if(subscriberCount >= 1) {
                        //logText = "Earthquake confirmed!! Broadcasting alarm command";
                        BroadcastEarthquakeConfirmation();
                        shakeSubscriberRef.removeEventListener(this);
                        BroadcastStopWarningAfter120s();
                    }
                }
            }

            private void BroadcastEarthquakeConfirmation() {
                ShakeOrigin alarmObj = new ShakeOrigin(myId, currentLongitude, currentLatitude, true);
                shakeOriginRef.child(myId).setValue(alarmObj);
            }

            int GetSubscriberCount(DataSnapshot dataSnapshot) {
                int subscriberCount = 0;
                for(DataSnapshot subscriberSnapshot:dataSnapshot.getChildren()){
                    ShakeSubscriber temp = subscriberSnapshot.getValue(ShakeSubscriber.class);
                    if(temp.originId.equals(myId))
                        subscriberCount++;
                }
                return subscriberCount;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*
    * Detected earthquake as well. Now waiting for the initiator to set the alarm command
    * */
    public void StartListeningForAlarmCommand(final String originId){
        shakeOriginRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot originSnapshot:dataSnapshot.getChildren()){
                    if(originSnapshot.getKey().equals(originId)){
                        ShakeOrigin origin = originSnapshot.getValue(ShakeOrigin.class);
                        if(origin.alarm){
                            shakeSubscriberRef.child(myId).removeValue();
                            shakeOriginRef.removeEventListener(this);
                            PlayAlarm(ActionState.ALARMED);
                            //shakeButton.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    void StartListeningForEarthquakeAlarm(){
        shakeOriginRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(QuakeDetectorMain.PROTECTION_ON) {
                    ShakeOrigin nearestOrigin = null;
                    float nearestDistance = NOTIFICATION_RADIUS;
                    for (DataSnapshot originSnapshot : dataSnapshot.getChildren()) {
                        ShakeOrigin origin = originSnapshot.getValue(ShakeOrigin.class);
                        if (origin.alarm && origin.distanceFromOrigin(currentLongitude, currentLatitude) < nearestDistance) {
                            nearestDistance = origin.distanceFromOrigin(currentLongitude, currentLatitude);
                            nearestOrigin = origin;
                        }
                        break;
                    }
                    if(nearestOrigin != null){
                        //logText = "Earthquake confirmed!!!";
                        earthquaeOrigin = nearestOrigin;
                        shakeOriginRef.removeEventListener(this);
                        PlayAlarm(ActionState.ALARMED);
                        StartListeningForShakeOriginToBeDeletedByInitiator();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    void StartListeningForShakeOriginToBeDeletedByInitiator(){
        shakeOriginRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for(DataSnapshot originSnapshot:dataSnapshot.getChildren()){
                    if(originSnapshot.getKey().equals(earthquaeOrigin.originId)){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    //logText = "Tracking accelerometer to detect earthquake";
                    shakeOriginRef.removeEventListener(this);
                    RemoveMeFromSubscriberList();
                    Reset();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    void RemoveMeFromSubscriberList(){
        shakeSubscriberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot subscriber : dataSnapshot.getChildren()){
                    if(subscriber.getKey().equals(myId)){
                        shakeSubscriberRef.removeEventListener(this);
                        shakeSubscriberRef.child(myId).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void StartListeningForCancelWarning(){
        shakeOriginRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot originSnapshot:dataSnapshot.getChildren()){
                    if(originSnapshot.getKey().equals(earthquaeOrigin.originId)){
                        ShakeOrigin origin = originSnapshot.getValue(ShakeOrigin.class);
                        if(origin.canceled){
                            shakeSubscriberRef.child(myId).removeValue();
                            shakeOriginRef.removeEventListener(this);
                            PlayAlarm(ActionState.ALARMED);
                            //shakeButton.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (QuakeDetectorMain.PROTECTION_ON && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ProcessAccelerometerData(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    void ProcessAccelerometerData(SensorEvent event) {
        CalculateLinearAcceleration(event);
        magnitudeArr[sampleCount] = (double) Math.sqrt(x*x + y*y + z*z);
        sampleCount++;
        if(sampleCount == windowSize){
            new FFTAsynctask(windowSize, DataType.TYPE_MAGNITUDE).execute(magnitudeArr);
            sampleCount = 0;
        }
    }

    void CalculateLinearAcceleration(SensorEvent event) {
        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        x = (event.values[0] - gravity[0]) * 1f;
        y = (event.values[1] - gravity[1]) * 1f;
        z = (event.values[2] - gravity[2]) * 1f;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            currentLongitude = (long)location.getLongitude();
            currentLatitude = (long)location.getLatitude();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    class NearestShakeOrigin{
        public long distanceToOrigin;
        public ShakeOrigin shakeOrigin;
        public NearestShakeOrigin(long distanceToOrigin, ShakeOrigin shakeOrigin){
            this.distanceToOrigin = distanceToOrigin;
            this.shakeOrigin = shakeOrigin;
        }
        public NearestShakeOrigin(){
            this.distanceToOrigin = 999999999;
            this.shakeOrigin = null;
        }
    }

    class FFTAsynctask extends AsyncTask<double[], Void, double[]> {

        int wsize; /* window size must be power of 2 */
        DataType dataType;

        // constructor to set window size
        FFTAsynctask(int wsize, DataType dataType) {
            this.wsize = wsize;
            this.dataType = dataType;
        }

        @Override
        protected double[] doInBackground(double[]... values) {


            double[] realPart = values[0].clone();
            double[] imagPart = new double[wsize];

            FFT fft = new FFT(wsize);
            fft.fft(realPart, imagPart);

            double[] magnitude = new double[wsize];

            for (int i = 0; i < wsize ; i++) {
                magnitude[i] = sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
            }

            return magnitude;

        }

        @Override
        protected void onPostExecute(double[] values) {

            freqCounts = values;

            double peakValue = 0;

            for (int i = 0; i< wsize; i++){
                if(freqCounts[i] > peakValue){
                    peakValue = freqCounts[i];
                }
            }
//            Log.d(TAG, "onPostExecute: " + dataType.toString() + " " + peakValue);
            UpdateData(dataType, peakValue);
        }
    }

}
