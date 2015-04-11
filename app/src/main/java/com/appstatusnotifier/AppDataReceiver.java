package com.appstatusnotifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Misaal on 11/04/2015.
 */
public class AppDataReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName name = new ComponentName(context.getPackageName(), AppDataService.class.getName());
        startWakefulService(context, intent.setComponent(name));
    }
}
