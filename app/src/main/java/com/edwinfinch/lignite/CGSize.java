package com.edwinfinch.lignite;

import android.widget.FrameLayout;

/**
 * Created by edwinfinch on 9/11/15.
 */
public class CGSize {
    public int width, height;

    /**
     * Creates a new CGSize.
     * @param width The width in px
     * @param height The height in px
     */
    public CGSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    /**
     * Gets the LayoutParams from the CGSize
     * @param size The size to grab from.
     * @return The layout params that fit the size.
     */
    public static FrameLayout.LayoutParams layoutParamsFromSize(CGSize size){
        return new FrameLayout.LayoutParams(size.width, size.height);
    }
}
