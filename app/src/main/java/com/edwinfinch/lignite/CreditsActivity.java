package com.edwinfinch.lignite;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by edwinfinch on 15-05-29.
 */
public class CreditsActivity extends Activity {
    ScrollView creditScrollView;
    ImageView titleView;
    TextView textView;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_credits);

        creditScrollView = (ScrollView)findViewById(R.id.creditsScrollView);

        titleView = new ImageView(getApplicationContext());
        titleView.setImageDrawable(getResources().getDrawable(R.drawable.lignite_logo));

        textView = new TextView(getApplicationContext());
        textView.setText(getString(R.string.full_credits));

        creditScrollView.addView(textView);
    }
}
