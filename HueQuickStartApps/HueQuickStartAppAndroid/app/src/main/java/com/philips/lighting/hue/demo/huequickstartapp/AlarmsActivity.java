package com.philips.lighting.hue.demo.huequickstartapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
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

/**
 * This Activity is responsible for setting the app's alarm and registering the IoT devices
 */
public class AlarmsActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    private static final String TAG = "AlarmsActivity";

    // Hue Bridge
    private Bridge bridge;

    // UI elements
    private Button cancelAlarmButton;
    private TextView alarmTimeTextView;
    private Button selectAlarmTimeButton;
    private Uri alarmSound; //get the default alarm sound
    private MediaPlayer mp; //make a media player of the alarm sound
    private CheckBox phoneSoundCheckbox;
    private CheckBox hueLightsCheckbox;
    private CheckBox smartPlug1Checkbox;
    private CheckBox smartPlug2Checkbox;
    private CheckBox watchVibrationCheckbox;
    private String smartplug1 = "8006D533442D25A6A864522D93217C121A255439";
    private String smartplug2 = "80069E32EB7ED682EA56429752DDE14A1A25686B";

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
                cancelAlarm();// cancels the set alarm
            }
        });

        //media player object to play the alarm sound on phone if selected
        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); //get the default alarm sound
        mp = MediaPlayer.create(getApplicationContext(), alarmSound); //make a media player of the alarm sound
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alarmSound); //make a ringtone of the alarm sound

        registerReceiver(alarmReceiver, new IntentFilter("ALARM_RECEIVED")); //Alarm Receiver
        registerReceiver(dismissAlarmReceiver, new IntentFilter("DISMISS_ALARM_RECEIVED")); //Dismiss Alarm Receiver

        phoneSoundCheckbox = (CheckBox) findViewById(R.id.phone_sound_checkbox);
        hueLightsCheckbox = (CheckBox) findViewById(R.id.hue_lights_checkbox);
        smartPlug1Checkbox = (CheckBox) findViewById(R.id.smartplug1_checkbox);
        smartPlug2Checkbox = (CheckBox) findViewById(R.id.smartplug2_checkbox);
        watchVibrationCheckbox = (CheckBox) findViewById(R.id.watch_vibration_checkbox);

        // Connect to the last used bridge
        String bridgeIp = getLastUsedBridgeIp();
        if (bridgeIp != null) {
            connectToBridge(bridgeIp);
        }

    } //end of onCreate()

    @Override
    protected void onStop() {
        super.onStop();
    } //end of onStop()

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
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
    }

    /**
     * The callback that receives bridge connection events
     */
    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
            Log.i(TAG, "Connection event: " + connectionEvent);

            switch (connectionEvent) {
                case LINK_BUTTON_NOT_PRESSED:
                    break;

                case COULD_NOT_CONNECT:
                    break;

                case CONNECTION_LOST:
                    break;

                case CONNECTION_RESTORED:
                    break;

                case DISCONNECTED:
                    // User-initiated disconnection.
                    break;

                default:
                    break;
            }
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

            switch (bridgeStateUpdatedEvent) {
                case INITIALIZED:
                    // The bridge state was fully initialized for the first time.
                    // It is now safe to perform operations on the bridge state.
                    break;

                case LIGHTS_AND_GROUPS:
                    // At least one light was updated.
                    break;

                default:
                    break;
            }
        }
    };
    //--------------------------------------------End of Hue Default code-----------------------------------------------------------------

    /**
     * alarmReceiver from the AlarmReceiver class. Used to call the individual alarm functions
     */
    BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if the Hue Lights are selected to be used by the user******:
            if (hueLightsCheckbox.isChecked()) {
                turnOnHueLights();//turn on the hue lights
            }

            //if phone alarm sound is selected to be used by the user****:
            if (phoneSoundCheckbox.isChecked()) {
                turnOnPhoneSound();//turn on the phone sound alarm
            }

            //if smart plug is selected to be used by the user****:
            if (smartPlug1Checkbox.isChecked()) {
                setSmartPlugState(smartplug1,"1");//turn on the smartplug1
            }

            //if smart plug is selected to be used by the user****:
            if (smartPlug2Checkbox.isChecked()) {
                setSmartPlugState(smartplug2,"1");//turn on the smartplug2
            }

            //if watch vibration is selected to be used by the user****:
            /*if (watchVibrationCheckbox.isChecked()) {
                turnOnWatchVibration();//turn on the phone sound alarm
            }*/
        }
    };

    /**
     * dismissAlarmReceiver from the dismissAlarmReceiver class. Used to call the individual dismiss alarm functions
     */
    BroadcastReceiver dismissAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Dismiss Alarm
            turnOffPhoneSound();
            setSmartPlugState(smartplug1,"0");//turn off the smartplug1
            setSmartPlugState(smartplug2,"0");//turn off the smartplug2
            //turnOffWatchVibration();
            setCheckboxEditable(true);//allow editing of the alarm checkboxes
            Log.i(TAG, "Received Dismiss Alarm Broadcast");
        }
    };

    /**
     * Turn ON all the lights of the bridge when alarm goes off
     */
    private void turnOnHueLights() {
        BridgeState bridgeState = bridge.getBridgeState();
        List<LightPoint> lights = bridgeState.getLights();

        int hueColour = 8337; //(0-65535)this will change based on which colour we want. 8337 is sunny. (test is red = 0)
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

    /**
     * Turn OFF all the lights of the bridge
     */
    private void turnOffHueLights() {
        BridgeState bridgeState = bridge.getBridgeState();
        List<LightPoint> lights = bridgeState.getLights();

        for (final LightPoint light : lights) { // this loops through each connected light
            final LightState lightState = new LightState();

            lightState.setOn(false);

            light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                @Override
                public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                    if (returnCode == ReturnCode.SUCCESS) {
                        Log.i(TAG, "Turned OFF light of hue light " + light.getIdentifier());
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

    /**
     * Turn ON the phone's alarm sound
     */
    private void turnOnPhoneSound(){
        Log.i(TAG, "turning on phone alarm sound");
        mp.start(); //play the alarm sound
        //r.play(); //play the alarm sound
    }

    /**
     * Turn OFF phone's alarm sound
     */
    private void turnOffPhoneSound(){
        if (mp.isPlaying()) {
            mp.pause(); //stop the sound from playing
            mp.seekTo(0); //reset the media player to the start of the alarm
        }
    }

    /**
     * Set the state of a smartplug
     * @param deviceID the id of the device to control
     * @param state the state to which you wish to set the selected device (0-OFF, 1-ON)
     */
    private void setSmartPlugState(String deviceID, String state){
        Log.i(TAG, "turning on smartplug");
        String surl = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A1876mDY6ETrqjG66WFrJwx";
        String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \""+deviceID+"\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":"+state+"}}}\" }}";
        Curl returnedCurl = new Curl();
        returnedCurl.execute(surl, sdata);
    }

    /**
     * Turn ON the smartwatch vibrations
     */
    private void turnOnWatchVibration(){
        Log.i(TAG, "turning on smartwatch vibration");
        //to implement
    }

    /**
     * Turn OFF the smartwatch vibrations
     */
    private void turnOffWatchVibration(){
        // to implement
    }

    /**
     * Set the state of the Checkboxes
     * @param state of the editable checkbox (true - editable, false - non-editable)
     */
    private void setCheckboxEditable(boolean state){
        phoneSoundCheckbox.setEnabled(state);
        hueLightsCheckbox.setEnabled(state);
        smartPlug1Checkbox.setEnabled(state);
        smartPlug2Checkbox.setEnabled(state);
    }

    /**
     * To update the text showing alarm time
     * @param c Calendar object to update the set alarm text view
     */
    private void updateAlarmTimeText(Calendar c){
        String timeText = "Alarm set for: ";
        //append the short date format (hour and minute) to the string
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        //display the string in the text view
        alarmTimeTextView.setText(timeText);
    }

    /**
     * Set an Alarm
     * @param c Calendar object to set an alarm
     */
    private void startAlarm(Calendar c){
        //create a pending intent which will trigger at the moment of the alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        // if the set time is before the current time, add 1 day to the time so the alarm is tomorrow
        /*if (c.before(Calendar.getInstance())){ //todo: comment this out when testing to get instant alarm***
            c.add(Calendar.DATE, 1);
        }*/

        //set the alarm manager to wake up the device with the alarm receiver intent at the exact time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        // use this if we want a repeating alarm daily
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        setCheckboxEditable(false); //lock editing of alarm checkboxes
    }

    /**
     * Cancels the set alarm
     */
    private void cancelAlarm(){
        //same as above for startAlarm()
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        //cancel the pending intent for the alarm
        alarmManager.cancel(pendingIntent);
        alarmTimeTextView.setText("Alarm Canceled");

        setCheckboxEditable(true); //allow for editing of alarm checkboxes
    }

    /**
     * when the user selects the time of the alarm, we will display the alarm time and activate it
     * @param timePicker
     * @param hour
     * @param minute
     */
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