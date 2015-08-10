package com.edwinfinch.lignite.preference;

import android.content.Context;
import android.preference.ListPreference;

import org.json.JSONObject;

/**
 * Created by edwinfinch on 8/10/15.
 */
public class LigniteListPreference extends ListPreference {
    public JSONObject item;
    public LigniteListPreference(Context context){
        super(context);
    }
}
