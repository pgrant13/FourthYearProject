package com.philips.lighting.hue.demo.huequickstartapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.Calendar;

/**
 * This is the Main Activity which is launched when the app is opened
 * It will display the past sleep
 * It will have buttons to enter the different activities
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SleepTherapyApp";

    // UI elements
    private TextView alarmsTextView;
    private TextView hueSetupTextView;
    private TextView tplinkSetupTextView;
    private TextView sleepTipsTextView;
    private TextView historyTextView;
    private AlertDialog.Builder builder; //alert dialog for dismissing alarm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        // Setup the UI
        alarmsTextView = (TextView)findViewById(R.id.alarms_text);
        alarmsTextView.setOnClickListener(new OnClickListener() {
            // The code in this method will be executed when the alarms tab is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link AlarmsActivity}
                Intent alarmsIntent = new Intent(MainActivity.this, AlarmsActivity.class);
                // Start the new activity
                startActivity(alarmsIntent);
            }
        });
        hueSetupTextView = (TextView)findViewById(R.id.hue_setup_text);
        hueSetupTextView.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the setup hue tab is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link SetupHueActivity}
                Intent setupHueIntent = new Intent(MainActivity.this, SetupHueActivity.class);
                // Start the new activity
                startActivity(setupHueIntent);
            }
        });
        tplinkSetupTextView = (TextView)findViewById(R.id.tplink_setup_text);
        tplinkSetupTextView.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the setup hue tab is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link SetupHueActivity}
                Intent setupTPLinkIntent = new Intent(MainActivity.this, SetupTPLinkActivity.class);
                // Start the new activity
                startActivity(setupTPLinkIntent);
            }
        });
        sleepTipsTextView = (TextView)findViewById(R.id.sleep_tips_text);
        sleepTipsTextView.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the sleep tips tab is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link SleepTipsActivity}
                Intent sleepTipsIntent = new Intent(MainActivity.this, SleepTipsActivity.class);
                // Start the new activity
                startActivity(sleepTipsIntent);
            }
        });
        historyTextView = (TextView)findViewById(R.id.history_text);
        historyTextView.setOnClickListener(new View.OnClickListener(){
            //The code in this method will be executed when the history tab is clicked on.
            @Override
            public void onClick(View view) {
                //Create a new intent to open the {@link HistoryActivity}
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                // Start the new activity
                startActivity(historyIntent);
            }

        });


        registerReceiver(broadcastReceiver, new IntentFilter("ALARM_RECEIVED")); //Alarm Receiver
        builder = new AlertDialog.Builder(this); //Alert Dialog for Alarm Dismissing
    } //end onCreate()

    //need an onStop, onRestart, onStart?


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * alarmReceiver from the AlarmReceiver class. Used to make Alert Dialog Box
      */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received Alarm Broadcast");

            /*//Alert message pop up dialog to dismiss alarm ****
            builder.setMessage(R.string.alarm)
                    .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismissAlarm(); //function to dismiss the alarm
                        }
                    })
                    .setNegativeButton(R.string.snooze, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Snooze Alarm? *** to be implemented
                        }
                    });
            AlertDialog alertdialog = builder.create();
            alertdialog.show();*/
        }
    };

    /**
     * Todo: to stop the alarm sound, send broadcast to AlarmsActivity.java to kill the media player object. Maybe have dismiss notification on all alarm activities??
     * problem is that the Activity dies/is garbage collected so can't kill the media player object. Need UI and Activity to persist
     */
    /*private void dismissAlarm(){
        Log.i(TAG, "Sent Dismiss Alarm Broadcast");
        sendBroadcast(new Intent("DISMISS_ALARM_RECEIVED"));
        //Intent intent = new Intent(this, DismissAlarmReceiver.class);

        *//*Calendar c = Calendar.getInstance();
        //create a pending intent which will trigger at the moment of the alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DismissAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);*//*
    }*/

}
