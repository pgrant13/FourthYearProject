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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "HueQuickStartApp";

    private static final int MAX_HUE = 65535;

    private Bridge bridge;

    private BridgeDiscovery bridgeDiscovery;

    private List<BridgeDiscoveryResult> bridgeDiscoveryResults;

    // UI elements
    private TextView statusTextView;
    private ListView bridgeDiscoveryListView;
    private TextView bridgeIpTextView;
    private View pushlinkImage;
    private Button randomizeLightsButton;
    private Button toggleLightsButton;
    private Button cancelAlarmButton;
    private Button bridgeDiscoveryButton;
    private TextView alarmTimeTextView;
    private Button selectAlarmTimeButton;
    private Uri alarmSound; //get the default alarm sound
    private MediaPlayer mp; //make a media player of the alarm sound
    private AlertDialog.Builder builder; //alert dialog for dismissing alarm
    private CheckBox phoneSoundCheckbox;
    private CheckBox hueLightsCheckbox;
    private CheckBox smartPlugCheckbox;
    private CheckBox watchVibrationCheckbox;

    enum UIState {
        Idle,
        BridgeDiscoveryRunning,
        BridgeDiscoveryResults,
        Connecting,
        Pushlinking,
        Connected
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the UI
        statusTextView = (TextView)findViewById(R.id.status_text);
        bridgeDiscoveryListView = (ListView)findViewById(R.id.bridge_discovery_result_list);
        bridgeDiscoveryListView.setOnItemClickListener(this);
        bridgeIpTextView = (TextView)findViewById(R.id.bridge_ip_text);
        pushlinkImage = findViewById(R.id.pushlink_image);
        bridgeDiscoveryButton = (Button)findViewById(R.id.bridge_discovery_button);
        bridgeDiscoveryButton.setOnClickListener(this);
        randomizeLightsButton = (Button)findViewById(R.id.randomize_lights_button);
        randomizeLightsButton.setOnClickListener(this);
        toggleLightsButton = (Button)findViewById(R.id.toggle_lights_button);
        toggleLightsButton.setOnClickListener(this);
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
        builder = new AlertDialog.Builder(this); //Alert Dialog for Alarm Dismissing

        phoneSoundCheckbox = (CheckBox) findViewById(R.id.phone_sound_checkbox);
        hueLightsCheckbox = (CheckBox) findViewById(R.id.hue_lights_checkbox);
        smartPlugCheckbox = (CheckBox) findViewById(R.id.smart_plug_checkbox);
        watchVibrationCheckbox = (CheckBox) findViewById(R.id.watch_vibration_checkbox);

        // Connect to a bridge or start the bridge discovery
        String bridgeIp = getLastUsedBridgeIp();
        if (bridgeIp == null) {
            startBridgeDiscovery();
        } else {
            connectToBridge(bridgeIp);
        }
    }

    //need an onStop, onRestart, onStart?


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
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
     * Start the bridge discovery search
     * Read the documentation on meethue for an explanation of the bridge discovery options
     */
    private void startBridgeDiscovery() {
        disconnectFromBridge();
        bridgeDiscovery = new BridgeDiscovery();
        // ALL Include [UPNP, IPSCAN, NUPNP] but in some nets UPNP and NUPNP is not working properly
        bridgeDiscovery.search(BridgeDiscovery.BridgeDiscoveryOption.ALL, bridgeDiscoveryCallback);
        updateUI(UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...");
    }

    /**
     * Stops the bridge discovery if it is still running
     */
    private void stopBridgeDiscovery() {
        if (bridgeDiscovery != null) {
            bridgeDiscovery.stop();
            bridgeDiscovery = null;
        }
    }

    /**
     * The callback that receives the results of the bridge discovery
     */
    private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
        @Override
        public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
            // Set to null to prevent stopBridgeDiscovery from stopping it
            bridgeDiscovery = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (returnCode == ReturnCode.SUCCESS) {
                        bridgeDiscoveryListView.setAdapter(new BridgeDiscoveryResultAdapter(getApplicationContext(), results));
                        bridgeDiscoveryResults = results;

                        updateUI(UIState.BridgeDiscoveryResults, "Found " + results.size() + " bridge(s) in the network.");
                    } else if (returnCode == ReturnCode.STOPPED) {
                        Log.i(TAG, "Bridge discovery stopped.");
                    } else {
                        updateUI(UIState.Idle, "Error doing bridge discovery: " + returnCode);
                    }
                }
            });
        }
    };

    /**
     * Use the BridgeBuilder to create a bridge instance and connect to it
     */
    private void connectToBridge(String bridgeIp) {
        stopBridgeDiscovery();
        disconnectFromBridge();

        bridge = new BridgeBuilder("app name", "device name")
                .setIpAddress(bridgeIp)
                .setConnectionType(BridgeConnectionType.LOCAL)
                .setBridgeConnectionCallback(bridgeConnectionCallback)
                .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                .build();

        bridge.connect();

        bridgeIpTextView.setText("Bridge IP: " + bridgeIp);
        updateUI(UIState.Connecting, "Connecting to bridge...");
    }

    /**
     * Disconnect a bridge
     * The hue SDK supports multiple bridge connections at the same time,
     * but for the purposes of this demo we only connect to one bridge at a time.
     */
    private void disconnectFromBridge() {
        if (bridge != null) {
            bridge.disconnect();
            bridge = null;
        }
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
                    updateUI(UIState.Pushlinking, "Press the link button to authenticate.");
                    break;

                case COULD_NOT_CONNECT:
                    updateUI(UIState.Connecting, "Could not connect.");
                    break;

                case CONNECTION_LOST:
                    updateUI(UIState.Connecting, "Connection lost. Attempting to reconnect.");
                    break;

                case CONNECTION_RESTORED:
                    updateUI(UIState.Connected, "Connection restored.");
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
                    updateUI(UIState.Connected, "Connected!");
                    break;

                case LIGHTS_AND_GROUPS:
                    // At least one light was updated.
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * Randomize the color of all lights in the bridge
     * The SDK contains an internal processing queue that automatically throttles
     * the rate of requests sent to the bridge, therefore it is safe to
     * perform all light operations at once, even if there are dozens of lights.
     */
    private void randomizeLights() {
        BridgeState bridgeState = bridge.getBridgeState();
        List<LightPoint> lights = bridgeState.getLights();

        Random rand = new Random();

        for (final LightPoint light : lights) { // this loops through each connected light
            final LightState lightState = new LightState();

            lightState.setHue(rand.nextInt(MAX_HUE));

            light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                @Override
                public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                    if (returnCode == ReturnCode.SUCCESS) {
                        Log.i(TAG, "Changed hue of light " + light.getIdentifier() + " to " + lightState.getHue());
                    } else {
                        Log.e(TAG, "Error changing hue of light " + light.getIdentifier());
                        for (HueError error : errorList) {
                            Log.e(TAG, error.toString());
                        }
                    }
                }
            });
        }
    }

    /**
     * Toggle all the lights of the bridge
     */
    private void toggleLights() {
        BridgeState bridgeState = bridge.getBridgeState();
        List<LightPoint> lights = bridgeState.getLights();

        int hueColour = 42337; //(0-65535)this will change based on which colour we want. 42337 is sunny
        int hueBrightness = 200; //(0-254)this is the brightness

        for (final LightPoint light : lights) { // this loops through each connected light
            //final LightState lightState = new LightState(); //this has no light flickering but can't read current state of the lights
            final LightState lightState = light.getLightState(); //using this to read the state of the lights but it causes flickering
            //of the lights. Probably need to use this to read in the current brightness and increase by xx% each interval

            if (!lightState.isOn()) { //if the light is off, turn it on
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

            else{ //the light is on so turn it off
                lightState.setOn(false);

                light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
                    @Override
                    public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                        if (returnCode == ReturnCode.SUCCESS) {
                            Log.i(TAG, "Turned OFF light of hue light " + light.getIdentifier());
                        } else {
                            Log.e(TAG, "Error turning OFF hue light " + light.getIdentifier());
                            for (HueError error : errorList) {
                                Log.e(TAG, error.toString());
                            }
                        }
                    }
                });
            }
        }
    }

    //---------------------------------------------My Methods--------------------------------------------------

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
        mp.start(); //play the alarm sound
        //r.play(); //play the alarm sound
    }

    //to turn on the smart plug's power
    private void alarmSmartPlug(){
        //to implement
    }

    //to begin vibrations on the smart watch
    private void alarmWatchVibration(){
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
        if (c.before(Calendar.getInstance())){ //comment this out when testing to get instant alarm***
            c.add(Calendar.DATE, 1);
        }

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
            /*if (smartPlugCheckbox.isChecked()) {
                alarmSmartPlug();//turn on the phone sound alarm
            }*/

            //if watch vibration is selected to be used by the user****:
            /*if (watchVibrationCheckbox.isChecked()) {
                alarmWatchVibration();//turn on the phone sound alarm
            }*/

            //Alert message pop up dialog to dismiss alarm ****
            builder.setMessage(R.string.alarm)
                    .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Dismiss Alarm
                            dismissAlarmSound();
                            //dismissWatchVibration();
                        }
                    })
                    .setNegativeButton(R.string.snooze, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Snooze Alarm *** to be implemented
                        }
                    });
            AlertDialog alertdialog = builder.create();
            alertdialog.show();
        }
    };

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

    //when the user clicks on the bridge they wish to connect to
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String bridgeIp = bridgeDiscoveryResults.get(i).getIP();

        connectToBridge(bridgeIp);
    }

    //when the user clicks on one of the buttons
    @Override
    public void onClick(View view) {
        if (view == randomizeLightsButton) {
            randomizeLights();
        }

        if (view == toggleLightsButton) {
            toggleLights();
        }

        if (view == bridgeDiscoveryButton) {
            startBridgeDiscovery();
        }
    }

    private void updateUI(final UIState state, final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Status: " + status);
                statusTextView.setText(status);

                bridgeDiscoveryListView.setVisibility(View.GONE);
                bridgeIpTextView.setVisibility(View.GONE);
                pushlinkImage.setVisibility(View.GONE);
                randomizeLightsButton.setVisibility(View.GONE);
                toggleLightsButton.setVisibility(View.GONE);
                bridgeDiscoveryButton.setVisibility(View.GONE);
                alarmTimeTextView.setVisibility(View.GONE);
                selectAlarmTimeButton.setVisibility(View.GONE);
                cancelAlarmButton.setVisibility(View.GONE);

                switch (state) {
                    case Idle:
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryRunning:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryResults:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                        break;
                    case Connecting:
                        bridgeIpTextView.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case Pushlinking:
                        bridgeIpTextView.setVisibility(View.VISIBLE);
                        pushlinkImage.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case Connected:
                        statusTextView.setVisibility(View.GONE);
                        //randomizeLightsButton.setVisibility(View.VISIBLE);
                        //toggleLightsButton.setVisibility(View.VISIBLE);
                        alarmTimeTextView.setVisibility(View.VISIBLE);
                        selectAlarmTimeButton.setVisibility(View.VISIBLE);
                        cancelAlarmButton.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }
}
