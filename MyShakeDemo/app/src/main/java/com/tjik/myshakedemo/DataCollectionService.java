package com.tjik.myshakedemo;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import static java.lang.Math.sqrt;

public class DataCollectionService extends Service implements SensorEventListener {

    public enum DataType{
        TYPE_X, TYPE_Y, TYPE_Z, TYPE_MAGNITUDE
    }

    static final String TAG = "DataCollectionService";
    static boolean timerStarted = false;
    SharedPreferences defaultPref;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    float[] gravity = {0,0,0};
    private double[] freqCounts;
    int sampleCount = 0;
    int windowSize = 64;

    float x,y,z;
    double[] xArr, yArr, zArr, magnitudeArr;
    double peakFFT_X, peakFFT_Y, peakFFT_Z, peakFFT_MAGNITUDE;

    public DataCollectionService(Context applicationContext) {
        super();
        defaultPref = PreferenceManager.getDefaultSharedPreferences(applicationContext.getApplicationContext());
        mSensorManager = (SensorManager) applicationContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        xArr = yArr = zArr = magnitudeArr = new double[windowSize];
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

    public void UpdateData(DataType dataType, double data){
        Log.d(TAG, "UpdateData: " + peakFFT_X + " " + peakFFT_Y + " " + peakFFT_Z + " " + peakFFT_MAGNITUDE);
        switch (dataType){
            case TYPE_X:
                if(data > peakFFT_X) {
                    peakFFT_X = data;
                    SharedPreferences.Editor editor = defaultPref.edit();
                    editor.putString("fft_x", peakFFT_X+"");
                    editor.commit();
                }
                break;
            case TYPE_Y:
                if(data > peakFFT_Y) {
                    peakFFT_Y = data;
                    SharedPreferences.Editor editor = defaultPref.edit();
                    editor.putString("fft_y", peakFFT_Y+"");
                    editor.commit();
                }
                break;
            case TYPE_Z:
                if(data > peakFFT_Z) {
                    peakFFT_Z = data;
                    SharedPreferences.Editor editor = defaultPref.edit();
                    editor.putString("fft_z", peakFFT_Z+"");
                    editor.commit();
                }
                break;
            case TYPE_MAGNITUDE:
                if(data > peakFFT_MAGNITUDE) {
                    peakFFT_MAGNITUDE = data;
                    SharedPreferences.Editor editor = defaultPref.edit();
                    editor.putString("fft_magnitude", peakFFT_MAGNITUDE+"");
                    editor.commit();
                }
                break;
                default:

                    break;
        }
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

        x = (event.values[0] - gravity[0]) * 1f;
        y = (event.values[1] - gravity[1]) * 1f;
        z = (event.values[2] - gravity[2]) * 1f;

        xArr[sampleCount] = x;
        yArr[sampleCount] = y;
        zArr[sampleCount] = z;
        magnitudeArr[sampleCount] = (double) Math.sqrt(x*x + y*y + z*z);

        sampleCount++;
        if(sampleCount == windowSize){
            new FFTAsynctask(windowSize, DataType.TYPE_X).execute(xArr);
            new FFTAsynctask(windowSize, DataType.TYPE_Y).execute(yArr);
            new FFTAsynctask(windowSize, DataType.TYPE_Z).execute(zArr);
            new FFTAsynctask(windowSize, DataType.TYPE_MAGNITUDE).execute(magnitudeArr);
            sampleCount = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class FFTAsynctask extends AsyncTask<double[], Void, double[]> {

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
            Log.d(TAG, "onPostExecute: " + dataType.toString() + " " + peakValue);
            UpdateData(dataType, peakValue);
        }
    }

}
