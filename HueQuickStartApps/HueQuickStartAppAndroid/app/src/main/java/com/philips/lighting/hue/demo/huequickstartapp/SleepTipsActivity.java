package com.philips.lighting.hue.demo.huequickstartapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SleepTipsActivity extends AppCompatActivity implements View.OnClickListener { //right now using this class to debug smart plug

    private static final String TAG = "SleepTipsActivity";

    private Button authSmartplugButton;
    private Button urlSmartplugButton;
    private Button onSmartplugButton;
    private Button offSmartplugButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tips);

        authSmartplugButton = (Button)findViewById(R.id.auth_smartplug_button);
        authSmartplugButton.setOnClickListener(this);
        urlSmartplugButton = (Button)findViewById(R.id.url_smartplug_button);
        urlSmartplugButton.setOnClickListener(this);
        onSmartplugButton = (Button)findViewById(R.id.on_smartplug_button);
        onSmartplugButton.setOnClickListener(this);
        offSmartplugButton = (Button)findViewById(R.id.off_smartplug_button);
        offSmartplugButton.setOnClickListener(this);
    } //end onCreate()

    //when the user clicks on one of the buttons
    @Override
    public void onClick(View view) {
        if (view == authSmartplugButton) {
            Log.i(TAG, "authSmartplugButton was clicked");
            //must get UUID before from https://www.uuidgenerator.net/version4 - already done on Paul's android
            String surl = "https://wap.tplinkcloud.com";
            String sdata = "{\"method\": \"login\", \"params\": {\"appType\": \"Kasa_Android\", \"cloudUserName\": \"paul.stephen.grant@gmail.com\", \"cloudPassword\": \"hockey13\", \"terminalUUID\": \"4dc0a122-aa41-449e-a464-f137fef2cf67\"}}";
            //Curl.curl(surl, sdata);

            //need to get returned token
        }
        if (view == urlSmartplugButton) { //i already have url so i don't think this is necessary
            Log.i(TAG, "urlSmartplugButton was clicked");
            String surl = "https://wap.tplinkcloud.com?token=08d8afb2-A62wJmPMOqaFzYY8vVgoR98 HTTP/1.1"; //use returned token here
            String sdata = "{\"method\":\"getDeviceList\"}";
            //Curl.curl(surl, sdata);
        }
        if (view == onSmartplugButton) {
            Log.i(TAG, "onSmartplugButton was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A62wJmPMOqaFzYY8vVgoR98 HTTP/1.1";
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":1}}}\" }}";
            //Curl.curl(surl, sdata);
        }
        if (view == offSmartplugButton) {
            Log.i(TAG, "offSmartplugButton was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A62wJmPMOqaFzYY8vVgoR98 HTTP/1.1";
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":0}}}\" }}";
            // Curl.curl(surl, sdata);
        }
    }
}
