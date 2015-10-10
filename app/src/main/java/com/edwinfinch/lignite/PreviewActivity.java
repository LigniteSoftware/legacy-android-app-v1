package com.edwinfinch.lignite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.edwinfinch.lignite.LigniteInfo.App;
import com.edwinfinch.lignite.LigniteInfo.Pebble;

import static com.edwinfinch.lignite.CGRect.CGRectMake;
import static com.edwinfinch.lignite.LigniteInfo.getAppScreenshot;
import static com.edwinfinch.lignite.LigniteInfo.getPebble;

/**
 * Created by edwinfinch on 9/13/15.
 */
public class PreviewActivity extends AppCompatActivity {
    public App currentApp;
    public Pebble currentPebble;
    public int[] currentScreenshots = new int[2];
    public int[] amountOfScreenshotsForApp = new int[2];
    public static final String PREVIEW_FILE = "previewPreferences";

    ImageSwitcher pebble_view, pebble_screenshot_view;
    ImageView pebble_left_arrow, pebble_right_arrow;

    Button set_default_button;

    ImageView screenshot_view;
    ImageView screenshot_left_arrow, screenshot_right_arrow;

    TextView pebble_title_view, screenshot_title_view;

    public static Pebble getUserSetPebble(){
        try {
            SharedPreferences preferences = ContextManager.ctx.getSharedPreferences(PREVIEW_FILE, 0);

            return Pebble.fromInt(preferences.getInt("default_pebble", Pebble.SNOWY_BLACK.toInt()));
        }
        catch(Exception e){
            e.printStackTrace();
            return Pebble.SNOWY_BLACK;
        }
    }

    public static void setUserSetPebble(Pebble pebble){
        SharedPreferences preferences = ContextManager.ctx.getSharedPreferences(PREVIEW_FILE, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("default_pebble", pebble.toInt());
        editor.apply();
    }

    public static int getDisplayHeight(Display display){
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    public static int getDisplayWidth(Display display){
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public static View getScreenshotView(boolean imageView, CGRect pebbleViewRect, FrameLayout layout){
        double defaultPebbleHeight = 460;
        double defaultScreenshotHeight = 168, defaultScreenshotWidth = 144;
        double heightBetweenPebbleAndScreenshot = 126;

        double scaleFactor = (double)pebbleViewRect.size.height/defaultPebbleHeight;

        double screenshotHeight = scaleFactor*defaultScreenshotHeight;
        double screenshotWidth = scaleFactor*defaultScreenshotWidth;
        double screenshotYOffset = scaleFactor*heightBetweenPebbleAndScreenshot;
        screenshotHeight++;
        screenshotWidth++;

        View screenshot_view;
        if(imageView){
            screenshot_view = new ImageView(ContextManager.ctx);
        }
        else{
            screenshot_view = new ImageSwitcher(ContextManager.ctx);
        }
        CGRect screenshotViewRect = CGRectMake(pebbleViewRect.size.width/2 - (int)screenshotWidth/2, pebbleViewRect.origin.y+(int)screenshotYOffset, (int)screenshotWidth, (int)screenshotHeight);
        CGRect.applyRectToView(screenshotViewRect, screenshot_view);
        layout.addView(screenshot_view);

        return screenshot_view;
    }

    public static ImageSwitcher getPebbleView(FrameLayout layout, int padding, Display display, Pebble pebble, Resources resources, String packageName){
        int width = getDisplayWidth(display);
        int height = getDisplayHeight(display);

        ImageSwitcher pebble_view = new ImageSwitcher(ContextManager.ctx);
        pebble_view.setFactory(imageFactory);
        pebble_view.setImageResource(getPebble(pebble, resources, packageName));
        CGRect pebbleViewRect = CGRectMake(0, 0, width - padding * 2, height / 3);
        CGRect.applyRectToView(pebbleViewRect, pebble_view);
        layout.addView(pebble_view);

        Animation in = AnimationUtils.loadAnimation(ContextManager.ctx, android.R.anim.fade_in);
        pebble_view.setInAnimation(in);
        Animation out = AnimationUtils.loadAnimation(ContextManager.ctx, android.R.anim.fade_out);
        pebble_view.setOutAnimation(out);

        return pebble_view;
    }

    public int getActionBarHeight(){
        TypedValue tv = new TypedValue();
        int actionBarHeight = -1;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    private void updateCurrentPebble(boolean right){
        if(currentPebble == null){
            currentPebble = getUserSetPebble();
        }
        int current = currentPebble.toInt();
        current += right ? 1 : -1;
        if(current < Pebble.getBottomPebble().toInt()){
            current = Pebble.getTopPebble().toInt();
        }
        if(current > Pebble.getTopPebble().toInt()){
            current = Pebble.getBottomPebble().toInt();
        }
        Pebble newPebble = Pebble.fromInt(current);
        pebble_view.setImageResource(getPebble(newPebble, getResources(), getPackageName()));

        currentPebble = newPebble;

        set_default_button.setEnabled(!(currentPebble == getUserSetPebble()));

        updateCurrentScreenshot(true);
    }

    private void updateCurrentScreenshot(boolean right){
        if(currentPebble == null){
            currentPebble = getUserSetPebble();
        }
        int isBasalt = currentPebble.isBasaltInt();
        currentScreenshots[isBasalt] += right ? 1 : -1;
        if(currentScreenshots[isBasalt] < 1){
            currentScreenshots[isBasalt] = amountOfScreenshotsForApp[isBasalt]-1;
        }
        else if(currentScreenshots[isBasalt] > amountOfScreenshotsForApp[isBasalt]-1){
            currentScreenshots[isBasalt] = 1;
        }
        int screenshot = getAppScreenshot(currentApp, currentPebble, currentScreenshots[isBasalt], getResources(), getPackageName());
        screenshot_view.setImageResource(screenshot);
        pebble_screenshot_view.setImageResource(screenshot);
    }

    OnSwipeTouchListener switchPebbleListener = new OnSwipeTouchListener(ContextManager.ctx){
        @Override
        public void onSwipeRight(){
            updateCurrentPebble(true);
        }

        @Override
        public void onSwipeLeft(){
            updateCurrentPebble(false);
        }
    };

    OnSwipeTouchListener switchScreenshotListener = new OnSwipeTouchListener(ContextManager.ctx){
        @Override
        public void onSwipeRight(){
            updateCurrentScreenshot(true);
        }

        @Override
        public void onSwipeLeft(){
            updateCurrentScreenshot(false);
        }
    };

    View.OnClickListener defaultButtonPressed = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setUserSetPebble(currentPebble);
            set_default_button.setEnabled(false);
        }
    };

    public static ViewSwitcher.ViewFactory imageFactory = new ViewSwitcher.ViewFactory() {
        public View makeView() {
            ImageView myView = new ImageView(ContextManager.ctx);
            myView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            return myView;
        }
    };

    @Override
    public void onBackPressed(){
        Intent launchIntent = new Intent(PreviewActivity.this, AppsActivity.class);
        startActivity(launchIntent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_preview_builder);

        Intent sourceIntent = getIntent();
        currentApp = App.fromInt(sourceIntent.getIntExtra("pebbleApp", 0));

        FrameLayout layout = (FrameLayout)findViewById(R.id.previewBuilderLayout);

        Resources resources = getResources();
        String packageName = getPackageName();
        Pebble userSetPebble = getUserSetPebble();

        Toolbar toolbar = (Toolbar)findViewById(R.id.previewToolbar);
        toolbar.setTitle("Preview Builder");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        Typeface helveticaNeue = Typeface.createFromAsset(getAssets(), "HelveticaNeue-Regular.ttf");
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        int displayWidth = getDisplayWidth(defaultDisplay), displayHeight = getDisplayHeight(defaultDisplay);

        final int titleSize = 24;

        pebble_title_view = new TextView(ContextManager.ctx);
        pebble_title_view.setTextSize(titleSize);
        pebble_title_view.setTypeface(helveticaNeue);
        pebble_title_view.setText("Pebble");
        pebble_title_view.setTextColor(Color.BLACK);
        pebble_title_view.setGravity(Gravity.CENTER);
        pebble_title_view.measure(0, 0);
        int titleHeight = pebble_title_view.getMeasuredHeight();
        CGRect pebbleTitleRect = CGRectMake(0, getActionBarHeight()+10, displayWidth, titleHeight+titleHeight/3);
        CGRect.applyRectToView(pebbleTitleRect, pebble_title_view);
        layout.addView(pebble_title_view);

        pebble_view =  getPebbleView(layout, 0, getWindowManager().getDefaultDisplay(), userSetPebble, resources, packageName);
        CGRect.applyOffsetToView(CGRect.CGRectOffset.Y, getActionBarHeight() + pebbleTitleRect.size.height, pebble_view);
        pebble_view.setOnTouchListener(switchPebbleListener);

        pebble_screenshot_view = (ImageSwitcher)getScreenshotView(false, CGRect.getRectFromView(pebble_view), layout);
        pebble_screenshot_view.setFactory(imageFactory);
        Animation in = AnimationUtils.loadAnimation(ContextManager.ctx, android.R.anim.fade_in);
        pebble_screenshot_view.setInAnimation(in);
        Animation out = AnimationUtils.loadAnimation(ContextManager.ctx, android.R.anim.fade_out);
        pebble_screenshot_view.setOutAnimation(out);
        pebble_screenshot_view.setImageResource(getAppScreenshot(currentApp, userSetPebble, 1, resources, packageName));

        CGRect pebbleViewRect = CGRect.getRectFromView(pebble_view);

        int arrowSize = displayHeight/100 * 2;

        pebble_left_arrow = new ImageView(ContextManager.ctx);
        pebble_left_arrow.setImageResource(R.drawable.arrow_active_left);
        CGRect pebbleLeftArrowRect = CGRectMake(50, pebbleViewRect.origin.y + pebbleViewRect.size.height/2, arrowSize, arrowSize);
        CGRect.applyRectToView(pebbleLeftArrowRect, pebble_left_arrow);
        layout.addView(pebble_left_arrow);

        pebble_right_arrow = new ImageView(ContextManager.ctx);
        pebble_right_arrow.setImageResource(R.drawable.arrow_active_right);
        CGRect pebbleRightArrowRect = CGRectMake(displayWidth-50-arrowSize, pebbleViewRect.origin.y + pebbleViewRect.size.height/2, arrowSize, arrowSize);
        CGRect.applyRectToView(pebbleRightArrowRect, pebble_right_arrow);
        layout.addView(pebble_right_arrow);

        set_default_button = new Button(ContextManager.ctx);
        set_default_button.setText("Set as default");
        set_default_button.setTypeface(helveticaNeue);
        set_default_button.measure(0, 0);
        set_default_button.setEnabled(false);
        set_default_button.setOnClickListener(defaultButtonPressed);
        int defaultWidth = set_default_button.getMeasuredWidth()+40;
        CGRect defaultButtonRect = CGRectMake(displayWidth/2 - defaultWidth/2, pebbleViewRect.origin.y+pebbleViewRect.size.height, defaultWidth, set_default_button.getMeasuredHeight() + 5);
        CGRect.applyRectToView(defaultButtonRect, set_default_button);
        layout.addView(set_default_button);

        screenshot_title_view = new TextView(ContextManager.ctx);
        screenshot_title_view.setTextSize(titleSize);
        screenshot_title_view.setTypeface(helveticaNeue);
        screenshot_title_view.setText("Screenshot");
        screenshot_title_view.setTextColor(Color.BLACK);
        screenshot_title_view.setGravity(Gravity.CENTER);
        CGRect screenshotTitleRect = CGRectMake(0, defaultButtonRect.origin.y+defaultButtonRect.size.height, displayWidth, titleHeight);
        CGRect.applyRectToView(screenshotTitleRect, screenshot_title_view);
        layout.addView(screenshot_title_view);

        screenshot_view = (ImageView)getScreenshotView(true, pebbleViewRect, layout);
        screenshot_view.setImageResource(getAppScreenshot(currentApp, userSetPebble, 1, resources, packageName));
        screenshot_view.setOnTouchListener(switchScreenshotListener);
        screenshot_view.setPadding(20, 20, 20, 20);
        int screenshotOrigin = screenshotTitleRect.origin.y+screenshotTitleRect.size.height+titleHeight/4;
        CGRect screenshotRect = CGRectMake(0, screenshotOrigin, displayWidth, displayHeight-screenshotOrigin-getActionBarHeight());
        CGRect.applyRectToView(screenshotRect, screenshot_view);

        screenshot_left_arrow = new ImageView(ContextManager.ctx);
        screenshot_left_arrow.setImageResource(R.drawable.arrow_active_left);
        CGRect screenshotLeftArrowRect = CGRectMake(50, screenshotRect.origin.y + screenshotRect.size.height/2, arrowSize, arrowSize);
        CGRect.applyRectToView(screenshotLeftArrowRect, screenshot_left_arrow);
        layout.addView(screenshot_left_arrow);

        screenshot_right_arrow = new ImageView(ContextManager.ctx);
        screenshot_right_arrow.setImageResource(R.drawable.arrow_active_right);
        CGRect screenshotRightArrowRect = CGRectMake(displayWidth-50-arrowSize, screenshotRect.origin.y + screenshotRect.size.height/2, arrowSize, arrowSize);
        CGRect.applyRectToView(screenshotRightArrowRect, screenshot_right_arrow);
        layout.addView(screenshot_right_arrow);

        amountOfScreenshotsForApp[0] = 1;
        amountOfScreenshotsForApp[1] = 1;
        Pebble[] fixedPebbleArray = {
                Pebble.BIANCA_SILVER, Pebble.SNOWY_BLACK
        };
        for(int i = 0; i < 2; i++){
            while(getAppScreenshot(currentApp, fixedPebbleArray[i], amountOfScreenshotsForApp[i], resources, packageName) != 0){
                amountOfScreenshotsForApp[i]++;
            }
        }
    }
}
