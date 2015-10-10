package com.edwinfinch.lignite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.app.ActionBar.LayoutParams;

import org.apache.commons.lang3.text.WordUtils;

import static com.edwinfinch.lignite.CGRect.CGRectMake;
import static com.edwinfinch.lignite.LigniteInfo.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AppFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AppFragment} factory method to
 * create an instance of this fragment.
 */
public class AppFragment extends android.support.v4.app.Fragment {
    private App type;
    private boolean other = false;
    private boolean purchased = false;

    public AppsActivity sourceActivity;

    ImageView install_button, settings_button, left_arrow_view, right_arrow_view;
    ImageSwitcher pebble_view, screenshot_view;
    ScrollView textScrollParentView;
    TextView textScrollView, titleView;

    public void setPurchased(boolean purchasedornot){
        purchased = purchasedornot;
        if(isAdded()) {
            System.out.println("Setting " + type + " as purchased to " + purchasedornot);
            settings_button.setImageResource(purchased ? R.drawable.settings_button : R.drawable.purchase_button);
        }
    }

    public AppFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = App.fromInt(getArguments().getInt("t")-1);
        }
    }

    static public int getNavigationBarHeight(Context c, boolean is_tablet, Resources resources) {
        int result = 0;
        boolean hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if(!hasMenuKey && !hasBackKey) {
            //The device has a navigation bar

            int orientation = resources.getConfiguration().orientation;
            int resourceId;
            if (is_tablet){
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            }  else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }


    public static boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public ViewSwitcher.ViewFactory imageFactory = new ViewSwitcher.ViewFactory() {
        public View makeView() {
            ImageView myView = new ImageView(ContextManager.ctx);
            myView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            return myView;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.fragment_test, container, false);
        int padding = 50;
        layout.setPadding(padding, padding, padding, padding / 2);

        //FrameLayout layout = new FrameLayout(ContextManager.ctx);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        pebble_view = PreviewActivity.getPebbleView(layout, padding, getActivity().getWindowManager().getDefaultDisplay(), PreviewActivity.getUserSetPebble(), getResources(), getActivity().getPackageName());
        CGRect.applyOffsetToView(CGRect.CGRectOffset.HEIGHT, 180, pebble_view);
        pebble_view.setOnClickListener(sourceActivity.previewBuilderListener);

        CGRect pebbleViewRect = CGRect.getRectFromView(pebble_view);
        screenshot_view = (ImageSwitcher)PreviewActivity.getScreenshotView(false, pebbleViewRect, layout);
        screenshot_view.setFactory(PreviewActivity.imageFactory);
        screenshot_view.setImageResource(getAppScreenshot(type, PreviewActivity.getUserSetPebble(), 1, getResources(), getActivity().getPackageName()));
        screenshot_view.setOnClickListener(sourceActivity.previewBuilderListener);

        Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_out_right);
        in.setInterpolator(new AnticipateOvershootInterpolator());
        out.setInterpolator(new AnticipateOvershootInterpolator());

        left_arrow_view = new ImageView(ContextManager.ctx);
        left_arrow_view.setImageResource(R.drawable.arrow_active_left);
        left_arrow_view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        CGRect leftArrowRect = CGRectMake(0, pebbleViewRect.origin.y + pebbleViewRect.size.height / 2 - 30, 30, 30);
        CGRect.applyRectToView(leftArrowRect, left_arrow_view);
        layout.addView(left_arrow_view);

        right_arrow_view = new ImageView(ContextManager.ctx);
        right_arrow_view.setImageResource(R.drawable.arrow_active_right);
        right_arrow_view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        CGRect rightArrowRect = CGRectMake(width - 40 - padding * 2, pebbleViewRect.origin.y + pebbleViewRect.size.height / 2, 30, 30);
        CGRect.applyRectToView(rightArrowRect, right_arrow_view);
        layout.addView(right_arrow_view);

        if(type == App.SPEEDOMETER){
            left_arrow_view.setImageResource(R.drawable.arrow_inactive_left);
        }
        else if(type.toInt() == AMOUNT_OF_APPS-1){
            right_arrow_view.setImageResource(R.drawable.arrow_inactive_right);
        }

        Typeface helveticaNeue = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeue-Regular.ttf");

        titleView = new TextView(ContextManager.ctx);
        titleView.setText(WordUtils.capitalize(NAME[type.toInt()]));
        titleView.setTextColor(Color.BLACK);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(helveticaNeue);
        titleView.setTextSize(26);
        titleView.measure(0, 0);
        int titleHeight = titleView.getMeasuredHeight();
        CGRect titleFrame = CGRectMake(0, pebbleViewRect.size.height + pebbleViewRect.origin.y - padding, pebbleViewRect.size.width, titleHeight+10);
        CGRect.applyRectToView(titleFrame, titleView);
        layout.addView(titleView);

        int buttonSize = height/11;
        int navBarHeight = getNavigationBarHeight(ContextManager.ctx, isTablet(ContextManager.ctx), getResources());
        int buttonY = height-(padding*2)-(int)(navBarHeight*1.5)-buttonSize-padding/2;

        settings_button = new ImageView(ContextManager.ctx);
        settings_button.setEnabled(type != App.TIMEDOCK);
        settings_button.setImageResource(R.drawable.settings_button);
        settings_button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        settings_button.setPadding(5, 5, 5, 5);
        settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sourceActivity.openSettings(null);
            }
        });
        CGRect settingsFrame = CGRectMake(width/2 - padding/2, buttonY, buttonSize, buttonSize);
        CGRect.applyRectToView(settingsFrame, settings_button);
        layout.addView(settings_button);

        install_button = new ImageView(ContextManager.ctx);
        install_button.setEnabled(type != App.TIMEDOCK);
        install_button.setImageResource(R.drawable.install_button);
        install_button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        install_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sourceActivity.installApp(null);
            }
        });
        CGRect installFrame = CGRectMake(width/2 - buttonSize - (int)(padding*1.5), buttonY, buttonSize, buttonSize);
        CGRect.applyRectToView(installFrame, install_button);
        layout.addView(install_button);

        textScrollParentView = new ScrollView(ContextManager.ctx);
        textScrollView = new TextView(getActivity().getApplicationContext());
        textScrollView.setTextColor(Color.BLACK);
        textScrollView.setTypeface(helveticaNeue);
        textScrollView.setText(getAbootText(App.fromInt(type.toInt()), getResources(), getActivity().getPackageName()));
        textScrollView.setTextSize(16);
        textScrollParentView.addView(textScrollView);
        int scrollOrigin = titleFrame.origin.y + titleFrame.size.height;
        CGRect descriptionFrame = CGRectMake(0, scrollOrigin, pebbleViewRect.size.width, installFrame.origin.y-scrollOrigin-padding/5);
        CGRect.applyRectToView(descriptionFrame, textScrollParentView);
        layout.addView(textScrollParentView);

        setPurchased(getArguments().getBoolean("purchased"));
        if(purchased || DataFramework.getUserIsBacker(getActivity().getApplicationContext())){
            settings_button.setImageResource(R.drawable.settings_button);
        }

        return layout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}

/*
    public void sideloadInstall(Context ctx, String assetFilename){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(ctx.getExternalFilesDir(null), assetFilename);
            InputStream is = ctx.getResources().getAssets().open(assetFilename);
            OutputStream os = new FileOutputStream(file);
            byte[] pbw = new byte[is.available()];
            is.read(pbw);
            os.write(pbw);
            is.close();
            os.close();
            intent.setDataAndType(Uri.fromFile(file), "application/pbw");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
        catch (IOException e) {
            Toast.makeText(ctx, "ERROR: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
*/