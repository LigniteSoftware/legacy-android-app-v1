package com.edwinfinch.lignite;

import android.content.Context;
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

    final Preference.OnPreferenceClickListener imma_good_listener_babe = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            PebbleDictionary dictionary = new PebbleDictionary();

            LigniteCheckBoxPreference lignitePreference = (LigniteCheckBoxPreference)preference;

            try {
                dictionary.addInt32(lignitePreference.item.getInt("pebble_key"), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(preference.getKey(), true) ? 1 : 0);
            } catch(Exception e){
                e.printStackTrace();
            }

            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section.toInt()]), dictionary);
            System.out.println(preference.getKey() + " " + preference.getTitle());
            return false;
        }
    };

    Preference.OnPreferenceChangeListener timezone_listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            preference.setTitle((String) newValue);

            TimeZone nba = TimeZone.getDefault();
            TimeZone inTheZone = TimeZone.getTimeZone((String)newValue);

            long timeDifference = nba.getRawOffset() - inTheZone.getRawOffset() + nba.getDSTSavings() - inTheZone.getDSTSavings();

            PebbleDictionary dict = new PebbleDictionary();
            dict.addInt32(7, (int)(timeDifference/1000));
            PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(LigniteInfo.UUID[section.toInt()]), dict);

            preference.getSharedPreferences().edit().putString(preference.getKey(), (String)newValue).apply();

            return false;
        }
    };

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
            return false;
        }
    };

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

            preference.getSharedPreferences().edit().putString(preference.getKey(), (String)newValue);

            return false;
        }
    };

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
            return false;
        }
    };

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
            return false;
        }
    };

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        name = getIntent().getStringExtra("app_name");
        section = LigniteInfo.getSectionFromAppName(name);

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

    private String getInternationalizedString(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        System.out.println("Searching for string: " + aString + " returning: " + resId);
        return getString(resId);
    }

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
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
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
