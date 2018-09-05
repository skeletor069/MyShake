package com.tjik.myshakedemo;

public class ShakeOrigin {
    public String originId;
    public long latitude;
    public long longitude;
    public boolean alarm;

    public ShakeOrigin(){}

    public ShakeOrigin(String originId, long longitude, long latitude, boolean alarm){
        this.originId = originId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.alarm = alarm;
    }

    public long distanceFromOrigin(long longitude, long latitude){
        return 0;
    }
}
