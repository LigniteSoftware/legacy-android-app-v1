package com.edwinfinch.lignite;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;


/**
 * Created by edwinfinch on 15-05-13.
 */
public class FeedbackActivity extends Activity {
    Spinner typeFeedbackSpinner;
    SeekBar importanceSeekBar;
    TextView importanceValueTextView;
    EditText anythingElseEditText;
    Button sendButton;

    String result;
    JSONObject resultJSON;
    int status = 0;

    int progressBarStatus = 0;
    int waitTime = 0;

    SeekBar.OnSeekBarChangeListener importanceChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            importanceValueTextView.setText("" + progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    Spinner.OnItemSelectedListener typeSelectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    Button.OnClickListener sendClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(anythingElseEditText.getText().toString().replace(" ", "").equals("")){
                anythingElseEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(anythingElseEditText.getContext(), getString(R.string.error_enter_text), Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            final ProgressDialog dialog = new ProgressDialog(importanceValueTextView.getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.sending_feedback));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.show();

            String[] feedback_type = getResources().getStringArray(R.array.feedback_types);

            final String type = feedback_type[typeFeedbackSpinner.getSelectedItemPosition()];
            final String anythingElse = anythingElseEditText.getText().toString().replace("'", "");
            final int importance = importanceSeekBar.getProgress();

            new Thread(new Runnable() {
                public void run() {
                    try {
                        progressBarStatus = 0;
                        waitTime = 0;
                        String params = "username=" + DataFramework.getAccessCode(getApplicationContext()) + "&currentDevice="
                                + android.os.Build.MODEL + "&accessToken=" + DataFramework.getAccessToken(getApplicationContext())
                                + "&type=" + type + "&details=" + anythingElse + "&importance=" + importance;
                        result = DataFramework.sendPost(params, "https://api.lignite.me/v2/feedback/index.php");
                        resultJSON = new JSONObject(result);
                        status = resultJSON.getInt("status");
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    while (progressBarStatus < 100) {
                        // process some tasks
                        if(result == null){
                            progressBarStatus = 500;
                        }
                        else if(progressBarStatus < 85) {
                            progressBarStatus += Math.random() * 5;
                        }
                        else if(status == 200 && progressBarStatus > 84){
                            progressBarStatus = 100;
                        }
                        else if(status != 200 && progressBarStatus > 84){
                            waitTime++;
                        }

                        if(waitTime > 200){
                            dialog.dismiss();
                            progressBarStatus = 101;
                            importanceValueTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    failedToast();
                                }
                            });
                            return;
                        }

                        // sleep 1 second (simulating a time consuming task...)
                        try {
                            Thread.sleep((int)(Math.random()*20));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // Update the progress bar
                        importanceValueTextView.post(new Runnable() {
                            public void run() {
                                dialog.setProgress(progressBarStatus);
                            }
                        });
                    }
                    if (progressBarStatus >= 100) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                        if(progressBarStatus != 500) {
                            importanceValueTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    successToast();
                                }
                            });
                        }
                        else{
                            importanceValueTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    nullToast();
                                }
                            });
                        }
                        finish();
                    }
                }
            }).start();

        }
    };

    public void nullToast(){
        Toast.makeText(typeFeedbackSpinner.getContext(), getString(R.string.error_unknown), Toast.LENGTH_LONG).show();
    }

    public void failedToast(){
        try {
            Toast.makeText(typeFeedbackSpinner.getContext(), getString(R.string.error_unknown) + " " + resultJSON.getString("localized_message") + ".", Toast.LENGTH_LONG).show();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void successToast(){
        Toast.makeText(typeFeedbackSpinner.getContext(), getString(R.string.feedback_sent), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_activity);

        typeFeedbackSpinner = (Spinner)findViewById(R.id.typeFeedbackSpinner);
        ArrayAdapter<CharSequence> typesAdapter = ArrayAdapter.createFromResource(this, R.array.feedback_types, android.R.layout.simple_spinner_dropdown_item);
        typeFeedbackSpinner.setAdapter(typesAdapter);
        typeFeedbackSpinner.setOnItemSelectedListener(typeSelectedListener);
        //typeFeedbackSpinner.setBackgroundColor(getResources().getColor(android.R.color.black));

        importanceSeekBar = (SeekBar)findViewById(R.id.importanceSeekBar);
        importanceSeekBar.setOnSeekBarChangeListener(importanceChangeListener);

        importanceValueTextView = (TextView)findViewById(R.id.importanceValueTextView);

        sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(sendClickListener);

        anythingElseEditText = (EditText)findViewById(R.id.anythingElseEditText);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(Color.parseColor("#D32F2F"));
        }
    }
}
