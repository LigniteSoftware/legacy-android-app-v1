package com.edwinfinch.lignite;

import android.widget.FrameLayout;

/**
 * Created by edwinfinch on 9/11/15.
 */
public class CGPoint {
    public int x, y;

    /**
     * Initializes a new CGPoint on an X and Y coordinate.
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public CGPoint(float x, float y){
        this.x = (int)x;
        this.y = (int)y;
    }
}
