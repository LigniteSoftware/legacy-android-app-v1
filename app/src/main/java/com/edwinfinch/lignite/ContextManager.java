package com.edwinfinch.lignite;

import android.content.Context;

/**
 * Created by edwinfinch on 15-04-05.
 *
 * This shit just holds context in it because Android sort of sucks without it
 */
public class ContextManager {
    public static Context ctx;
    public ContextManager(Context context){
        ctx = context;
    }
}
