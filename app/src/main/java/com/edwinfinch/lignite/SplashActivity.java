package com.edwinfinch.lignite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by edwinfinch on 15-05-12.
 */
public class SplashActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_splash);

        if(!DataFramework.getTakenBackerQuestion(getApplicationContext())){
            new AlertDialog.Builder(SplashActivity.this)
                    .setTitle("Hello!")
                    .setMessage("Are you a Kickstarter backer?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataFramework.setTakenBackerQuestion(getApplicationContext(), true);
                            DataFramework.setUserIsBacker(getApplicationContext(), true);
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Intent launchMain = new Intent(SplashActivity.this, LoginActivity.class);
                                    SplashActivity.this.startActivity(launchMain);
                                    finish();
                                }
                            }, 1500);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataFramework.setTakenBackerQuestion(getApplicationContext(), true);
                            DataFramework.setUserIsBacker(getApplicationContext(), false);
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Intent launchMain = new Intent(SplashActivity.this, AppsActivity.class);
                                    SplashActivity.this.startActivity(launchMain);
                                    finish();
                                }
                            }, 1500);
                        }
                    }).show();
            return;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent launchMain = new Intent(SplashActivity.this, DataFramework.getUserIsBacker(getApplicationContext()) ?
                        LoginActivity.class : AppsActivity.class);
                SplashActivity.this.startActivity(launchMain);
                finish();
            }
        }, 1500);
    }
}
