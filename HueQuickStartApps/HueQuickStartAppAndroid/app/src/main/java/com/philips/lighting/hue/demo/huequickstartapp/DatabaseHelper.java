package com.philips.lighting.hue.demo.huequickstartapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DatabaseHelper extends AppCompatActivity {

    private static final String TABLE_NAME = "nights";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static SQLiteDatabase CreateMyDb(){

        String path = "/data/data/com.philips.lighting.hue.demo.huequickstartapp/files/weekly_sleep";
        SQLiteDatabase myDb = SQLiteDatabase.openOrCreateDatabase(path, null);
        String COLUMN1 = "ID";                  String TYPE1 = "INTEGER";
        String COLUMN2 = "EPOCH";               String TYPE2 = "INTEGER";
        String COLUMN3 = "Heart_Rate";          String TYPE3 = "BLOB";
        String COLUMN4 = "Heart_Rate_Time";     String TYPE4 = "BLOB";
        String COLUMN5 = "Accelerometer";       String TYPE5 = "BLOB";
        String COLUMN6 = "Accelerometer_Time";  String TYPE6 = "BLOB";
        String COLUMN7 = "Gyroscope_x";         String TYPE7 = "BLOB";
        String COLUMN8 = "Gyroscope_y";         String TYPE8 = "BLOB";
        String COLUMN9 = "Gyroscope_z";         String TYPE9 = "BLOB";
        String COLUMN10 = "Gyroscope_Time";     String TYPE10 = "BLOB"; //Assuming all 3 axis relate to the same time axis

        myDb.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + COLUMN1 + " " + TYPE1 + " PRIMARY KEY AUTOINCREMENT, "
                + COLUMN2 + " " + TYPE2 + ", "
                + COLUMN3 + " " + TYPE3 + ", "
                + COLUMN4 + " " + TYPE4 + ", "
                + COLUMN5 + " " + TYPE5 + ", "
                + COLUMN6 + " " + TYPE6 + ", "
                + COLUMN7 + " " + TYPE7 + ", "
                + COLUMN8 + " " + TYPE8 + ", "
                + COLUMN9 + " " + TYPE9 + ", "
                + COLUMN10 + " " + TYPE10
                + ")");
        return myDb;
    }
    public static void insertNight(SQLiteDatabase myDb, int epoch, String HR, String HR_time, String accel, String accel_time, String gyro_x, String gyro_y, String gyro_z, String gyro_time) {

        //Check length of the table
        //Using cursor and SQL query to access table
        Cursor epoch_res = myDb.rawQuery("SELECT EPOCH FROM " + TABLE_NAME, null);
        if (epoch_res.getCount() == 0){
            //No record yet, skip to adding record

        }else if (epoch_res.getCount() < 7){
            //Less than 7 records, skip to adding record

        }else{
            //7 records in table, delete the oldest record which will have the smallest epoch value
            //Find smallest epoch value
            long min = 0;
            while (epoch_res.moveToNext()) {
                if (min == 0) {
                    min = epoch_res.getLong(0);
                }
                if (epoch_res.getLong(0) * 1000 < min) {
                    min = epoch_res.getLong(0);
                }
            }
            Log.d("Min epoch: ", Long.toString(min));
            //Deleting record with smallest epoch (oldest record)
            myDb.execSQL("DELETE FROM nights WHERE EPOCH = '" + Long.toString(min) + "'");
        }

        //Insert a new row into the table
        myDb.execSQL("INSERT INTO nights(EPOCH, Heart_Rate, Heart_Rate_Time, Accelerometer, Accelerometer_Time, Gyroscope_x, Gyroscope_y, Gyroscope_z, Gyroscope_Time)" +
                " VALUES (" +
                Integer.toString(epoch) + ", '" +
                HR +"', '"+
                HR_time +"', '"+
                accel +"', '"+
                accel_time +"', '"+
                gyro_x +"', '"+
                gyro_y +"', '"+
                gyro_z +"', '"+
                gyro_time + "'"+
                ")");

        //Closing the cursor
        epoch_res.close();

    }

    public class NightSet{
        public NightSet(){}

        ArrayList<Integer> myID;
        ArrayList<Integer> myEpoch;
        ArrayList<String> myCalendarTime;
        ArrayList<String> myHR;
        ArrayList<String> myHRtime;
        ArrayList<String> myAccel;
        ArrayList<String> myAcceltime;
        ArrayList<String> myGyro_x;
        ArrayList<String> myGyro_y;
        ArrayList<String> myGyro_z;
        ArrayList<String> myGyro_time;
        int myFirst;
        int mySecond;
        int myThird;
        int myFourth;
        int myFifth;
        int mySixth;
        int mySeventh;

        public void set(ArrayList<Integer> ID,
                        ArrayList<Integer> Epoch,
                        ArrayList<String> CalendarTime,
                        ArrayList<String> HR,
                        ArrayList<String> HRtime,
                        ArrayList<String> Accel,
                        ArrayList<String> Acceltime,
                        ArrayList<String> Gyro_x,
                        ArrayList<String> Gyro_y,
                        ArrayList<String> Gyro_z,
                        ArrayList<String> Gyro_time,
                        int first,
                        int second,
                        int third,
                        int fourth,
                        int fifth,
                        int sixth,
                        int seventh)
        {
            this.myID = ID;
            this.myEpoch = Epoch;
            this.myCalendarTime = CalendarTime;
            this.myHR = HR;
            this.myHRtime = HRtime;

            this.myAccel = Accel;
            this.myAcceltime = Acceltime;

            this.myGyro_x = Gyro_x;
            this.myGyro_y = Gyro_y;
            this.myGyro_z = Gyro_z;
            this.myGyro_time = Gyro_time;

            this.myFirst = first;
            this.mySecond = second;
            this.myThird = third;
            this.myFourth = fourth;
            this.myFifth = fifth;
            this.mySixth = sixth;
            this.mySeventh = seventh;
        }
        public ArrayList getID(){return this.myID;}
        public ArrayList getEpoch(){return this.myEpoch;}
        public ArrayList getCalendarTime(){return this.myCalendarTime;}
        public ArrayList getHR(){return this.myHR;}
        public ArrayList getHRtime(){return this.myHRtime;}

        public ArrayList getAccel(){return this.myAccel;}
        public ArrayList getAcceltime(){return this.myAcceltime;}

        public ArrayList getGyro_x(){return this.myGyro_x;}
        public ArrayList getGyro_y(){return this.myGyro_y;}
        public ArrayList getGyro_z(){return this.myGyro_z;}
        public ArrayList getGyro_time(){return this.myGyro_time;}

        public Integer getfirst(){return this.myFirst;}
        public Integer getsecond(){return this.mySecond;}
        public Integer getthird(){return this.myThird;}
        public Integer getfourth(){return this.myFourth;}
        public Integer getfifth(){return this.myFifth;}
        public Integer getsixth(){return this.mySixth;}
        public Integer getseventh(){return this.mySeventh;}
    }

    public NightSet CollectData(SQLiteDatabase myDb) {

        NightSet myNightSet = new NightSet();

        //Querying all data from database
        Cursor res = myDb.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        //Setting up arrays for each data set
        ArrayList<Integer> ID = new ArrayList<>(7);
        ArrayList<Integer> Epoch = new ArrayList<>(7);
        ArrayList<String> HR = new ArrayList<>(7);
        ArrayList<String> HRtime = new ArrayList<>(7);
        ArrayList<String> Accel = new ArrayList<>(7);
        ArrayList<String> Acceltime = new ArrayList<>(7);
        ArrayList<String> Gyro_x = new ArrayList<>(7);
        ArrayList<String> Gyro_y = new ArrayList<>(7);
        ArrayList<String> Gyro_z = new ArrayList<>(7);
        ArrayList<String> Gyro_time = new ArrayList<>(7);

        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_yyyy");
        ArrayList<String> CalenderTime = new ArrayList<>(7);

        //Extracting data from database using Cursor
        if (res.getCount() != 0) { //returns the number of rows in the cursor
            StringBuffer buffer = new StringBuffer();
            while (res.moveToNext()) { //moveToNext moves the cursor to the next row

                ID.add(res.getInt(0));
                Epoch.add(res.getInt(1));
                CalenderTime.add(formatter.format(res.getLong(1) * 1000)); //constructor needs ms
                HR.add(res.getString(2));
                HRtime.add(res.getString(3));
                Accel.add(res.getString(4));
                Acceltime.add(res.getString(5));
                Gyro_x.add(res.getString(6));
                Gyro_y.add(res.getString(7));
                Gyro_z.add(res.getString(8));
                Gyro_time.add(res.getString(9));
            }

            //Before displaying, will need to sort
            // - Considerations need to be made on various table lengths (# of rows)

            //Indexes ordered to display the most recent nights first and oldest night last
            //All set as 8 (an invalid value for our table as it only displays 7 nights)
            int first = 8; int second = 8;
            int third = 8; int fourth = 8;
            int fifth = 8; int sixth = 8;
            int seventh = 8;

            Log.d("Size", Integer.toString(Epoch.size()));

            int count;
            if (Epoch.size() > 0) { //Find max
                int max = 0; count = 0;
                while (count < Epoch.size()) {
                    if (Epoch.get(count) > max) {
                        max = Epoch.get(count); first = count;
                    } count++;
                }
            }
            if (Epoch.size() > 1) { //Find 2nd max
                int max2 = 0; count = 0;
                while (count < Epoch.size()) {
                    if (Epoch.get(count) > max2 && count != first) {
                        max2 = Epoch.get(count); second = count;
                    } count++;
                }
            }
            if (Epoch.size() > 2) { //Find 3rd max
                int max3 = 0; count = 0;
                while (count < Epoch.size()) {
                    if (Epoch.get(count) > max3 && count != first && count != second) {
                        max3 = Epoch.get(count); third = count;
                    } count++;
                }
            }
            if (Epoch.size() > 3) { //Find 4th max
                int max4 = 0; count = 0;
                while (count < Epoch.size()) {
                    if (Epoch.get(count) > max4 && count != first && count != second && count != third) {
                        max4 = Epoch.get(count); fourth = count;
                    } count++;
                }
            }
            if (Epoch.size() > 4) { //Find 5th max
                int max5 = 0; count = 0;
                while (count < Epoch.size()) {
                    if (Epoch.get(count) > max5 && count != first && count != second && count != third && count != fourth) {
                        max5 = Epoch.get(count); fifth = count;
                    } count++;
                }
            }
            if (Epoch.size() > 5) { //Find 6th max
                int max6 = 0; count = 0;
                while (count < Epoch.size()) {
                    if (Epoch.get(count) > max6 && count != first && count != second && count != third && count != fourth && count != fifth) {
                        max6 = Epoch.get(count); sixth = count;
                    } count++;
                }
            }
            if (Epoch.size() > 6) { //Find 7th max (smallest)
                seventh = 1 + 2 + 3 + 4 + 5 + 6 - first - second - third - fourth - fifth - sixth;
            }

            //Debugging
            Log.d("1st: ", Integer.toString(first));
            Log.d("2nd: ", Integer.toString(second));
            Log.d("3rd: ", Integer.toString(third));
            Log.d("4th: ", Integer.toString(fourth));
            Log.d("5th: ", Integer.toString(fifth));
            Log.d("6th: ", Integer.toString(sixth));
            Log.d("7th: ", Integer.toString(seventh));

            myNightSet.set(ID, Epoch, CalenderTime, HR, HRtime, Accel, Acceltime, Gyro_x, Gyro_y, Gyro_z, Gyro_time, first, second, third, fourth, fifth, sixth, seventh);
        }
        return myNightSet;
    }

    public static void CloseMyDb(SQLiteDatabase myDb){
        myDb.close();
    }
    public static void ClearMyDb(SQLiteDatabase myDb){ myDb.execSQL("DELETE FROM "+TABLE_NAME);}

}

