package com.edwinfinch.lignite.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;

import org.json.JSONObject;

/**
 * Created by edwinfinch on 8/10/15.
 */
public class LigniteCheckBoxPreference extends CheckBoxPreference {
    public JSONObject item;
    public LigniteCheckBoxPreference(Context context){
        super(context);
    }
}
