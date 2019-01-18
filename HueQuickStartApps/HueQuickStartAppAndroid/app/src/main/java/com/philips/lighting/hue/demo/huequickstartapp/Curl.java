package com.philips.lighting.hue.demo.huequickstartapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Curl extends AsyncTask<String, Void, String> {

    private static final String TAG = "Curl Class";

    /**
     * Performs the Http curl (POST) request on a secondary thread
     * @param strings array of Strings to be used in the Http request
     * @return String containing the returned GET from the Http request
     */
    @Override
    protected String doInBackground(String... strings) {
        //ie, url = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A62wJmPMOqaFzYY8vVgoR98 HTTP/1.1";
        //ie, data = "{"method":"passthrough", "params": {"deviceId": "8006D533442D25A6A864522D93217C121A255439", "requestData": "{\"system\":{\"set_relay_state\":{\"state\":1}}}" }}"
        //header is fixed as "Content-Type: application/json"
        String returnCurl = "no curl POST response";

        try {
            URL url = new URL(strings[0]); //url is the first string
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); //open connection to the url
            StringBuilder sb = new StringBuilder();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            //open the output stream and write the curl data to it
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(strings[1]);
            out.close();

            //read the curl response
            int HttpResult =conn.getResponseCode();
            if(HttpResult ==HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) { //having trouble reading multiple lines...maybe the stream points to last line
                    sb.append(line + "\n");
                }
                br.close();
                returnCurl = sb.toString();
                Log.i(TAG, "read curl: "+returnCurl);

            }else{
                Log.i(TAG, "http not okay: "+conn.getResponseMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; //return the curl string returnCurl for parsing to retrieve token, url, device id
    }

}
