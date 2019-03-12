package com.philips.lighting.hue.demo.huequickstartapp;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Activity to display Heart Rate, Accelerometer and Gyroscope data from the SQL database
 */
public class HistoryActivity extends AppCompatActivity{
    private static final String TAG = "HistoryActivity";

    private Spinner dropdown;
    private ArrayAdapter<String> adapter;
    private Button displayGraphButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //------------------------------------------------------------------------------------------
        //Storing data into SQL database
        DatabaseHelper myHelper = new DatabaseHelper();
        SQLiteDatabase myDb = myHelper.CreateMyDb();
        myHelper.ClearMyDb(myDb); //only for testing - we want to clear every time

        //---------------------------------------------
        //Gathering data to insert into SQL database

        //Obtaining current Epoch
        Long longEpoch = System.currentTimeMillis()/1000;
        String stringEpoch = longEpoch.toString();
        int currentEpoch = Integer.valueOf(stringEpoch);
        Log.d("Current Epoch: ", Integer.toString(currentEpoch));

        ArrayList<String> testing = new ArrayList();
        testing.add("1");
        testing.add("2");
        testing.add("3");
        Gson mygson = new Gson();
        String json = mygson.toJson(testing);
        //Insert data into row
        myHelper.insertNight(myDb, 1352296345, json, json, json, json, json, json, json, json);
        myHelper.insertNight(myDb, 1452296345, json, json, json, json, json, json, json, json);
        myHelper.insertNight(myDb, currentEpoch, json, json, json, json, json, json, json, json);
        DatabaseHelper.NightSet myNightSet = myHelper.CollectData(myDb);
        DatabaseHelper.CloseMyDb(myDb);
        final ArrayList<String> times = myNightSet.getCalendarTime();

        //---------------------------------------------
        //------------------------------------------------------------------------------------------


        //get the spinner from the xml.
        dropdown = findViewById(R.id.nightSpinner);
        //create a list of items for the spinner.
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, times);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        displayGraphButton = (Button)findViewById(R.id.displayGraphsButton);
        displayGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when the user clicks on the display graph button

                String selectedDate = String.valueOf(dropdown.getSelectedItem());
                Log.d("Selected Item: ", String.valueOf(dropdown.getSelectedItem()));

                //Call Generate Graph
                //Display data

                //Find index of selected date
                int count = 0;
                int index = -1;
                while (count < times.size()){
                    if(Objects.equals(times.get(count), selectedDate)){
                        index = count;
                    }
                    count++;
                }
                //Continue from here



            }
        });




        ;

    } //end onCreate()



}
