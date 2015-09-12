package com.edwinfinch.lignite;

import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by edwinfinch on 9/11/15.
 */
public class CGRect {
    public CGPoint origin;
    public CGSize size;

    @Override
    public String toString(){
        return "{ x: " + this.origin.x + ", y: " + this.origin.y + ", w: " + this.size.width + ", h: " + this.size.height + " }";
    }

    public CGRect(int x, int y, int width, int height){
        this.origin = new CGPoint(x, y);
        this.size = new CGSize(width, height);
    }

    public CGRect(CGPoint origin, CGSize size){
        this.origin = origin;
        this.size = size;
    }

    public static CGRect getRectFromView(View view){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();

        CGSize size = new CGSize(params.width, params.height);
        CGPoint origin = new CGPoint(view.getTranslationX(), view.getTranslationY());

        return new CGRect(origin, size);
    }

    public static void applyRectToView(CGRect rect, View view){
        view.setTranslationX(rect.origin.x);
        view.setTranslationY(rect.origin.y);
        view.setLayoutParams(CGSize.layoutParamsFromSize(rect.size));
    }
}
