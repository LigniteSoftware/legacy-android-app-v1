package com.edwinfinch.lignite;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;


import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.TimeZone;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.widget.AmbilWarnaPreference;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class AppSettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = true;
    public int section = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        Bundle extras = getIntent().getExtras();
        int current = extras.getInt("com.edwinfinch.lignite.CURRENT_ITEM");
        System.out.println("current " + current);
        section = current;

        PebbleDictionary dictionary = new PebbleDictionary();
        dictionary.addString(PebbleInfo.unlock_keys[section][0], PebbleInfo.UUID_endings[section]);
        dictionary.addString(PebbleInfo.unlock_keys[section][1], PebbleInfo.unlock_tokens[section]);
        PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(PebbleInfo.UUID[section]), dictionary);
        System.out.println("Sending " + PebbleInfo.UUID[section] + " to watch.");
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
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

    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        final Preference.OnPreferenceClickListener imma_good_listener_babe = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PebbleDictionary dictionary = new PebbleDictionary();
                dictionary.addInt32(preference.getOrder(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(preference.getKey(), true) ? 1 : 0);
                PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(PebbleInfo.UUID[section]), dictionary);
                System.out.println(preference.getKey() + " " + preference.getTitle());
                return false;
            }
        };

        Preference.OnPreferenceChangeListener timezone_listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);

                TimeZone nba = TimeZone.getDefault();
                TimeZone inTheZone = TimeZone.getTimeZone((String)newValue);

                long timeDifference = nba.getRawOffset() - inTheZone.getRawOffset() + nba.getDSTSavings() - inTheZone.getDSTSavings();

                PebbleDictionary dict = new PebbleDictionary();
                dict.addInt32(7, (int)(timeDifference/1000));
                PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(PebbleInfo.UUID[section]), dict);

                preference.getSharedPreferences().edit().putString(preference.getKey(), (String)newValue).apply();

                return false;
            }
        };

        Preference.OnPreferenceChangeListener edittext_listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String)newValue);

                PebbleDictionary dict = new PebbleDictionary();
                dict.addString(preference.getOrder(), (String)newValue);
                PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString(PebbleInfo.UUID[section]), dict);

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
                dictionary.addInt32(preference.getOrder(), newValueBoolean.booleanValue() ? 1 : 0);
                PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(PebbleInfo.UUID[section]), dictionary);
                System.out.println(preference.getKey() + " " + preference.getTitle());

                preference.getPreferenceManager().getSharedPreferences().edit().putBoolean(preference.getKey(), newValueBoolean.booleanValue()).apply();

                CheckBoxPreference checkPref = (CheckBoxPreference)findPreference(preference.getKey());
                checkPref.setChecked(newValueBoolean.booleanValue());

                return false;
            }
        };

        Preference.OnPreferenceChangeListener colourListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String hexString = Integer.toHexString((Integer)newValue).substring(2);

                System.out.println("New value: " + hexString);

                PebbleDictionary dictionary = new PebbleDictionary();
                dictionary.addString(preference.getOrder(), hexString);
                PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(PebbleInfo.UUID[section]), dictionary);

                AmbilWarnaPreference ambilPref = (AmbilWarnaPreference) findPreference(preference.getKey());
                ambilPref.forceSetValue((Integer) newValue);
                return false;
            }
        };

        Preference.OnPreferenceChangeListener numberListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Integer valueInt = (Integer)newValue;

                System.out.println("Sending new value to Pebble: " + valueInt);

                PebbleDictionary dictionary = new PebbleDictionary();
                dictionary.addInt32(preference.getOrder(), valueInt);
                PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(PebbleInfo.UUID[section]), dictionary);
                return false;
            }
        };

        switch(section){
            case PebbleInfo.SPEEDOMETER:
                addPreferencesFromResource(R.xml.spe_pref_general);

                PreferenceCategory fakeHeader = new PreferenceCategory(this);
                fakeHeader.setTitle(R.string.pref_title_lookandfeel);
                getPreferenceScreen().addPreference(fakeHeader);
                addPreferencesFromResource(R.xml.spe_pref_lookandfeel);

                fakeHeader = new PreferenceCategory(this);
                fakeHeader.setTitle(R.string.pref_header_colours);
                getPreferenceScreen().addPreference(fakeHeader);
                addPreferencesFromResource(R.xml.spe_pref_colours);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference){
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                }

                Preference loopPref;
                for(int i = 0; i < PebbleInfo.SETTINGS_COUNT[PebbleInfo.SPEEDOMETER]; i++){
                    loopPref = findPreference(PebbleInfo.SETTINGS_KEYS[PebbleInfo.SPEEDOMETER][i]);
                    if(loopPref == null){
                        Toast.makeText(getApplicationContext(), "Preference " + PebbleInfo.SETTINGS_KEYS[i] + " is null... u w0t m8?", Toast.LENGTH_LONG).show();
                    }
                    else {
                        loopPref.setOnPreferenceChangeListener(booleanListener);
                    }
                }
                break;
            case PebbleInfo.KNIGHTRIDER:
                addPreferencesFromResource(R.xml.knig_pref_general);

                PreferenceCategory fakeHeaderKnig = new PreferenceCategory(this);
                fakeHeaderKnig.setTitle(R.string.pref_title_lookandfeel);
                getPreferenceScreen().addPreference(fakeHeaderKnig);
                addPreferencesFromResource(R.xml.knig_pref_other);

                Preference loopPrefKnig;
                for(int i = 0; i < PebbleInfo.SETTINGS_COUNT[PebbleInfo.KNIGHTRIDER]; i++){
                    loopPrefKnig = findPreference(PebbleInfo.SETTINGS_KEYS[PebbleInfo.KNIGHTRIDER][i]);
                    if(loopPrefKnig == null){
                        Toast.makeText(getApplicationContext(), "Preference " + PebbleInfo.SETTINGS_KEYS[PebbleInfo.KNIGHTRIDER][i] + " is null... u w0t m8?", Toast.LENGTH_LONG).show();
                    }
                    else {
                        loopPrefKnig.setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                }
                break;
            case PebbleInfo.CHUNKY:
                addPreferencesFromResource(R.xml.chu_pref_general);

                PreferenceCategory fakeHeaderChu = new PreferenceCategory(this);
                fakeHeaderChu.setTitle(R.string.pref_title_lookandfeel);
                getPreferenceScreen().addPreference(fakeHeaderChu);
                addPreferencesFromResource(R.xml.chu_pref_lookandfeel);

                fakeHeaderChu = new PreferenceCategory(this);
                fakeHeaderChu.setTitle(R.string.pref_header_colours);
                getPreferenceScreen().addPreference(fakeHeaderChu);
                addPreferencesFromResource(R.xml.chu_pref_colours);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference){
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                }

                Preference loopPrefChu;
                for(int i = 0; i < PebbleInfo.SETTINGS_COUNT[PebbleInfo.CHUNKY]; i++){
                    loopPrefChu = findPreference(PebbleInfo.SETTINGS_KEYS[PebbleInfo.CHUNKY][i]);
                    if(loopPrefChu == null){
                        Toast.makeText(getApplicationContext(), "Preference " + PebbleInfo.SETTINGS_KEYS[PebbleInfo.CHUNKY][i] + " is null... u w0t m8?", Toast.LENGTH_LONG).show();
                    }
                    else {
                        loopPrefChu.setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                }
                break;
            case PebbleInfo.LINES:
                addPreferencesFromResource(R.xml.lin_pref_general);

                PreferenceCategory fakeHeaderLin = new PreferenceCategory(this);
                fakeHeaderLin.setTitle(R.string.pref_title_lookandfeel);
                getPreferenceScreen().addPreference(fakeHeaderLin);
                addPreferencesFromResource(R.xml.lin_pref_lookandfeel);

                fakeHeaderLin = new PreferenceCategory(this);
                fakeHeaderLin.setTitle(R.string.pref_header_colours);
                getPreferenceScreen().addPreference(fakeHeaderLin);
                addPreferencesFromResource(R.xml.lin_pref_colours);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                }

                Preference loopPrefLin;
                for(int i = 0; i < PebbleInfo.SETTINGS_COUNT[PebbleInfo.LINES]; i++){
                    loopPrefLin = findPreference(PebbleInfo.SETTINGS_KEYS[PebbleInfo.LINES][i]);
                    if(loopPrefLin == null){
                        Toast.makeText(getApplicationContext(), "Preference " + PebbleInfo.SETTINGS_KEYS[PebbleInfo.LINES][i] + " is null... u w0t m8?", Toast.LENGTH_LONG).show();
                    }
                    else {
                        loopPrefLin.setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                }
                break;
            case PebbleInfo.COLOURS:
                addPreferencesFromResource(R.xml.col_pref_general);

                PreferenceCategory fakeHeaderCol = new PreferenceCategory(this);
                fakeHeaderCol.setTitle(R.string.pref_title_lookandfeel);
                getPreferenceScreen().addPreference(fakeHeaderCol);
                addPreferencesFromResource(R.xml.col_lookandfeel);

                fakeHeaderCol = new PreferenceCategory(this);
                fakeHeaderCol.setTitle(R.string.pref_header_colours);
                getPreferenceScreen().addPreference(fakeHeaderCol);
                addPreferencesFromResource(R.xml.col_pref_colours);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof NumberPickerPreference){
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(numberListener);
                    }
                }

                Preference loopPrefCol;
                for(int i = 0; i < PebbleInfo.SETTINGS_COUNT[PebbleInfo.COLOURS]; i++){
                    loopPrefCol = findPreference(PebbleInfo.SETTINGS_KEYS[PebbleInfo.COLOURS][i]);
                    if(loopPrefCol == null){
                        Toast.makeText(getApplicationContext(), "Preference " + PebbleInfo.SETTINGS_KEYS[PebbleInfo.COLOURS][i] + " is null... u w0t m8?", Toast.LENGTH_LONG).show();
                    }
                    else {
                        loopPrefCol.setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                }
                break;
            case PebbleInfo.TREE_OF_COLOURS:
                addPreferencesFromResource(R.xml.tre_pref_general);

                PreferenceCategory fakeHeaderTre = new PreferenceCategory(this);
                fakeHeaderTre.setTitle(R.string.pref_title_lookandfeel);
                getPreferenceScreen().addPreference(fakeHeaderTre);
                addPreferencesFromResource(R.xml.tre_pref_lookandfeel);

                fakeHeaderTre = new PreferenceCategory(this);
                fakeHeaderTre.setTitle(R.string.pref_header_custom_colours);
                getPreferenceScreen().addPreference(fakeHeaderTre);
                addPreferencesFromResource(R.xml.tre_custom_colours);

                fakeHeaderTre = new PreferenceCategory(this);
                fakeHeaderTre.setTitle(R.string.pref_header_colours);
                getPreferenceScreen().addPreference(fakeHeaderTre);
                addPreferencesFromResource(R.xml.tre_other_colours);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof NumberPickerPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(numberListener);
                    }
                }

                Preference loopPrefTre;
                for(int i = 0; i < PebbleInfo.SETTINGS_COUNT[PebbleInfo.TREE_OF_COLOURS]; i++){
                    loopPrefTre = findPreference(PebbleInfo.SETTINGS_KEYS[PebbleInfo.TREE_OF_COLOURS][i]);
                    if(loopPrefTre == null){
                        Toast.makeText(getApplicationContext(), "Preference " + PebbleInfo.SETTINGS_KEYS[PebbleInfo.TREE_OF_COLOURS][i] + " is null... u w0t m8?", Toast.LENGTH_LONG).show();
                    }
                    else {
                        loopPrefTre.setOnPreferenceClickListener(imma_good_listener_babe);
                        if(loopPrefTre.getKey().equals(PebbleInfo.SETTINGS_KEYS[PebbleInfo.TREE_OF_COLOURS][2])){
                            loopPrefTre.setOnPreferenceClickListener(custom_colours_listener);
                            for (int i1 = 6; i1 < 9; i1++) {
                                getPreferenceScreen().getPreference(i1).setEnabled(!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(loopPrefTre.getKey(), true));
                            }
                        }
                    }
                }
                break;
            case PebbleInfo.TIMEZONES:
                addPreferencesFromResource(R.xml.tim_pref_general);

                PreferenceCategory fakeHeaderTim = new PreferenceCategory(this);
                fakeHeaderTim.setTitle(R.string.your_time);
                getPreferenceScreen().addPreference(fakeHeaderTim);
                addPreferencesFromResource(R.xml.tim_pref_main_user);

                fakeHeaderTim = new PreferenceCategory(this);
                fakeHeaderTim.setTitle(R.string.other_time);
                getPreferenceScreen().addPreference(fakeHeaderTim);
                addPreferencesFromResource(R.xml.tim_pref_second_user);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof NumberPickerPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(numberListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof CheckBoxPreference){
                        getPreferenceScreen().getPreference(i).setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof ListPreference){
                        ListPreference preference = (ListPreference)getPreferenceScreen().getPreference(i);
                        preference.setEntries(TimeZone.getAvailableIDs());
                        preference.setEntryValues(TimeZone.getAvailableIDs());
                        preference.setOnPreferenceChangeListener(timezone_listener);
                        preference.setSummary(preference.getSharedPreferences().getString(preference.getKey(), "Not set"));
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof EditTextPreference){
                        EditTextPreference pref = (EditTextPreference)getPreferenceScreen().getPreference(i);
                        pref.setSummary(pref.getSharedPreferences().getString(pref.getKey(), "Not set"));
                        pref.setOnPreferenceChangeListener(edittext_listener);
                    }
                }
                break;
            case PebbleInfo.SLOT_MACHINE:
                addPreferencesFromResource(R.xml.slo_pref_general);

                PreferenceCategory fakeHeaderSlo = new PreferenceCategory(this);
                fakeHeaderSlo.setTitle("More");
                getPreferenceScreen().addPreference(fakeHeaderSlo);
                addPreferencesFromResource(R.xml.slo_pref_other);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof NumberPickerPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(numberListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof CheckBoxPreference){
                        getPreferenceScreen().getPreference(i).setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof ListPreference){
                        ListPreference preference = (ListPreference)getPreferenceScreen().getPreference(i);
                        preference.setEntries(TimeZone.getAvailableIDs());
                        preference.setEntryValues(TimeZone.getAvailableIDs());
                        preference.setOnPreferenceChangeListener(timezone_listener);
                        preference.setSummary(preference.getSharedPreferences().getString(preference.getKey(), "Not set"));
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof EditTextPreference){
                        EditTextPreference pref = (EditTextPreference)getPreferenceScreen().getPreference(i);
                        pref.setSummary(pref.getSharedPreferences().getString(pref.getKey(), "Not set"));
                        pref.setOnPreferenceChangeListener(edittext_listener);
                    }
                }
                getPreferenceScreen().getPreference(4).setSummary("Coming soon");
                getPreferenceScreen().getPreference(4).setEnabled(false);
                break;
            case PebbleInfo.PULSE:
                addPreferencesFromResource(R.xml.pul_pref_general);

                PreferenceCategory fakeHeaderPul = new PreferenceCategory(this);
                fakeHeaderPul.setTitle(getString(R.string.pref_title_lookandfeel));
                getPreferenceScreen().addPreference(fakeHeaderPul);
                addPreferencesFromResource(R.xml.pul_pref_look_and_feel);

                fakeHeaderPul = new PreferenceCategory(this);
                fakeHeaderPul.setTitle(getString(R.string.pref_header_custom_colours));
                getPreferenceScreen().addPreference(fakeHeaderPul);
                addPreferencesFromResource(R.xml.pul_pref_colours);

                for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
                    if(getPreferenceScreen().getPreference(i) instanceof AmbilWarnaPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(colourListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof NumberPickerPreference) {
                        getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(numberListener);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof CheckBoxPreference){
                        getPreferenceScreen().getPreference(i).setOnPreferenceClickListener(imma_good_listener_babe);
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof ListPreference){
                        ListPreference preference = (ListPreference)getPreferenceScreen().getPreference(i);
                        preference.setEntries(TimeZone.getAvailableIDs());
                        preference.setEntryValues(TimeZone.getAvailableIDs());
                        preference.setOnPreferenceChangeListener(timezone_listener);
                        preference.setSummary(preference.getSharedPreferences().getString(preference.getKey(), "Not set"));
                    }
                    if(getPreferenceScreen().getPreference(i) instanceof EditTextPreference){
                        EditTextPreference pref = (EditTextPreference)getPreferenceScreen().getPreference(i);
                        pref.setSummary(pref.getSharedPreferences().getString(pref.getKey(), "Not set"));
                        pref.setOnPreferenceChangeListener(edittext_listener);
                    }
                    getPreferenceScreen().getPreference(3).setSummary("Coming soon");
                    getPreferenceScreen().getPreference(3).setEnabled(false);
                    getPreferenceScreen().getPreference(4).setSummary("Coming soon");
                    getPreferenceScreen().getPreference(4).setEnabled(false);
                    getPreferenceScreen().getPreference(5).setSummary("Coming soon");
                    getPreferenceScreen().getPreference(5).setEnabled(false);
                }
                break;
        }

        String[] appNames = getResources().getStringArray(R.array.app_names);
        try {
            getActionBar().setTitle(appNames[section]);
        }
        catch(Exception e){
            e.printStackTrace();
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
                PebbleKit.sendDataToPebble(ContextManager.ctx, UUID.fromString(PebbleInfo.UUID[PebbleInfo.SPEEDOMETER]), dictionary);
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
