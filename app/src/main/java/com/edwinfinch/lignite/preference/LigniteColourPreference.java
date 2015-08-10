package com.edwinfinch.lignite.preference;

import android.content.Context;
import android.util.AttributeSet;

import org.json.JSONObject;

import yuku.ambilwarna.widget.AmbilWarnaPreference;

/**
 * Created by edwinfinch on 8/10/15.
 */
public class LigniteColourPreference extends AmbilWarnaPreference {
    public JSONObject item;
    public LigniteColourPreference(Context context, AttributeSet set){
        super(context, set);
    }
}
