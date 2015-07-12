package com.edwinfinch.lignite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by edwinfinch on 15-04-03.
 */
public class DataFramework {
    private static final String USER_AGENT = "Mozilla/6.0";
    private static final String USER_DETAILS_FILE = "UserDetails";
    private static final String USER_TOKEN_FILE = "UserToken";
    private static final String USER_IS_BACKER_FILE = "UserBacker";

    public static String sendPost(String params, String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //Sets the properties
        con.setRequestMethod("POST");
        con.addRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        //Writes the bytes, sends it off
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(params);
        wr.flush();
        wr.close();
        //Gets the response
        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'POST' request to URL : " + url);

        //System.out.println("Parameters: \n" + params);

        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println("Response : " + response.toString());

        return response.toString();
    }

    public static String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //Sets the properties
        con.setRequestMethod("GET");
        con.addRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        //Writes the bytes, sends it off
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.flush();
        wr.close();
        //Gets the response
        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'GET' request to URL : " + url);

        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //System.out.println("Response : " + response.toString());

        return response.toString();
    }

    public static void wipeUserDetails(Context context){
        SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
        SharedPreferences.Editor userDetailsEditor = userDetails.edit();

        userDetailsEditor.remove("name");
        userDetailsEditor.remove("accessCode");
        userDetailsEditor.remove("pledged");

        userDetailsEditor.apply();

        SharedPreferences userToken = context.getSharedPreferences(USER_TOKEN_FILE, 0);
        SharedPreferences.Editor userTokenEditor = userToken.edit();

        userTokenEditor.remove("token");

        userTokenEditor.apply();
    }

    public static boolean accessCodeIsSet(Context context){
        return (!getAccessCode(context).equals("ERROR1"));
    }

    public static String getAccessCode(Context context){
        SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
        return userDetails.getString("accessCode", "ERROR1");
    }

    public static boolean setAccessCode(Context context, String newAccessCode) {
        SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
        SharedPreferences.Editor userDetailsEditor = userDetails.edit();
        userDetailsEditor.putString("accessCode", newAccessCode);
        userDetailsEditor.apply();
        return true;
    }

    public static JSONObject getUserDetailsFromInternet(Context context){
        String result;
        JSONObject parseObject = null;
        try {
            result = sendGet("https://api.lignite.me/v2/backer/get_info_with_code.php?username=" + getAccessCode(context));
            parseObject = new JSONObject(result);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return parseObject;
    }

    public static String getAccessToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(USER_TOKEN_FILE, 0);
        String token = preferences.getString("token", "nothing");
        return token;
    }

    public void verifyAccessToken(Context context, final Activity activity, final View view){
        try {
            final String code = getAccessCode(context);
            final String token = getAccessToken(context);
            final Context context1 = context;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result = sendPost("username=" + code + "&accessToken=" + token, "https://api.lignite.me/v2/checkaccesstoken/index.php");
                        JSONObject object = new JSONObject(result);
                        int status = object.getInt("status");
                        switch (status) {
                            case 200:
                                //Good to go, do nothing
                                break;
                            default:
                                view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context1, "Illegally logged in. Logging out!", Toast.LENGTH_LONG).show();
                                        DataFramework.wipeUserDetails(context1);
                                        Intent launchMain = new Intent(activity, LoginActivity.class);
                                        activity.startActivity(launchMain);
                                        activity.finish();
                                    }
                                });
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void setAccessToken(Context context, String newToken){
        SharedPreferences preferences = context.getSharedPreferences(USER_TOKEN_FILE, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", newToken);
        editor.apply();
    }

    public static JSONObject getUserDetailsFromStorage(Context context){
        JSONObject object = null;
        try {
            object = new JSONObject();
            SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
            object.put("name", userDetails.getString("name", "my friend"));
            object.put("pledged", userDetails.getString("pledged", "something"));
            object.put("number", userDetails.getInt("number", 1337));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return object;
    }

    public static boolean setUserDetails(Context context, JSONObject details){
        try {
            SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
            SharedPreferences.Editor userDetailsEditor = userDetails.edit();
            userDetailsEditor.putString("name", details.getString("name"));
            userDetailsEditor.putString("pledged", details.getString("pledged"));
            userDetailsEditor.putInt("number", details.getInt("number"));
            userDetailsEditor.apply();
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean getTakenBackerQuestion(Context context){
        SharedPreferences backer = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        return backer.getBoolean("has_taken", false);
    }

    public static void setTakenBackerQuestion(Context context, boolean has){
        SharedPreferences taken = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        SharedPreferences.Editor takenEditor = taken.edit();
        takenEditor.putBoolean("has_taken", has);
        takenEditor.apply();
    }

    public static void setUserIsBacker(Context context, boolean isBacker){
        SharedPreferences backer = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        SharedPreferences.Editor backerEditor = backer.edit();
        backerEditor.putBoolean("said_backer", isBacker);
        backerEditor.apply();
    }

    public static boolean getUserIsBacker(Context context){
        SharedPreferences backer = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        return backer.getBoolean("said_backer", false);
    }
}
