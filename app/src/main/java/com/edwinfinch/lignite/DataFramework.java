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

    /**
     * Makes a POST request
     * @param params The parameters (in proper format, ie. "param1=value1&param2=value=2".
     * @param url The URL to make the request to.
     * @return The String received by making the POST.
     * @throws Exception In case invalid URL is provided, etc.
     */
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

        return response.toString();
    }

    /**
     * Sends a GET request
     * @param url The URL to send the request to.
     * @return The String gotten from the GET request.
     * @throws Exception Yep
     */
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

    /**
     * Wipes anything I've saved in shared preferences before.
     * @param context The context?
     */
    public static void wipeUserDetails(Context context){
        SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
        SharedPreferences.Editor userDetailsEditor = userDetails.edit();

        userDetailsEditor.clear();

        /*
        userDetailsEditor.remove("name");
        userDetailsEditor.remove("accessCode");
        userDetailsEditor.remove("pledged");
        */

        userDetailsEditor.apply();

        SharedPreferences userToken = context.getSharedPreferences(USER_TOKEN_FILE, 0);
        SharedPreferences.Editor userTokenEditor = userToken.edit();

        userTokenEditor.clear();

        //userTokenEditor.remove("token");

        userTokenEditor.apply();
    }

    /**
     * Checks whether or not the access code is set
     * @param context The context.
     * @return A value of true or false (whether or not it's set)
     */
    public static boolean accessCodeIsSet(Context context){
        return (!getAccessCode(context).equals("ERROR1"));
    }

    /**
     * Gets the access code of the user.
     * @param context The context
     * @return The access code as a String
     */
    public static String getAccessCode(Context context){
        SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
        return userDetails.getString("accessCode", "ERROR1");
    }

    /**
     * Sets the access code of the user.
     * @param context The context, used for the preferences
     * @param newAccessCode The access code to write
     * @return A boolean of success. Result will always be true.
     * Todo: make so it can say false in case of an actual fail.
     */
    public static boolean setAccessCode(Context context, String newAccessCode) {
        SharedPreferences userDetails = context.getSharedPreferences(USER_DETAILS_FILE, 0);
        SharedPreferences.Editor userDetailsEditor = userDetails.edit();
        userDetailsEditor.putString("accessCode", newAccessCode);
        userDetailsEditor.apply();
        return true;
    }

    /**
     * Gets the user details (including things like amount pledged) from the internet :O
     * @param context The context, used for stuff
     * @return A JSONObject full of content and awesomeness.
     * Note: send a simple get request to this addess with an access code like EDWINF or TESTER
     * to grab sample data and see what the keys will be.
     */
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

    /**
     * Gets the access token of the user. Didn't we already do this?
     * @param context The context
     * @return The access token as a String
     */
    public static String getAccessToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(USER_TOKEN_FILE, 0);
        String token = preferences.getString("token", "nothing");
        return token;
    }

    /**
     * Verifies the access token of the user.
     * Sometimes, a user may try to bypass the system of one Lignite account on a phone by hacking or whatever.
     * They may have also reset their account on lignite.me/reset/.
     * In either case, we check to make sure their account is up to date, and if it isn't, we automatically log
     * them out of the app to prevent them from using it illegally.
     * @param context The context
     * @param activity The activity which you are launching the check from
     * @param view The view for view purposes
     */
    public void verifyAccessToken(Context context, final Activity activity, final View view){
        try {
            final String code = getAccessCode(context);
            final String token = getAccessToken(context);
            final Context context1 = context;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Sends a post to the server to check
                        String result = sendPost("username=" + code + "&accessToken=" + token, "https://api.lignite.me/v2/checkaccesstoken/index.php");
                        JSONObject object = new JSONObject(result);
                        int status = object.getInt("status");
                        switch (status) {
                            case 200:
                                //Good to go, do nothing
                                break;
                            //If there is any error at all provided by the server, it will log them out.
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

    /**
     * Sets the access token of the user
     * @param context The context
     * @param newToken The new token to write
     */
    public static void setAccessToken(Context context, String newToken){
        SharedPreferences preferences = context.getSharedPreferences(USER_TOKEN_FILE, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", newToken);
        editor.apply();
    }

    /**
     * Gets the user details in JSON format from storage (SharedPreferences)
     * @param context The context
     * @return User details in JSONObject format
     */
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

    /**
     * Sets the user details to storage (SharedPreferences)
     * @param context The context
     * @param details The user details to write. Please make sure that all 3 keys listed below are ALWAYS
     *                included, otherwise it will break.
     * @return The status of whether or not the write was successful.
     */
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

    /**
     * Get whether or not the user has been asked if they are a backer (if they have, you
     * should not do anything)
     * @param context The context
     * @return The boolean of whether or not they have answered the simple popup question.
     */
    public static boolean getTakenBackerQuestion(Context context){
        SharedPreferences backer = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        return backer.getBoolean("has_taken", false);
    }

    /**
     * Sets the status of whether or not they have taken the backer question
     * @param context The context
     * @param has Whether or not the user has taken the question
     */
    public static void setTakenBackerQuestion(Context context, boolean has){
        SharedPreferences taken = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        SharedPreferences.Editor takenEditor = taken.edit();
        takenEditor.putBoolean("has_taken", has);
        takenEditor.apply();
    }

    /**
     * Sets if the user is a backer or not. This is pretty much as low as we get security wise, seriously.
     * @param context The context
     * @param isBacker Whether or not the user is a backer
     */
    public static void setUserIsBacker(Context context, boolean isBacker){
        SharedPreferences backer = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        SharedPreferences.Editor backerEditor = backer.edit();
        backerEditor.putBoolean("said_backer", isBacker);
        backerEditor.apply();
    }

    /**
     * Gets whether or not the user is a backer
     * @param context The context
     * @return Whether or not they are a backer
     */
    public static boolean getUserIsBacker(Context context){
        SharedPreferences backer = context.getSharedPreferences(USER_IS_BACKER_FILE, 0);
        return backer.getBoolean("said_backer", false);
    }
}
