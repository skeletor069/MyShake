package com.tjik.myshakedemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrackerActivity extends AppCompatActivity implements SensorEventListener {



    class SensorData{
        public float x;
        public float y;
        public float z;

        public SensorData(float x, float y, float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    Button startBtn;
    Button endBtn;
    TextView notificationText;
    EditText labelInput;

    List<SensorData> sensorDataList = new ArrayList<SensorData>();

    int sampleRate = 2000;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    FileOutputStream outputStream;
    boolean tracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        startBtn = (Button) findViewById(R.id.trackStartBtn);
        endBtn = (Button) findViewById(R.id.trackEndBtn);
        labelInput = (EditText) findViewById(R.id.labelInput);
        notificationText = (TextView) findViewById(R.id.notificationText);

        endBtn.setClickable(false);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!labelInput.getText().equals("")) {
                    startBtn.setClickable(false);
                    endBtn.setClickable(true);
                    writeText = "";
                    notificationText.setText("Tracker running");
                    tracking = true;
                }
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracking = false;
                endBtn.setClickable(false);
                startBtn.setClickable(true);
                notificationText.setText("");
                String fileName = "accdata/" + labelInput.getText().toString() + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".txt";
                try {
                    Log.d("fileName", fileName);
                    File directory = Environment.getExternalStorageDirectory();
                    File file = new File(directory, fileName);

                    outputStream = new FileOutputStream(file);
                    outputStream.write(writeText.getBytes());
                    outputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);



    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (tracking && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    String writeText = "";
    float[] gravity = {0,0,0};
    private void getAccelerometer(SensorEvent event) {

        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float x = (event.values[0] - gravity[0]) * 1f;
        float y = (event.values[1] - gravity[1]) * 1f;
        float z = (event.values[2] - gravity[2]) * 1f;

        writeText += new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime()) + " " + x + " " + y + " " + z + "\n";

//        outputStream.write();
    }

}
