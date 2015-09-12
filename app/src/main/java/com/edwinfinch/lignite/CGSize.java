package com.edwinfinch.lignite;

import android.widget.FrameLayout;

/**
 * Created by edwinfinch on 9/11/15.
 */
public class CGSize {
    public int width, height;

    public CGSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    public static FrameLayout.LayoutParams layoutParamsFromSize(CGSize size){
        return new FrameLayout.LayoutParams(size.width, size.height);
    }
}
