package com.edwinfinch.lignite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.edwinfinch.lignite.preference.LigniteCheckBoxPreference;
import com.edwinfinch.lignite.preference.LigniteColourPreference;
import com.edwinfinch.lignite.preference.LigniteEditTextPreference;
import com.edwinfinch.lignite.preference.LigniteListPreference;
import com.edwinfinch.lignite.preference.LigniteNumberPickerPreference;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by edwinfinch on 8/7/15.
 */
public class JSONSettingsActivity extends PreferenceActivity {

    LigniteInfo.App section;
    String name;
    static final String TAG = "JSONSettingsActivity";

    Preference.OnPreferenceChangeListener lastUsedListener;
    Preference lastUsedPreference;
    Object lastSentNewValue;

    /**
     * Checks whether or not the watch acknowledged the messsage that was sent to it.
     * If it doesn't, this will handle it accordingly.
     * @param listener The preference change listener that was used to send the preference.
     * @param attemptedPreference The preference that was attempted to change.
     * @param newValue The new value of the preference that failed to go through.
     * @param failedToAck Whether or not the watch failed to ack the message.
     */
    public void checkForWatchAndHandle(final Preference.OnPreferenceChangeListener listener, final Preference attemptedPreference, final Object newValue, boolean failedToAck){
        lastUsedListener = listener;
        lastUsedPreference = attemptedPreference;
        lastSentNewValue = newValue;

        if(!failedToAck){
            return;
        }

        AlertDialog.Builder errorBuilder = new AlertDialog.Builder(JSONSettingsActivity.this)
            .setMessage(R.string.failed_to_ack)
            .setPositiveButton(R.string.okay, null)
            .setNeutralButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (listener != null) {
                        listener.onPreferenceChange(attemptedPreference, newValue);
                    } else {
                        System.out.println("Preference: " + attemptedPreference);
                        imma_good_listener_babe.onPreferenceClick(attemptedPreference);
                    }
                }
            });
        if(!PebbleKit.isWatchConnected(getApplicationContext())){
            errorBuilder.setMessage(R.string.watch_not_connected);
        }
        errorBuilder.show();
    }

    /**
     * The listener for boolean values.
     */
    final Preference.OnPreferenceClickListener imma_good_listener_babe = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            PebbleDictionary dictionary = new PebbleDictionary();

            //Gets the preference and the item accordingly.
            LigniteCheckBoxPreference lignitePreference = (LigniteCheckBoxPreference)preference;
            try {
                dictionary.addInt32(lignitePreference.item.getInt("pebble_key"), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(preference.getKey(), true) ? 1 : 0);
            } catch(Exception e){
                e.printStackTrace();
            }

            //Sends the data to Pebble
            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section.toInt()]), dictionary);
            if(lignitePreference != null) {
                System.out.println(lignitePreference.getKey() + " " + lignitePreference.getTitle());
            }
            else{
                System.out.println("Preference is null :(");
            }

            //Updates the last handled preferences and stuff in case there is an error.
            checkForWatchAndHandle(null, preference, null, false);
            return false;
        }
    };

    /**
     * The listener for timezone value changes.
     * See the boolean listener for an example of how the base functionality works.
     */
    Preference.OnPreferenceChangeListener timezone_listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            preference.setTitle((String) newValue);

            //Get the offset and send that off
            TimeZone nba = TimeZone.getDefault();
            TimeZone inTheZone = TimeZone.getTimeZone((String)newValue);

            long timeDifference = nba.getRawOffset() - inTheZone.getRawOffset() + nba.getDSTSavings() - inTheZone.getDSTSavings();

            PebbleDictionary dict = new PebbleDictionary();
            dict.addInt32(7, (int)(timeDifference/1000));
            PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(LigniteInfo.UUID[section.toInt()]), dict);

            preference.getSharedPreferences().edit().putString(preference.getKey(), (String)newValue).apply();

            checkForWatchAndHandle(this, preference, newValue, false);
            return false;
        }
    };

    /**
     * The listener for list value changes.
     * See the boolean listener for an example of how the base functionality works.
     */
    Preference.OnPreferenceChangeListener list_listener = new Preference.OnPreferenceChangeListener(){
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            LigniteListPreference listPreference = (LigniteListPreference)preference;

            PebbleDictionary dict = new PebbleDictionary();
            Integer integer = Integer.parseInt((String)newValue);
            preference.setSummary(listPreference.getEntries()[integer]);
            try {
                dict.addInt32(listPreference.item.getInt("pebble_key"), integer);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(LigniteInfo.UUID[section.toInt()]), dict);

            preference.getSharedPreferences().edit().putString(preference.getKey(), preference.getSummary().toString()).apply();

            checkForWatchAndHandle(this, preference, newValue, false);
            return false;
        }
    };

    /**
     * The listener for text value changes.
     * See the boolean listener for an example of how the base functionality works.
     */
    Preference.OnPreferenceChangeListener edittext_listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            preference.setTitle((String)newValue);

            LigniteEditTextPreference lignitePreference = (LigniteEditTextPreference)preference;

            PebbleDictionary dict = new PebbleDictionary();
            try {
                dict.addString(lignitePreference.item.getInt("pebble_key"), (String) newValue);
            } catch (Exception e){
                e.printStackTrace();
            }
            PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(LigniteInfo.UUID[section.toInt()]), dict);

            preference.getSharedPreferences().edit().putString(preference.getKey(), (String) newValue);

            checkForWatchAndHandle(this, preference, newValue, false);
            return false;
        }
    };

    /**
     * The listener for colour changes.
     * See the boolean listener for an example of how the base functionality works.
     */
    Preference.OnPreferenceChangeListener colourListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String hexString = Integer.toHexString((Integer)newValue).substring(2);

            System.out.println("New value: " + hexString);

            LigniteColourPreference colourPref = (LigniteColourPreference) findPreference(preference.getKey());
            colourPref.forceSetValue((Integer) newValue);

            PebbleDictionary dictionary = new PebbleDictionary();
            try {
                dictionary.addString(colourPref.item.getInt("pebble_key"), hexString);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section.toInt()]), dictionary);

            checkForWatchAndHandle(this, preference, newValue, false);
            return false;
        }
    };

    /**
     * The listener for number changes.
     * See the boolean listener for an example of how the base functionality works.
     */
    Preference.OnPreferenceChangeListener numberListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Integer valueInt = (Integer)newValue;

            LigniteNumberPickerPreference lignitePreference = (LigniteNumberPickerPreference)preference;
            
            PebbleDictionary dictionary = new PebbleDictionary();
            try {
                dictionary.addInt32(lignitePreference.item.getInt("pebble_key"), valueInt);
            } catch(Exception e){ e.printStackTrace(); }

            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section.toInt()]), dictionary);

            checkForWatchAndHandle(this, preference, newValue, false);
            return false;
        }
    };

    PebbleKit.PebbleNackReceiver nackRec;
    PebbleKit.PebbleAckReceiver ackRec;

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        name = getIntent().getStringExtra("app_name");
        section = LigniteInfo.getSectionFromAppName(name);

        System.out.println("Setting for " + LigniteInfo.UUID[section.toInt()] + " (" + section + ")");

        /**
         * The nack receiver handles any issues that the watch may have had getting the message.
         * Regardless of the error, the job of this handler is to pass in the most recently used listener,
         * preference, and value to let that handle the rest and maybe even try again if the user wants.
         */
        nackRec = new PebbleKit.PebbleNackReceiver(UUID.fromString(LigniteInfo.UUID[section.toInt()])) {
            @Override
            public void receiveNack(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received nack for transaction " + transactionId);
                checkForWatchAndHandle(lastUsedListener, lastUsedPreference, lastSentNewValue, true);
            }
        };

        /**
         * We may do something (or you) with the ack receiver later, but for now it is just to make
         * sure you know the message went through.
         */
        ackRec = new PebbleKit.PebbleAckReceiver(UUID.fromString(LigniteInfo.UUID[section.toInt()])) {
            @Override
            public void receiveAck(Context context, int transactionId) {
                Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);
            }
        };

        //Registers the handlers.
        PebbleKit.registerReceivedNackHandler(getBaseContext(), nackRec);
        PebbleKit.registerReceivedAckHandler(getBaseContext(), ackRec);

        /*
        PebbleDictionary dictionary = new PebbleDictionary();
        dictionary.addString(LigniteInfo.unlock_keys[section.toInt()][0], LigniteInfo.UUID_endings[section.toInt()]);
        dictionary.addString(LigniteInfo.unlock_keys[section.toInt()][1], LigniteInfo.unlock_tokens[section.toInt()]);
        if(section != LigniteInfo.App.KNIGHTRIDER) {
            PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(LigniteInfo.UUID[section.toInt()]), dictionary);
        }
        */

        //System.out.println("Sending " + LigniteInfo.unlock_keys[section.toInt()][0] + " which is " + LigniteInfo.UUID_endings[section.toInt()] + " and " + LigniteInfo.unlock_keys[section.toInt()][1] + " which is " + LigniteInfo.unlock_tokens[section.toInt()]);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(ackRec);
        unregisterReceiver(nackRec);
    }

    /**
     * Reads a JSON file from assets.
     * @param ctx The context
     * @param assetFilename The JSON file name.
     * @return The JSON file (or just file) as a String.
     */
    public static String readFile(Context ctx, String assetFilename) {
        try {
            File file = new File(ctx.getExternalFilesDir(null), assetFilename);
            InputStream is = ctx.getResources().getAssets().open(assetFilename);
            OutputStream os = new FileOutputStream(file);
            byte[] pbw = new byte[is.available()];
            is.read(pbw);
            os.write(pbw);
            is.close();
            os.close();
            return new String(pbw);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ctx, "Failed to read settings " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return "fucking error mate";
    }

    /**
     * Gets the internationalized string of another string
     * @param aString The string to grab.
     * @return The internationalized version of aString.
     */
    private String getInternationalizedString(String aString) {
        String packageName = getPackageName();
        String updatedString = aString.replaceAll("%d", "x")
                .replaceAll("%", "")
                .replaceAll(" - ", "_")
                .replaceAll("-", "")
                .replaceAll(" ", "_")
                .replaceAll(",", "")
                .replaceAll("/", "_")
                .replaceAll(":", "")
                .replaceAll("\\.", "")
                .toLowerCase();

        int resId = getResources().getIdentifier(updatedString, "string", packageName);
        System.out.println("Searching for string: " + updatedString + " (previously: " + aString + ") returning: " + resId);
        return getString(resId);
    }

    /**
     * The simple preferences screen setup process.
     *
     * Essentially, this shit just runs through a loop and sets up each item with its
     * own listener and the previous value, if one exists.
     */
    private void setupSimplePreferencesScreen() {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getApplicationContext());
        try {
            JSONObject settingsObject = new JSONObject(readFile(this, name + ".json"));
            try {
                String name = settingsObject.getString("name");
                getActionBar().setTitle(WordUtils.capitalize(name));
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    getActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                    getActionBar().setDisplayShowHomeEnabled(false);
                }
            }
            catch(Exception e){
                e.printStackTrace();
                Log.i(TAG, "ERROR MATE");
            }

            JSONArray itemsArray = settingsObject.getJSONArray("items");
            for(int i = 0; i < itemsArray.length(); i++) {
                PreferenceCategory category = new PreferenceCategory(getApplicationContext());
                JSONObject section = itemsArray.getJSONObject(i);
                category.setTitle(getInternationalizedString(section.getString("title")));
                Log.d(TAG, "" + category + " and title " + category.getTitle() + " and screen " + ((screen == null) ? " is null " : " is *not* null "));
                screen.addPreference(category);

                JSONArray items = section.getJSONArray("items");
                for (int i1 = 0; i1 < items.length(); i1++) {
                    JSONObject item = items.getJSONObject(i1);
                    String itemType = item.getString("type");
                    if(itemType.equals("toggle")) {
                        LigniteCheckBoxPreference togglePreference = new LigniteCheckBoxPreference(JSONSettingsActivity.this);
                        togglePreference.item = item;
                        togglePreference.setTitle(getInternationalizedString(item.getString("label")));
                        togglePreference.setKey(item.getString("storage_key"));
                        screen.addPreference(togglePreference);
                        togglePreference.setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                    else if(itemType.equals("colour")){
                        LigniteColourPreference colourPreference = new LigniteColourPreference(JSONSettingsActivity.this, null);
                        colourPreference.item = item;
                        colourPreference.setTitle(getInternationalizedString(item.getString("label")));
                        colourPreference.setKey(item.getString("storage_key"));
                        screen.addPreference(colourPreference);
                        colourPreference.setOnPreferenceChangeListener(colourListener);
                    }
                    else if(itemType.equals("textfield")){
                        LigniteEditTextPreference textFieldPreference = new LigniteEditTextPreference(JSONSettingsActivity.this);
                        textFieldPreference.item = item;
                        textFieldPreference.setKey(item.getString("storage_key"));
                        screen.addPreference(textFieldPreference);
                        textFieldPreference.setOnPreferenceChangeListener(edittext_listener);
                        textFieldPreference.setTitle(textFieldPreference.getSharedPreferences().getString(textFieldPreference.getKey(), "Click to set"));
                    }
                    else if(itemType.equals("timezones")){
                        LigniteListPreference timezonesPreference = new LigniteListPreference(JSONSettingsActivity.this);
                        timezonesPreference.item = item;
                        timezonesPreference.setEntries(TimeZone.getAvailableIDs());
                        timezonesPreference.setEntryValues(TimeZone.getAvailableIDs());
                        timezonesPreference.setOnPreferenceChangeListener(timezone_listener);
                        timezonesPreference.setKey(item.getString("storage_key"));
                        screen.addPreference(timezonesPreference);
                        timezonesPreference.setTitle(timezonesPreference.getSharedPreferences().getString(timezonesPreference.getKey(), "Not set"));
                    }
                    else if(itemType.equals("number_picker")){
                        LigniteNumberPickerPreference numberPickerPreference = new LigniteNumberPickerPreference(JSONSettingsActivity.this, null);
                        numberPickerPreference.item = item;
                        numberPickerPreference.setKey(item.getString("storage_key"));
                        numberPickerPreference.setOnPreferenceChangeListener(numberListener);
                        numberPickerPreference.setValue(0);
                        screen.addPreference(numberPickerPreference);
                    }
                    else if(itemType.equals("list")){
                        LigniteListPreference listPreference = new LigniteListPreference(JSONSettingsActivity.this);
                        listPreference.item = item;
                        listPreference.setKey(item.getString("storage_key"));
                        listPreference.setOnPreferenceChangeListener(list_listener);
                        listPreference.setTitle(getInternationalizedString(item.getString("label")));
                        JSONArray list = item.getJSONArray("list");
                        String[] entries = new String[list.length()];
                        String[] entryValues = new String[list.length()];
                        for(int i2 = 0; i2 < list.length(); i2++){
                            entries[i2] = getInternationalizedString(list.getString(i2));
                            entryValues[i2] = "" + i2;
                        }
                        listPreference.setEntries(entries);
                        listPreference.setEntryValues(entryValues);
                        screen.addPreference(listPreference);
                        listPreference.setSummary(listPreference.getSharedPreferences().getString(listPreference.getKey(), "Click to set"));
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        Log.i(TAG, "setting screen " + screen);
        if(screen == null){
            Log.e(TAG, "Is nulll!!!!!!");
        }
        setPreferenceScreen(screen);
        Log.i(TAG, "set screen");

        setTheme(R.style.SettingsStyle);
        if(android.os.Build.MANUFACTURER == "samsung"){
            //Toast.makeText(ContextManager.ctx, "Your phone is a piece of shit", Toast.LENGTH_LONG).show();
            setTheme(R.style.SamsungStyle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return true;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

                PebbleDictionary dictionary = new PebbleDictionary();
                dictionary.addInt32(listPreference.getOrder()+2, index);
                PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[LigniteInfo.App.SPEEDOMETER.toInt()]), dictionary);
            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
