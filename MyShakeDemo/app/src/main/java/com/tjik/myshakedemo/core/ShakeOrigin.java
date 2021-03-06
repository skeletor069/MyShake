package com.tjik.myshakedemo.core;

import android.location.Location;

public class ShakeOrigin {
    public String originId;
    public long latitude;
    public long longitude;
    public long timeStamp;
    public boolean alarm;
    public boolean canceled;

    public ShakeOrigin(){}

    public ShakeOrigin(String originId, long longitude, long latitude, boolean alarm){
        this.originId = originId;
        this.longitude = longitude;
        this.latitude = latitude;
//        this.timeStamp = timeStamp;
        this.alarm = alarm;
        this.canceled = false;
    }

    public ShakeOrigin(String originId, long longitude, long latitude, boolean alarm, boolean canceled){
        this.originId = originId;
        this.longitude = longitude;
        this.latitude = latitude;
//        this.timeStamp = timeStamp;
        this.alarm = alarm;
        this.canceled = canceled;
    }

    public long distanceFromOrigin(long longitude, long latitude){
        float[] results = new float[3];
        Location.distanceBetween(this.latitude, this.longitude, latitude, longitude, results);
        return (long) results[0];
    }
}
