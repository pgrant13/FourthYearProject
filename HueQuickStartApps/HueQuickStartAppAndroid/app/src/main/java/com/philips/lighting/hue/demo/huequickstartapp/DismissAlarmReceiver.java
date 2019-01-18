package com.philips.lighting.hue.demo.huequickstartapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class may not be required. It will transmit the dismiss alarm broadcast to the desired classes
 */
public class DismissAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) { //called when the alarm is dismissed
        //here we want to send a broadcast back to the main activity to call the appropriate methods
        context.sendBroadcast(new Intent("DISMISS_ALARM_RECEIVED"));
    }
}
