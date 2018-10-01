package com.tjik.myshakedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DataCollectionServiceBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, DataCollectionService.class));
    }
}
