package com.philips.lighting.hue.demo.huequickstartapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Activity to setup the TPLink smartplug
 */
public class SetupTPLinkActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SetupTPLinkActivity";

    private Button authSmartplugButton;
    private Button urlSmartplugButton;
    private Button onSmartplug1Button;
    private Button offSmartplug1Button;
    private Button onSmartplug2Button;
    private Button offSmartplug2Button;

    private String smartplug1 = "8006D533442D25A6A864522D93217C121A255439";
    private String smartplug2 = "80069E32EB7ED682EA56429752DDE14A1A25686B";
    private String token = "08d8afb2-A2uQ3KFUINONnJLGpGKBwk5";

    //start of Halim's variables
    private final String DEVICE_ADDRESS="00:14:03:06:92:C3";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    Button startButton,clearButton,stopButton;//sendButton
    TextView textView;
    EditText editText;
    boolean deviceConnected=false;
    boolean seeData=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    //end of Halim's variables

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

        //start Halim's onCreate()
        startButton = (Button) findViewById(R.id.buttonStart);
        //sendButton = (Button) findViewById(R.id.buttonSend);
        clearButton = (Button) findViewById(R.id.buttonClear);
        stopButton = (Button) findViewById(R.id.buttonStop);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        setUiEnabled(false);
        //end Halim's onCreate()
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
            //todo: now parse string into token if want to allow user ability to setup smart plug, then input to arraylist/hashmap? //08d8afb2-A5zImSQZrdj2uWM5MnWooft 08d8afb2-A1876mDY6ETrqjG66WFrJwx
        }
        if (view == urlSmartplugButton) { //i already have url so i don't think this is necessary
            Log.i(TAG, "urlSmartplugButton was clicked");
            String surl = "https://wap.tplinkcloud.com?token=08d8afb2-A2uQ3KFUINONnJLGpGKBwk5";  // removed HTTP/1.1 to work
            String sdata = "{\"method\":\"getDeviceList\"}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == onSmartplug1Button) {
            Log.i(TAG, "onSmartplug1Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token="+token+""; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":1}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == offSmartplug1Button) {
            Log.i(TAG, "offSmartplug1Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token="+token+""; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"8006D533442D25A6A864522D93217C121A255439\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":0}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == onSmartplug2Button) {
            Log.i(TAG, "onSmartplug2Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token="+token+""; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"80069E32EB7ED682EA56429752DDE14A1A25686B\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":1}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
        if (view == offSmartplug2Button) {
            Log.i(TAG, "offSmartplug2Button was clicked");
            String surl = "https://use1-wap.tplinkcloud.com/?token="+token+""; //  removed HTTP/1.1 to work
            String sdata = "{\"method\":\"passthrough\", \"params\": {\"deviceId\": \"80069E32EB7ED682EA56429752DDE14A1A25686B\", \"requestData\": \"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":0}}}\" }}";
            Curl returnedCurl = new Curl();
            returnedCurl.execute(surl, sdata);
        }
    }

    //start of imported code from bluetooth heat and humidity sensor

    public void setUiEnabled(boolean bool)
    {
        startButton.setEnabled(!bool);
        //sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        textView.setEnabled(bool);

    }

    public boolean BTinit(){ //Bluetooth Initialize
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    } //end BTinit

    public boolean BTconnect(){ //start Bluetooth connection
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }//end BTConnect

    public void onClickStart(View view) {
        if(BTinit())
        {
            if(BTconnect())
            {
                setUiEnabled(true);
                deviceConnected=true;
                beginListenForData();

//                String string2 = "r";
//                    string2.concat("\n");
//                try {
//                 outputStream.write(string2.getBytes());
//                } catch (IOException e) {
//                  e.printStackTrace();
//                }

                textView.append("\nConnection Opened!\n");

            }

        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        final int byteCount = inputStream.available();
                        if(byteCount > 9)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");

                            final int dataLength = string.length();
                            if (dataLength == 10)
                            {
                                seeData = true;
                            }
                            final String temp =new String(string.substring(0,5));
                            final String humid =new String(string.substring(5,10));
                            final Float temp3 = Float.parseFloat(temp);
                            final Float humid3 = Float.parseFloat(humid);

//                            byte[] tempNum = Arrays.copyOfRange(rawBytes,0,5 );
//                            byte[] humidNum = Arrays.copyOfRange(rawBytes,5,10 );
//                            final String temp2 =new String(tempNum,"UTF-8");
//                            final String humid2 =new String(humidNum,"UTF-8");

//                           final String temp4 =new String(string.substring(0,1));

                            handler.post(new Runnable() {
                                public void run()
                                {
//                                    textView.append(String.valueOf(dataLength));
//                                    textView.setText("");

//                                    if (seeData) {
//                                        textView.append(temp2);
//                                        seeData = false;
//                                    }

                                    textView.append ("Temp = ");
                                    textView.append(String.valueOf(temp3));
                                    textView.append (" Humidity = ");
                                    textView.append(String.valueOf(humid3));
                                    if (humid3>50){
                                        textView.append (" Too High!");
                                    }
                                    textView.append ("\n");
                                }
                            });


                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

//    public void onClickSend(View view) {
//        String string = editText.getText().toString();
//        string.concat("\n");
//          String string2 = ("r");
//            string.concat("\n");
//        try {
//            outputStream.write(string.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        textView.append("\nMonitor Start:"+string+"\n");
//
//    }

    public void onClickStop(View view) throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        setUiEnabled(false);
        deviceConnected=false;
        textView.append("\nConnection Closed!\n");
    }

    public void onClickClear(View view) {
        textView.setText("");
    }
}
