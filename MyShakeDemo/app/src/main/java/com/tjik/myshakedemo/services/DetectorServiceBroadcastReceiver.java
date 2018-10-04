package com.tjik.myshakedemo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tjik.myshakedemo.QuakeDetectorMain;

public class DetectorServiceBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if(QuakeDetectorMain.PROTECTION_ON)
            context.startService(new Intent(context, DetectorService.class));
    }
}
