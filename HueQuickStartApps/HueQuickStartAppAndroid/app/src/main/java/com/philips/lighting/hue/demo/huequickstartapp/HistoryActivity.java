package com.philips.lighting.hue.demo.huequickstartapp;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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

        //---------------------------------------------
        //Gathering data to insert into SQL database

        //=============================================
        //Everything inside here was just for testing
        //Test data was generated for storage and graphing

        /*****Remove this section defined by "======="
         * once Ticwatch extraction and storage into SQL database has been integrated*****/

        myHelper.ClearMyDb(myDb); //for testing

        //Obtaining current Epoch
        Long longEpoch = System.currentTimeMillis()/1000;
        String stringEpoch = longEpoch.toString();
        int currentEpoch = Integer.valueOf(stringEpoch);
        Log.d("Current Epoch: ", Integer.toString(currentEpoch));

        ArrayList<String> testing = new ArrayList();
        ArrayList<String> testing2 = new ArrayList();
        ArrayList<String> testing3 = new ArrayList();

        //Can now verify that it works.
        //Important to note that HR, HR time, and Accel time are INT, while Accel is DOUBLE

        testing.add("1");
        testing.add("2");
        testing.add("4");
        testing2.add("4.1");
        testing2.add("5.5");
        testing2.add("6.3");
        testing3.add("9");
        testing3.add("8");
        testing3.add("7");

        final Gson mygson = new Gson();
        String json = mygson.toJson(testing);
        String json2 = mygson.toJson(testing2);
        String json3 = mygson.toJson(testing3);


        /****It seems to think the 5th entry is Accel_time as well as Accel****/
        //Insert data into row
        myHelper.insertNight(myDb, 1352296345, json , json, json3, json, json, json, json, json);
        myHelper.insertNight(myDb, 1452296345, json3, json, json2, json, json, json, json, json);
        myHelper.insertNight(myDb, currentEpoch,      json, json, json , json, json, json, json, json);

        //=============================================

        //Obtaining the needed data from the SQL database
        DatabaseHelper.NightSet myNightSet = myHelper.CollectData(myDb);
        DatabaseHelper.CloseMyDb(myDb);
        final ArrayList<String> times = myNightSet.getCalendarTime();
        final ArrayList<String> HRs = myNightSet.getHR();
        final ArrayList<String> HR_times = myNightSet.getHRtime();
        final ArrayList<String> Accels = myNightSet.getAccel();
        final ArrayList<String> Accel_times = myNightSet.getAcceltime();
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
            //When the user clicks, it will determine which entry the user wants and displays it
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
                //Obtaining HR and Accel JSON strings for the selected date
                ArrayList<String> stringHR = mygson.fromJson(HRs.get(index), ArrayList.class);
                ArrayList<String> stringHR_time = mygson.fromJson(HR_times.get(index), ArrayList.class);
                ArrayList<String> stringAccel = mygson.fromJson(Accels.get(index), ArrayList.class);
                ArrayList<String> stringAccel_time = mygson.fromJson(Accel_times.get(index), ArrayList.class);

                //JSON strings above were converted to ArrayList of strings
                //Converting Strings inside ArrayLists into the intended value types
                ArrayList<Integer> HR = new ArrayList<>();
                ArrayList<Integer> HR_time = new ArrayList<>();
                ArrayList<Double> Accel = new ArrayList<>();
                ArrayList<Integer> Accel_time = new ArrayList<>();


                count = 0;
                while (count < stringHR.size()){
                    HR.add(count, Integer.parseInt(stringHR.get(count)));
                    HR_time.add(count, Integer.parseInt(stringHR_time.get(count)));
                    Log.d("HR: ", stringHR.get(count));
                    Log.d("HR time: ", stringHR_time.get(count));
                    count++;
                }

                count = 0;
                while (count <stringAccel.size()){
                    Accel.add(count, Double.parseDouble(stringAccel.get(count)));
                    Accel_time.add(count, Integer.parseInt(stringAccel_time.get(count)));
                    count++;
                }

                LineGraphSeries<DataPoint> HRseries; //Heart Rate
                LineGraphSeries<DataPoint> LAseries; //Linear Acceleration

                LAseries = new LineGraphSeries<DataPoint>();
                LAseries.setColor(Color.rgb(43, 198, 229));
                LAseries.setThickness(4);

                int start_la = Accel_time.get(0);

                //Altering data to start at zero for the graph
                double max_la_value = -100; //max value for Linear acceleration axis
                double min_la_value = 100; //min value for Linear acceleration axis
                for (int i = 0;i<Accel_time.size(); i++) {

                    Accel_time.set(i, Accel_time.get(i)-start_la);

                    if(Accel.get(i) > max_la_value){ max_la_value = Accel.get(i); }
                    if(Accel.get(i) < min_la_value){ min_la_value = Accel.get(i); }
                    LAseries.appendData(new DataPoint(Accel_time.get(i), Accel.get(i)),true,Accel_time.size());
                }

                //Graphing accelerometer data
                GraphView LA_graph = findViewById(R.id.LAgraph);
                LA_graph.setTitle("Motion Tracker");
                LA_graph.setTitleTextSize(30);
                LA_graph.setTitleColor(Color.BLACK);
                LA_graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (seconds)");
                LA_graph.getGridLabelRenderer().setVerticalAxisTitle("Acceleration (m/s^2)");
                LA_graph.addSeries(LAseries);

                LA_graph.getViewport().setMinX(0);
                LA_graph.getViewport().setMaxX(Accel_time.get(Accel_time.size()-1));

                LA_graph.getViewport().setMinY(min_la_value - 5);
                LA_graph.getViewport().setMaxY(max_la_value + 5);

                LA_graph.getViewport().setYAxisBoundsManual(true);
                LA_graph.getViewport().setXAxisBoundsManual(true);

                LA_graph.getViewport().setScrollable(true); // enables horizontal scrolling
                LA_graph.getViewport().setScrollableY(true); // enables vertical scrolling
                LA_graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
                LA_graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

                HRseries = new LineGraphSeries<DataPoint>();
                HRseries.setColor(Color.rgb(43, 198, 229));
                HRseries.setThickness(4);

                //Altering data to start at zero for the graph
                int max_value = 0; //max value for HR axis
                int min_value = 200; //min value for HR axis
                int start = HR_time.get(0);
                for (int i = 0;i<HR_time.size(); i++){
                    HR_time.set(i, HR_time.get(i) - start);

                    HRseries.appendData(new DataPoint(HR_time.get(i),HR.get(i)),true,HR_time.size());
                    if (HR.get(i) > max_value){ max_value = HR.get(i);}
                    if (HR.get(i) < min_value){ min_value = HR.get(i);}
                }

                //Graphing HR data
                GraphView HR_graph = findViewById(R.id.HRgraph);
                HR_graph.setTitle("Heart Rate Tracker");
                HR_graph.setTitleTextSize(30);
                HR_graph.setTitleColor(Color.BLACK);
                HR_graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (seconds)");
                HR_graph.getGridLabelRenderer().setVerticalAxisTitle("HR (bps)");
                HR_graph.addSeries(HRseries);

                HR_graph.getViewport().setMinX(0);
                HR_graph.getViewport().setMaxX(HR_time.get(HR_time.size()-1));

                HR_graph.getViewport().setMinY(min_value - 5);
                HR_graph.getViewport().setMaxY(max_value + 5);

                HR_graph.getViewport().setYAxisBoundsManual(true);
                HR_graph.getViewport().setXAxisBoundsManual(true);

                HR_graph.getViewport().setScrollable(true); // enables horizontal scrolling
                HR_graph.getViewport().setScrollableY(true); // enables vertical scrolling
                HR_graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
                HR_graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

            }
        });
    } //end onCreate()



}
