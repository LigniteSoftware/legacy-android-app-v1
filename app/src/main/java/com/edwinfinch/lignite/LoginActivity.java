package com.edwinfinch.lignite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText codeView;
    private View progressView;
    private View loginForm;
    boolean loginTokenError = false;
    public TextView resetCodeView, gaveUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        codeView = (EditText) findViewById(R.id.code);
        populateAutoComplete();

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        loginForm = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(Color.parseColor("#D32F2F"));
        }
        if(DataFramework.accessCodeIsSet(getApplicationContext())){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject user = DataFramework.getUserDetailsFromStorage(getApplicationContext());
                    String name = getString(R.string.my_friend);
                    if(user != null) {
                        try {
                            name = user.getString("name");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Intent myIntent = new Intent(LoginActivity.this, AppsActivity.class);
                    myIntent.putExtra("name", name);
                    LoginActivity.this.startActivity(myIntent);
                    finish();
                }
            }).start();
        }

        Intent incomingIntent = getIntent();
        loginTokenError = incomingIntent.getBooleanExtra("BROKEN_TOKEN_FIX", false);
        if(loginTokenError){
            System.out.println("Running broken token fix...");
            String username = incomingIntent.getStringExtra("BROKEN_TOKEN_USERNAME");
            fireTask(username);
            Toast.makeText(getApplicationContext(), "Running token fix...", Toast.LENGTH_LONG).show();
        }
        resetCodeView = (TextView)findViewById(R.id.lostCodeView);
        resetCodeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.lignite.me/reset/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        gaveUpView = (TextView)findViewById(R.id.gaveUp);
        gaveUpView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DataFramework.setUserIsBacker(getApplicationContext(), false);
                Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void fireTask(final String access_code){
        System.out.println("Firing!");

        if(loginTokenError){
            DataFramework.setAccessCode(getApplicationContext(), access_code);
            codeView.setText(access_code);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    loginTokenError = false;
                    codeView.post(new Runnable() {
                        @Override
                        public void run() {
                            fireTask(access_code);
                        }
                    });
                }
            }, 1000);
            return;
        }
        mAuthTask = new UserLoginTask(access_code);
        mAuthTask.execute((Void) null);
        attemptLogin();
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        codeView.setError(null);

        // Store values at the time of the login attempt.
        String access_code = codeView.getText().toString();

        /*
         * Security at it's finest
         *
        if(access_code.equals("BYPASS")){
            Intent myIntent = new Intent(LoginActivity.this, AppsActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            LoginActivity.this.startActivity(myIntent);
            finish();
            Toast.makeText(getApplicationContext(), "Bypassed", Toast.LENGTH_SHORT).show();
            return;
        }
        */

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(access_code)) {
            codeView.setError(getString(R.string.error_field_required));
            focusView = codeView;
            cancel = true;
        } else if (!isCodeValid(access_code)) {
            codeView.setError(getString(R.string.error_invalid_code));
            focusView = codeView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            fireTask(access_code);
        }
    }

    private boolean isCodeValid(String code) {
        return code.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            loginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String accessCode;
        private int progressBarStatus = 0, waitTime = 0;
        private String result;
        private JSONObject resultJSON;
        private int status = 0;

        UserLoginTask(String code) {
            accessCode = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            codeView.post(new Runnable() {
                @Override
                public void run() {
                    final ProgressDialog dialog = new ProgressDialog(codeView.getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    dialog.setCancelable(false);
                    dialog.setMessage(getString(R.string.logging_in));
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setProgress(0);
                    dialog.setMax(100);
                    dialog.show();

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                progressBarStatus = 0;
                                waitTime = 0;
                                String params = "username=" + codeView.getText() + "&currentDevice=" + Build.MODEL;
                                //System.out.println(params);
                                result = DataFramework.sendPost(params, "https://api.lignite.me/v2/login/index.php");
                                resultJSON = new JSONObject(result);
                                status = resultJSON.getInt("status");
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                            while (progressBarStatus < 100) {
                                if(result == null){
                                    progressBarStatus = 500;
                                }
                                else if(progressBarStatus < 85) {
                                    progressBarStatus += Math.random() * 5;
                                }
                                else if(status == 200 && progressBarStatus > 84){
                                    progressBarStatus = 100;
                                }
                                else if(status == 401 || status == 404 && progressBarStatus > 84){
                                    progressBarStatus = 100;
                                }
                                else if(progressBarStatus > 84){
                                    waitTime++;
                                }

                                if(waitTime > 200){
                                    status = 501;
                                }

                                try {
                                    Thread.sleep((int)(Math.random()*20));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                codeView.post(new Runnable() {
                                    public void run() {
                                        dialog.setProgress(progressBarStatus);
                                    }
                                });
                            }
                            if (progressBarStatus >= 100) {
                                try {
                                    Thread.sleep(400);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(result == null){
                                    status = 501;
                                }
                                dialog.dismiss();
                                if(status == 200){
                                    DataFramework.setAccessCode(getApplicationContext(), accessCode);
                                    DataFramework.setUserIsBacker(getApplicationContext(), true);
                                    try { DataFramework.setAccessToken(getApplicationContext(), resultJSON.getString("token")); } catch(Exception e){ e.printStackTrace(); }

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                JSONObject detailsFromInternet = DataFramework.getUserDetailsFromInternet(getApplicationContext());
                                                DataFramework.setUserDetails(getApplicationContext(), detailsFromInternet);

                                                Intent myIntent = new Intent(LoginActivity.this, AppsActivity.class);
                                                String name = detailsFromInternet.getString("name");
                                                if(name == null){ name = getString(R.string.my_friend); }
                                                myIntent.putExtra("name", name); //Optional parameters
                                                LoginActivity.this.startActivity(myIntent);
                                                finish();
                                            }
                                            catch(final Exception e){
                                                e.printStackTrace();
                                                codeView.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), getString(R.string.error_unknown) + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();
                                }
                                else if(status == 401){
                                    codeView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            codeView.setError(getResources().getString(R.string.error_incorrect_code));
                                        }
                                    });
                                }
                                else if(status == 404){
                                    codeView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            codeView.setError(getResources().getString(R.string.error_doesnt_exist));
                                        }
                                    });
                                }
                                else if(status == 500){
                                    //Todo: show internal server error localized
                                    codeView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            codeView.setError(getResources().getString(R.string.error_other));
                                        }
                                    });
                                }
                                else if(status == 501){
                                    codeView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            codeView.setError(getResources().getString(R.string.error_other));
                                        }
                                    });
                                }
                                else{
                                    codeView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            codeView.setError(getResources().getString(R.string.server_error));
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                }
            });
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}


