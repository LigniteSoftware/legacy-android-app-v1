package com.edwinfinch.lignite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AppFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AppFragment} factory method to
 * create an instance of this fragment.
 */
public class AppFragment extends android.support.v4.app.Fragment {
    private LigniteInfo.App type;
    private boolean other = false;
    private boolean purchased = false;

    public AppsActivity sourceActivity;

    ImageView install_button, settings_button;
    ImageSwitcher pebble_view;
    ImageView left_arrow_view, right_arrow_view;
    ScrollView textScrollParentView;
    TextView textScrollView, titleView;

    private OnFragmentInteractionListener mListener;

    public void setPurchased(boolean purchasedornot){
        purchased = purchasedornot;
        if(isAdded()) {
            settings_button.setImageResource(purchased ? R.drawable.settings_button : R.drawable.purchase_button);
        }
    }

    public AppFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = LigniteInfo.App.fromInt(getArguments().getInt("t")-1);
            purchased = getArguments().getBoolean("purchased");
        }
    }

    public ImageSwitcher.OnClickListener previewImageListener = new ImageSwitcher.OnClickListener(){
        @Override
        public void onClick(View v) {
            Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_out_right);
            in.setInterpolator(new AnticipateOvershootInterpolator());
            out.setInterpolator(new AnticipateOvershootInterpolator());

            pebble_view.setInAnimation(in);
            pebble_view.setOutAnimation(out);
            pebble_view.setImageResource(LigniteInfo.getDrawable(LigniteInfo.App.fromInt(type.toInt()), other));
            other = !other;
        }
    };

    public CGRect CGRectMake(int x, int y, int width, int height){
        return new CGRect(x, y, width, height);
    }

    public int getNavigationBarHeight(Context c) {
        int result = 0;
        boolean hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if(!hasMenuKey && !hasBackKey) {
            //The device has a navigation bar
            Resources resources = ContextManager.ctx.getResources();

            int orientation = getResources().getConfiguration().orientation;
            int resourceId;
            if (isTablet(c)){
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            }  else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                return getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }


    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.fragment_test, container, false);
        int padding = 50;
        layout.setPadding(padding, padding, padding, padding/2);

        //FrameLayout layout = new FrameLayout(ContextManager.ctx);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        //pebble_view = (ImageSwitcher) layout.findViewById(R.id.pebbleImagePreview);
        pebble_view = new ImageSwitcher(ContextManager.ctx);
        pebble_view.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView myView = new ImageView(getActivity().getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
                return myView;
            }
        });
        pebble_view.setOnClickListener(previewImageListener);
        CGRect pebbleViewRect = CGRectMake(0, padding, width - padding * 2, height / 3);
        CGRect.applyRectToView(pebbleViewRect, pebble_view);
        layout.addView(pebble_view);

        Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_out_right);
        in.setInterpolator(new AnticipateOvershootInterpolator());
        out.setInterpolator(new AnticipateOvershootInterpolator());
        pebble_view.setImageResource(LigniteInfo.getDrawable(LigniteInfo.App.fromInt(type.toInt()), true));

        left_arrow_view = new ImageView(ContextManager.ctx);
        left_arrow_view.setImageResource(R.drawable.arrow_active_left);
        left_arrow_view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        CGRect leftArrowRect = CGRectMake(0, pebbleViewRect.origin.y+pebbleViewRect.size.height/2 - 15, 30, 30);
        CGRect.applyRectToView(leftArrowRect, left_arrow_view);
        layout.addView(left_arrow_view);

        right_arrow_view = new ImageView(ContextManager.ctx);
        right_arrow_view.setImageResource(R.drawable.arrow_active_right);
        right_arrow_view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        CGRect rightArrowRect = CGRectMake(width - 40 - padding * 2, pebbleViewRect.origin.y + pebbleViewRect.size.height / 2, 30, 30);
        CGRect.applyRectToView(rightArrowRect, right_arrow_view);
        layout.addView(right_arrow_view);

        if(type == LigniteInfo.App.SPEEDOMETER){
            left_arrow_view.setImageResource(R.drawable.arrow_inactive_left);
        }
        else if(type.toInt() == LigniteInfo.AMOUNT_OF_APPS-1){
            right_arrow_view.setImageResource(R.drawable.arrow_inactive_right);
        }

        Typeface helveticaNeue = Typeface.createFromAsset(getActivity().getAssets(), "HelveticaNeue-Regular.ttf");

        titleView = new TextView(ContextManager.ctx);
        titleView.setText(getResources().getStringArray(R.array.app_names)[type.toInt()]);
        titleView.setTextColor(Color.BLACK);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(helveticaNeue);
        titleView.setTextSize(28);
        CGRect titleFrame = CGRectMake(0, pebbleViewRect.size.height + pebbleViewRect.origin.y, pebbleViewRect.size.width, 200);
        CGRect.applyRectToView(titleFrame, titleView);
        layout.addView(titleView);

        textScrollParentView = new ScrollView(ContextManager.ctx);
        textScrollView = new TextView(getActivity().getApplicationContext());
        textScrollView.setTextColor(Color.BLACK);
        textScrollView.setTypeface(helveticaNeue);
        textScrollView.setText(LigniteInfo.getAbootText(LigniteInfo.App.fromInt(type.toInt()), getResources(), false));
        textScrollView.setTextSize(16);
        textScrollParentView.addView(textScrollView);
        CGRect descriptionFrame = CGRectMake(0, titleFrame.origin.y + titleFrame.size.height, pebbleViewRect.size.width, height/4);
        CGRect.applyRectToView(descriptionFrame, textScrollParentView);
        layout.addView(textScrollParentView);

        int buttonSize = height/11;
        int buttonY = height-(padding*2)-(int)(getNavigationBarHeight(ContextManager.ctx)*1.5)-buttonSize-padding/2;

        settings_button = new ImageView(ContextManager.ctx);
        settings_button.setEnabled(type != LigniteInfo.App.TIMEDOCK);
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
        install_button.setEnabled(type != LigniteInfo.App.TIMEDOCK);
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

        return layout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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