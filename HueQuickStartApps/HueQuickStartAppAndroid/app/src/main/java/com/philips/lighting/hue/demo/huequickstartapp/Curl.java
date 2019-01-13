package com.philips.lighting.hue.demo.huequickstartapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// see https://developer.android.com/reference/java/net/HttpURLConnection
public class Curl extends AsyncTask {

    private static final String TAG = "Curl Class";

    @Override
    protected Object doInBackground(Object[] objects) {
        return null;
    }

    public static void curl(String sURL, String sData) {
        //ie, url = "https://use1-wap.tplinkcloud.com/?token=08d8afb2-A62wJmPMOqaFzYY8vVgoR98 HTTP/1.1";
        //ie, data = "{"method":"passthrough", "params": {"deviceId": "8006D533442D25A6A864522D93217C121A255439", "requestData": "{\"system\":{\"set_relay_state\":{\"state\":1}}}" }}"
        //header is fixed as "Content-Type: application/json"
        HttpURLConnection urlConnection = null;
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(sURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            /*urlConnection.setUseCaches(false);
            //urlConn.setRequestProperty("Host", "android.schoolportal.gr"); //idk what this would be for
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);*/

            Log.i(TAG, "Curl successfully connected");

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream()); //this is causing problems********************
            Log.i(TAG, "1: OutputStreamWriter Created");
            out.write(sData);
            out.close();

            /*DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream ());
            printout.writeBytes(URLEncoder.encode(sData,"UTF-8"));
            Log.i(TAG, "Curl - 4");
            printout.flush ();
            printout.close ();

            /*OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            //writeStream(out);
            Log.i(TAG, "Curl - 4");

            /*OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            Log.i(TAG, "Curl - 6");
            out.write(data);
            Log.i(TAG, "Curl - 7");
            out.close();*/

            Log.i(TAG, "2: Curl wrote");

            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                Log.i(TAG, "3: Curl Reading");
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                Log.i(TAG, "Good HTTP Connection, reader input: " + sb.toString());

            } else {
                Log.i(TAG, "HTTP not OK" + urlConnection.getResponseMessage());
            }
        } catch (MalformedURLException e) {
            Log.i(TAG, "MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, "IOException");
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

    }

}
