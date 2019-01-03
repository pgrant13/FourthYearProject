package com.philips.lighting.hue.demo.huequickstartapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) { //called when the alarm is triggered
        //here we want to send a broadcast back to the main activity to call the appropriate methods
        context.sendBroadcast(new Intent("ALARM_RECEIVED"));
    }
}
