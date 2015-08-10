package com.edwinfinch.lignite.preference;

import android.content.Context;
import android.preference.EditTextPreference;

import org.json.JSONObject;

/**
 * Created by edwinfinch on 8/10/15.
 */
public class LigniteEditTextPreference extends EditTextPreference{
    public JSONObject item;
    public LigniteEditTextPreference(Context context){
        super(context);
    }
}
