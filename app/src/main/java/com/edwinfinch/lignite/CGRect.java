package com.edwinfinch.lignite;

import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by edwinfinch on 9/11/15.
 */
public class CGRect {
    public CGPoint origin;
    public CGSize size;

    /**
     * The offset enum
     */
    public enum CGRectOffset{
        X, Y, WIDTH, HEIGHT
    };

    @Override
    public String toString(){
        return "{ x: " + this.origin.x + ", y: " + this.origin.y + ", w: " + this.size.width + ", h: " + this.size.height + " }";
    }

    /**
     * Initializes a new CGRect with an X coordinate, Y coordinate, width and height
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width in px
     * @param height Height in px
     */
    public CGRect(int x, int y, int width, int height){
        this.origin = new CGPoint(x, y);
        this.size = new CGSize(width, height);
    }

    /**
     * Initializes a new CGRect on a certain point and size
     * @param origin The origin (X and Y) of the CGRect
     * @param size The size (width and height) of the CGRect
     */
    public CGRect(CGPoint origin, CGSize size){
        this.origin = origin;
        this.size = size;
    }

    /**
     * Pulls the CGRect from any view.
     * @param view The view to grab the CGRect from.
     * @return A CGRect that fits the view.
     */
    public static CGRect getRectFromView(View view){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();

        CGSize size = new CGSize(params.width, params.height);
        CGPoint origin = new CGPoint(view.getTranslationX(), view.getTranslationY());

        return new CGRect(origin, size);
    }

    /**
     * Applies a CGRect to a view's translation X and Y, and its layout params.
     * @param rect The CGRect to apply.
     * @param view The view to apply it to.
     */
    public static void applyRectToView(CGRect rect, View view){
        view.setTranslationX(rect.origin.x);
        view.setTranslationY(rect.origin.y);
        view.setLayoutParams(CGSize.layoutParamsFromSize(rect.size));
    }

    /**
     * Applies an offset to the view of [offset] amount of pixels
     * @param offsetType The type of offset (see CGRectOffset)
     * @param offset The offset in px
     * @param view The view to apply the offset to
     */
    public static void applyOffsetToView(CGRectOffset offsetType, int offset, View view){
        CGRect newViewRect = getRectFromView(view);
        switch(offsetType){
            case X:
                newViewRect.origin.x += offset;
                break;
            case Y:
                newViewRect.origin.y += offset;
                break;
            case WIDTH:
                newViewRect.size.width += offset;
                break;
            case HEIGHT:
                newViewRect.size.height += offset;
                break;
        }
        applyRectToView(newViewRect, view);
    }

    /**
     * Creates a CGRect using the same shit that iOS uses
     * @param x X
     * @param y Y
     * @param width width
     * @param height height
     * @return a new CGRect
     */
    public static CGRect CGRectMake(int x, int y, int width, int height){
        return new CGRect(x, y, width, height);
    }
}
