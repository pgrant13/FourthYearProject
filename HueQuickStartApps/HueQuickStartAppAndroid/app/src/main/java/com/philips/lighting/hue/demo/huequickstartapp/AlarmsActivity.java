package com.philips.lighting.hue.demo.huequickstartapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeState;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlarmsActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    private static final String TAG = "AlarmsActivity";

    private Bridge bridge;

    // UI elements
    private TextView statusTextView;
    private TextView bridgeIpTextView;

    private Button cancelAlarmButton;
    private TextView alarmTimeTextView;
    private Button selectAlarmTimeButton;
    private Uri alarmSound; //get the default alarm sound
    private MediaPlayer mp; //make a media player of the alarm sound
    private CheckBox phoneSoundCheckbox;
    private CheckBox hueLightsCheckbox;
    private CheckBox smartPlugCheckbox;
    private CheckBox watchVibrationCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        alarmTimeTextView = (TextView)findViewById(R.id.alarm_time_text);
        selectAlarmTimeButton = (Button)findViewById(R.id.select_alarm_time_button);
        selectAlarmTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });
        cancelAlarmButton = (Button)findViewById(R.id.cancel_alarm_button);
        cancelAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarm();//to implement function
            }
        });

        //media player object to play the alarm sound on phone if selected
        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); //get the default alarm sound
        mp = MediaPlayer.create(getApplicationContext(), alarmSound); //make a media player of the alarm sound
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alarmSound); //make a ringtone of the alarm sound

        registerReceiver(broadcastReceiver, new IntentFilter("ALARM_RECEIVED")); //Alarm Receiver
        registerReceiver(dismissAlarmReceiver, new IntentFilter("DISMISS_ALARM_RECEIVED")); //Dismiss Alarm Receiver

        phoneSoundCheckbox = (CheckBox) findViewById(R.id.phone_sound_checkbox);
        hueLightsCheckbox = (CheckBox) findViewById(R.id.hue_lights_checkbox);
        smartPlugCheckbox = (CheckBox) findViewById(R.id.smart_plug_checkbox);
        watchVibrationCheckbox = (CheckBox) findViewById(R.id.watch_vibration_checkbox);

    } //end of onCreate()

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(dismissAlarmReceiver);
    }

    //---------------------------------------------Hue Default Methods----------------------------------------------------------

    /**
     * Use the KnownBridges API to retrieve the last connected bridge
     * @return Ip address of the last connected bridge, or null
     */
    private String getLastUsedBridgeIp() {
        List<KnownBridge> bridges = KnownBridges.getAll();

        if (bridges.isEmpty()) {
            return null;
        }

        return Collections.max(bridges, new Comparator<KnownBridge>() {
            @Override
            public int compare(KnownBridge a, KnownBridge b) {
                return a.getLastConnected().compareTo(b.getLastConnected());
            }
        }).getIpAddress();
    }

    /**
     * Use the BridgeBuilder to create a bridge instance and connect to it
     */
    private void connectToBridge(String bridgeIp) {

        bridge = new BridgeBuilder("app name", "device name")
                .setIpAddress(bridgeIp)
                .setConnectionType(BridgeConnectionType.LOCAL)
                .setBridgeConnectionCallback(bridgeConnectionCallback)
                .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                .build();

        bridge.connect();

        bridgeIpTextView.setText("Bridge IP: " + bridgeIp);
    }

    /**
     * The callback that receives bridge connection events
     */
    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
            Log.i(TAG, "Connection event: " + connectionEvent);
        }

        @Override
        public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
            for (HueError error : list) {
                Log.e(TAG, "Connection error: " + error.toString());
            }
        }
    };

    /**
     * The callback the receives bridge state update events
     */
    private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
        @Override
        public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
            Log.i(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);
        }
    };
    //--------------------------------------------End of Hue Default code-----------------------------------------------------------------

    // broadcastReceiver from the AlarmReceiver class. Used to call the individual alarm functions
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if the Hue Lights are selected to be used by the user******:
            if (hueLightsCheckbox.isChecked()) {
                alarmHueLights();//turn on the hue lights
            }

            //if phone alarm sound is selected to be used by the user****:
            if (phoneSoundCheckbox.isChecked()) {
                alarmPhoneSound();//turn on the phone sound alarm
            }

            //if smart plug is selected to be used by the user****:
            if (smartPlugCheckbox.isChecked()) {
                turnOnSmartPlug();//turn on the phone sound alarm
            }

            //if watch vibration is selected to be used by the user****:
            /*if (watchVibrationCheckbox.isChecked()) {
                alarmWatchVibration();//turn on the phone sound alarm
            }*/
        }
    };

    // broadcastReceiver from the AlarmReceiver class. Used to call the individual alarm functions
    BroadcastReceiver dismissAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Dismiss Alarm
            dismissAlarmSound();
            //dismissWatchVibration();
            Log.i(TAG, "Received Dismiss Alarm Broadcast");
        }
    };

    /**
     * Turn ON all the lights of the bridge when alarm goes off
     */
    private void alarmHueLights() {
        BridgeState bridgeState = bridge.getBridgeState();
        List<LightPoint> lights = bridgeState.getLights();

        int hueColour = 0; //(0-65535)this will change based on which colour we want. 42337 is sunny. (test is red = 0)
        int hueBrightness = 254; //(0-254)this is the brightness

        for (final LightPoint light : lights) { // this loops through each connected light
            final LightState lightState = new LightState();

            lightState.setOn(true);
            lightState.setHue(hueColour);
            lightState.setBrightness(hueBrightness);

            light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                @Override
                public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                    if (returnCode == ReturnCode.SUCCESS) {
                        Log.i(TAG, "Turned ON light of hue light " + light.getIdentifier());
                    } else {
                        Log.e(TAG, "Error turning ON hue light " + light.getIdentifier());
                        for (HueError error : errorList) {
                            Log.e(TAG, error.toString());
                        }
                    }
                }
            });

        }
    }

    //to sound the phone's alarm
    private void alarmPhoneSound(){
        Log.i(TAG, "turning on phone alarm sound");
        mp.start(); //play the alarm sound
        //r.play(); //play the alarm sound
    }

    //to turn on the smart plug's power
    private void turnOnSmartPlug(){
        Log.i(TAG, "turning on smartplug");
        String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx";
        String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":1}}}\" }}";
        Curl returnedCurl = new Curl();
        returnedCurl.execute(surl, sdata);
    }

    //to turn on the smart plug's power
    private void turnOffSmartPlug(){
        Log.i(TAG, "turning off smartplug");
        String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx";
        String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":0}}}\" }}";
        Curl returnedCurl = new Curl();
        returnedCurl.execute(surl, sdata);
    }

    //to begin vibrations on the smart watch
    private void alarmWatchVibration(){
        Log.i(TAG, "turning on smartwatch vibration");
        //to implement
    }

    //to stop the sound on the phone's alarm
    private void dismissAlarmSound(){
        if (mp.isPlaying()) {
            mp.pause(); //stop the sound from playing
            mp.seekTo(0); //reset the media player to the start of the alarm
        }
    }

    //to stop the vibration on the watch
    private void dismissWatchVibration(){
        // to implement
    }

    //to update the text showing alarm time
    private void updateAlarmTimeText(Calendar c){
        String timeText = "Alarm set for: ";
        //append the short date format (hour and minute) to the string
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        //display the string in the text view
        alarmTimeTextView.setText(timeText);
    }

    //Start the alarm
    private void startAlarm(Calendar c){
        //create a pending intent which will trigger at the moment of the alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        // if the set time is before the current time, add 1 day to the time so the alarm is tomorrow
        /*if (c.before(Calendar.getInstance())){ //comment this out when testing to get instant alarm***
            c.add(Calendar.DATE, 1);
        }*/

        //set the alarm manager to wake up the device with the alarm receiver intent at the exact time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        // use this if we want a repeating alarm daily
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    //Cancel the alarm
    private void cancelAlarm(){
        //same as above for startAlarm()
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        //cancel the pending intent for the alarm
        alarmManager.cancel(pendingIntent);
        alarmTimeTextView.setText("Alarm Canceled");
    }

    // UI methods

    //when the user selects the time of the alarm, we will display the alarm time and activate it
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        updateAlarmTimeText(c);
        startAlarm(c);
    }


}
