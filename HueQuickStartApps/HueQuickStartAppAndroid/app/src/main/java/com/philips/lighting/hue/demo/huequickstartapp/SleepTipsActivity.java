package com.philips.lighting.hue.demo.huequickstartapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity which will display sleep tips to the user
 */
public class SleepTipsActivity extends AppCompatActivity { //right now using this class to debug smart plug

    private static final String TAG = "SleepTipsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tips);

    } //end onCreate()

}
