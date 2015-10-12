package com.edwinfinch.lignite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by edwinfinch on 14-11-16.
 */
public class BackgroundProcess extends BroadcastReceiver {

    /**
     * This shit is what I was talking about with the background processing.
     * This Class was actually just a test that was created a long time ago. Feel free to
     * take it or leave it.
     */

    final String USER_AGENT = "Mozilla/5.0";
    final private static String[] APP_UUIDS = {
            "e1c75a76-27fc-4f9c-85b3-73920ffdb3b7"
    };
    final private static int BATTERY = 0, WEATHER = 1;

    public String sendGet(final String url) {
        try {
            URL obj = new URL(url);
            URLConnection con = obj.openConnection();

            con.addRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.flush();
            wr.close();

            System.out.println("\nSending 'GET' request to URL : " + url);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response : " + response.toString());

            return response.toString();
        } catch(Exception e){ e.printStackTrace(); }
        return null;
    }

    public boolean isApprovedUUID(UUID uuid){
        for(int i = 0; i < APP_UUIDS.length; i++){
            if(APP_UUIDS[i].equals(uuid.toString())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Constants.INTENT_APP_RECEIVE)) {

            final UUID receivedUuid = (UUID) intent.getSerializableExtra(Constants.APP_UUID);

            if (!isApprovedUUID(receivedUuid)) {
                System.out.println("UUID " + receivedUuid.toString() + " is not approved. Fuck you, UUID. Rejecting request.");
                return;
            }

            final String jsonData = intent.getStringExtra(Constants.MSG_DATA);

            try {
                final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
                long result = data.getUnsignedIntegerAsLong(0);
                switch((int)result){
                    case BATTERY:
                        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                        Intent batteryStatus = context.registerReceiver(null, ifilter);
                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        float batteryPct = level / (float)scale;
                        System.out.println("Battery is at " + batteryPct);
                        break;
                    case WEATHER:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    SharedPreferences preferences = context.getSharedPreferences("EXTRA_PREFS", 0);
                                    String url = "http://api.openweathermap.org/data/2.5/weather?q=" + preferences.getString("weatherCity", "Toronto, CA");
                                    JSONObject weatherObject = new JSONObject(sendGet(url));
                                    int temperature = weatherObject.getJSONObject("main").getInt("temp");
                                    int condition = weatherObject.getJSONArray("weather").getJSONObject(0).getInt("id");
                                    System.out.println("It's currently " + temperature + " with a condition of " + condition);
                                    PebbleDictionary dict = new PebbleDictionary();
                                    dict.addInt32(200, temperature);
                                    dict.addInt32(201, condition);
                                    PebbleKit.sendDataToPebble(context, receivedUuid, dict);
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

            final int transactionId = intent.getIntExtra(Constants.TRANSACTION_ID, -1);

            //We gotcha bro. Chill.
            PebbleKit.sendAckToPebble(context, transactionId);
        }
    }

}
