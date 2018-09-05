package com.tjik.myshakedemo;

public class AccelerometerViewData {
    public float x;
    public float y;
    public float z;
    public float magnitude;
    public AccelerometerViewData(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.magnitude = GetMagnitude();
    }

    float GetMagnitude(){
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}
