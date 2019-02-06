package com.philips.lighting.hue.demo.huequickstartapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Activity to setup the TPLink smartplug
 */
public class SetupTPLinkActivity extends AppCompatActivity implements View.OnClickListener {
    //token: 08d8afb2-A1876mDY6ETrqjG66WFrJwx
    //smartplug1: 8006D533442D25A6A864522D93217C121A255439
    //smartplug2: 80069E32EB7ED682EA56429752DDE14A1A25686B

    private static final String TAG = "SetupTPLinkActivity";

    private Button authSmartplugButton;
    private Button urlSmartplugButton;
    private Button onSmartplug1Button;
    private Button offSmartplug1Button;
    private Button onSmartplug2Button;
    private Button offSmartplug2Button;

    private String smartplug1 = "8006D533442D25A6A864522D93217C121A255439";
    private String smartplug2 = "80069E32EB7ED682EA56429752DDE14A1A25686B";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_tplink);

        authSmartplugButton = (Button)findViewById(R.id.auth_smartplug_button);
        authSmartplugButton.setOnClickListener(this);
        urlSmartplugButton = (Button)findViewById(R.id.url_smartplug_button);
        urlSmartplugButton.setOnClickListener(this);
        onSmartplug1Button = (Button)findViewById(R.id.on_smartplug1_button);
        onSmartplug1Button.setOnClickListener(this);
        offSmartplug1Button = (Button)findViewById(R.id.off_smartplug1_button);
        offSmartplug1Button.setOnClickListener(this);
        onSmartplug2Button = (Button)findViewById(R.id.on_smartplug2_button);
        onSmartplug2Button.setOnClickListener(this);
        offSmartplug2Button = (Button)findViewById(R.id.off_smartplug2_button);
        offSmartplug2Button.setOnClickListener(this);
    } //end onCreate()

    //when the user clicks on one of the buttons
    @Override
    public void onClick(View view) {
        if (view == authSmartplugButton) {
            Log.i(TAG, "authSmartplugButton was clicked");
            //must get UUID before from https://www.uuidgenerator.net/version4 - already done on Paul's android
            String surl = "https://wap.tplinkcloud.com";
            String sdata = "{\"method\": \"login\", \"params\": {\"appType\": \"Kasa_Android\", \"cloudUserName\": \"paul.stephen.grant@gmail.com\", \"cloudPassword\": \"hockey13\", \"terminalUUID\": \"4dc0a122-aa41-449e-a464-f137fef2cf67\"}}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
            //Log.i(TAG, "Returned Curl: "+returnedCurl);
            //todo: now parse string into token if want to allow user ability to setup smart plug //08d8afb2-A5zImSQZrdj2uWM5MnWooft 08d8afb2-A1876mDY6ETrqjG66WFrJwx
        }
        if (view == urlSmartplugButton) { //i already have url so i don't think this is necessary
            Log.i(TAG, "urlSmartplugButton was clicked");
            String surl = "https://wap.tplinkcloud.com?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx";  // removed HTTP/1.1 to work
            String sdata = "{\"method\":\"getDeviceList\"}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == onSmartplug1Button) {
            Log.i(TAG, "onSmartplug1Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx"; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":1}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == offSmartplug1Button) {
            Log.i(TAG, "offSmartplug1Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx"; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":0}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == onSmartplug2Button) {
            Log.i(TAG, "onSmartplug2Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx"; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"80069E32EB7ED682EA56429752DDE14A1A25686B\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":1}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == offSmartplug2Button) {
            Log.i(TAG, "offSmartplug2Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx"; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"80069E32EB7ED682EA56429752DDE14A1A25686B\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":0}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
    }
}
