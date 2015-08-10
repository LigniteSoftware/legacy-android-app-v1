package com.edwinfinch.lignite;

import android.app.AlertDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
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
 * Use the {@link AppFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppFragment extends android.support.v4.app.Fragment {
    private int type = 0;
    private boolean other = false;
    private boolean purchased = false;

    Button settings_button;
    ImageSwitcher pebble_view;
    ScrollView textScrollParentView;
    TextView textScrollView;

    private OnFragmentInteractionListener mListener;

    public static AppFragment newInstance(String param1, String param2) {
        AppFragment fragment = new AppFragment();
        Bundle args = new Bundle();
        args.putInt("t", 0);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPurchased(boolean purchasedornot){
        purchased = purchasedornot;
        if(isAdded()) {
            settings_button.setText(purchased ? getString(R.string.settings) : getString(R.string.purchase));
        }
    }

    public AppFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("t");
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
            pebble_view.setImageResource(LigniteInfo.getDrawable(type - 1, other));
            other = !other;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.fragment_app, container, false);

        pebble_view = (ImageSwitcher) layout.findViewById(R.id.pebbleImagePreview);
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

        Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_out_right);
        in.setInterpolator(new AnticipateOvershootInterpolator());
        out.setInterpolator(new AnticipateOvershootInterpolator());
        pebble_view.setImageResource(LigniteInfo.getDrawable(type - 1, true));

        settings_button = (Button) layout.findViewById(R.id.settingsButton);
        settings_button.setEnabled(LigniteInfo.SETTINGS_ENABLED[type-1]);
        settings_button.setText(purchased ? getString(R.string.settings) : getString(R.string.purchase));

        textScrollParentView = (ScrollView) layout.findViewById(R.id.textScrollView);
        textScrollView = new TextView(getActivity().getApplicationContext());
        textScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(LigniteInfo.getAbootText(type - 1, getResources(), true))
                        .setPositiveButton(R.string.okay, null)
                        .show();
            }
        });
        textScrollView.setTextColor(Color.BLACK);
        textScrollView.setText(LigniteInfo.getAbootText(type - 1, getResources(), false));

        textScrollParentView.addView(textScrollView);

        return layout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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