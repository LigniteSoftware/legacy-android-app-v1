package com.edwinfinch.lignite;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.edwinfinch.lignite.preference.LigniteCheckBoxPreference;
import com.edwinfinch.lignite.preference.LigniteColourPreference;
import com.edwinfinch.lignite.preference.LigniteEditTextPreference;
import com.edwinfinch.lignite.preference.LigniteListPreference;
import com.edwinfinch.lignite.preference.LigniteNumberPickerPreference;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by edwinfinch on 8/7/15.
 */
public class JSONSettingsActivity extends PreferenceActivity {

    int section = 0;
    String name;
    static final boolean ALWAYS_SIMPLE_PREFS = true;
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

            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section]), dictionary);
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
            PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(LigniteInfo.UUID[section]), dict);

            preference.getSharedPreferences().edit().putString(preference.getKey(), (String)newValue).apply();

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
            PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(LigniteInfo.UUID[section]), dict);

            preference.getSharedPreferences().edit().putString(preference.getKey(), (String)newValue);

            return false;
        }
    };

    Preference.OnPreferenceClickListener custom_colours_listener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            imma_good_listener_babe.onPreferenceClick(preference);
            for (int i = 6; i < 9; i++) {
                getPreferenceScreen().getPreference(i).setEnabled(!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(preference.getKey(), true));
            }
            return false;
        }
    };

    Preference.OnPreferenceChangeListener booleanListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            PebbleDictionary dictionary = new PebbleDictionary();
            Boolean newValueBoolean = (Boolean)newValue;
            dictionary.addInt32(preference.getOrder(), newValueBoolean ? 1 : 0);
            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section]), dictionary);
            System.out.println(preference.getKey() + " " + preference.getTitle());

            preference.getPreferenceManager().getSharedPreferences().edit().putBoolean(preference.getKey(), newValueBoolean).apply();

            CheckBoxPreference checkPref = (CheckBoxPreference)findPreference(preference.getKey());
            checkPref.setChecked(newValueBoolean);

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
            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section]), dictionary);
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

            PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[section]), dictionary);
            return false;
        }
    };

    @Override
    public void onCreate(Bundle saved){
        super.onCreate(saved);
        name = getIntent().getStringExtra("app_name");
        section = LigniteInfo.getSectionFromAppName(name);
        setupActionBar();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            //getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private String readFromFile(String fileName) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public String newReadFromFile(String fileName){
        String path = this.getFilesDir().getAbsolutePath();
        File file = new File(path + "/speedometer.json");
        int length = (int) file.length();

        try {
            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }
            String contents = new String(bytes);
            return contents;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return "fucking error";
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
        System.out.println(aString + " and " + resId);
        return getString(resId);
    }

    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);

        try {
            JSONObject settingsObject = new JSONObject(readFile(this, name + ".json"));

            try {
                String name = settingsObject.getString("name");
                getActionBar().setTitle(name.substring(0, 1).toUpperCase() + name.substring(1));
            }
            catch(Exception e){
                e.printStackTrace();
            }

            JSONArray itemsArray = settingsObject.getJSONArray("items");
            for(int i = 0; i < itemsArray.length(); i++) {
                PreferenceCategory category = new PreferenceCategory(this);
                JSONObject section = itemsArray.getJSONObject(i);
                category.setTitle(getInternationalizedString(section.getString("title")));
                screen.addPreference(category);

                JSONArray items = section.getJSONArray("items");
                for (int i1 = 0; i1 < items.length(); i1++) {
                    JSONObject item = items.getJSONObject(i1);
                    String itemType = item.getString("type");
                    if(itemType.equals("toggle")) {
                        LigniteCheckBoxPreference togglePreference = new LigniteCheckBoxPreference(this);
                        togglePreference.item = item;
                        togglePreference.setTitle(getInternationalizedString(item.getString("label")));
                        togglePreference.setKey(item.getString("storage_key"));
                        screen.addPreference(togglePreference);
                        togglePreference.setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                    else if(itemType.equals("colour")){
                        LigniteColourPreference colourPreference = new LigniteColourPreference(this, null);
                        colourPreference.item = item;
                        colourPreference.setTitle(getInternationalizedString(item.getString("label")));
                        colourPreference.setKey(item.getString("storage_key"));
                        screen.addPreference(colourPreference);
                        colourPreference.setOnPreferenceChangeListener(colourListener);
                    }
                    else if(itemType.equals("textfield")){
                        LigniteEditTextPreference textFieldPreference = new LigniteEditTextPreference(this);
                        textFieldPreference.item = item;
                        textFieldPreference.setKey(item.getString("storage_key"));
                        screen.addPreference(textFieldPreference);
                        textFieldPreference.setOnPreferenceChangeListener(edittext_listener);
                        textFieldPreference.setTitle(textFieldPreference.getSharedPreferences().getString(textFieldPreference.getKey(), "Click to set"));
                    }
                    else if(itemType.equals("timezones")){
                        LigniteListPreference timezonesPreference = new LigniteListPreference(this);
                        timezonesPreference.item = item;
                        timezonesPreference.setEntries(TimeZone.getAvailableIDs());
                        timezonesPreference.setEntryValues(TimeZone.getAvailableIDs());
                        timezonesPreference.setOnPreferenceChangeListener(timezone_listener);
                        timezonesPreference.setKey(item.getString("storage_key"));
                        screen.addPreference(timezonesPreference);
                        timezonesPreference.setTitle(timezonesPreference.getSharedPreferences().getString(timezonesPreference.getKey(), "Not set"));
                    }
                    else if(itemType.equals("number_picker")){
                        LigniteNumberPickerPreference numberPickerPreference = new LigniteNumberPickerPreference(this, null);
                        numberPickerPreference.item = item;
                        numberPickerPreference.setKey(item.getString("storage_key"));
                        numberPickerPreference.setOnPreferenceChangeListener(numberListener);
                        screen.addPreference(numberPickerPreference);
                    }
                }
            }

            setPreferenceScreen(screen);
        }
        catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
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
                PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[LigniteInfo.SPEEDOMETER]), dictionary);
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
