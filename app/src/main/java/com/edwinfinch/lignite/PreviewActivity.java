package com.edwinfinch.lignite;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.widget.FrameLayout;

/**
 * Created by edwinfinch on 9/13/15.
 */
public class PreviewActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_preview_builder);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        FrameLayout layout = (FrameLayout)findViewById(R.id.previewBuilderLayout);

        Toolbar toolbar = (Toolbar)findViewById(R.id.previewToolbar);
        toolbar.setTitle("Preview Builder");
        setSupportActionBar(toolbar);
    }
}
