package com.edwinfinch.lignite;

import java.util.ArrayList;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.edwinfinch.lignite.util.IabHelper;
import com.edwinfinch.lignite.util.IabResult;
import com.edwinfinch.lignite.util.Inventory;
import com.edwinfinch.lignite.util.Purchase;
import com.getpebble.android.kit.PebbleKit;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONObject;


public class AppsActivity extends ActionBarActivity implements ActionBar.TabListener {

    static public final String TAG = "AppsActivity";
    int progressBarStatus = 0, waitTime = 0;
    String previousUsername;
    ProgressDialog logoutDialog;

    private DialogInterface.OnClickListener logoutListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            logoutDialog = new ProgressDialog(navigationDrawer.getRecyclerView().getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
            logoutDialog.setCancelable(false);
            logoutDialog.setMessage(getString(R.string.logging_out));
            logoutDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            logoutDialog.setProgress(0);
            logoutDialog.setMax(100);
            logoutDialog.show();

            new Thread(new Runnable() {
                public void run() {
                    String result;
                    int status = 0;
                    try{
                        progressBarStatus = waitTime = 0;
                        result = DataFramework.sendPost("username=" + DataFramework.getAccessCode(getApplicationContext()) + "&currentDevice=" + Build.MODEL
                                + "&accessToken=" + DataFramework.getAccessToken(getApplicationContext()), "https://api.lignite.me/v2/logout/index.php");
                        resultJSON = new JSONObject(result);
                        status = resultJSON.getInt("status");
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    while (progressBarStatus < 100) {
                        // process some tasks
                        if(progressBarStatus < 85) {
                            progressBarStatus += Math.random() * 5;
                        }
                        else if((status == 200 || status == 404) && progressBarStatus > 84){
                            progressBarStatus = 100;
                        }
                        else if(status != 200 && progressBarStatus > 84){
                            logoutDialog.dismiss();
                            progressBarStatus = 101;
                            navigationDrawer.getRecyclerView().post(new Runnable() {
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
                        navigationDrawer.getRecyclerView().post(new Runnable() {
                            public void run() {
                                logoutDialog.setProgress(progressBarStatus);
                            }
                        });
                    }
                    if (progressBarStatus >= 100) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        logoutDialog.dismiss();
                        if(progressBarStatus != 500) {
                            navigationDrawer.getRecyclerView().post(new Runnable() {
                                @Override
                                public void run() {
                                    successToast();
                                    DataFramework.wipeUserDetails(getApplicationContext());
                                    Intent launchMain = new Intent(AppsActivity.this, LoginActivity.class);
                                    if (loginTokenFix) {
                                        launchMain.putExtra("BROKEN_TOKEN_FIX", true);
                                        launchMain.putExtra("BROKEN_TOKEN_USERNAME", previousUsername);
                                    }
                                    AppsActivity.this.startActivity(launchMain);
                                    finish();
                                }
                            });
                        }
                        else{
                            navigationDrawer.getRecyclerView().post(new Runnable() {
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

    JSONObject resultJSON;
    boolean loginTokenFix = false;

    public void nullToast(){
        Toast.makeText(navigationDrawer.getRecyclerView().getContext(), getString(R.string.error_response_null), Toast.LENGTH_LONG).show();
    }

    public void failedToast(){
        try {
            Toast.makeText(navigationDrawer.getRecyclerView().getContext(), getString(R.string.error_logout_failed) + " (" + resultJSON.getString("localized_message") + ")", Toast.LENGTH_LONG).show();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void successToast(){
        Toast.makeText(navigationDrawer.getRecyclerView().getContext(), getString(R.string.logout_success), Toast.LENGTH_LONG).show();
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    IabHelper mHelper;
    boolean owns_app[] = new boolean[LigniteInfo.AMOUNT_OF_APPS];
    Drawer navigationDrawer;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure()) {
                Log.d("AppsActivity", "Error purchasing: " + result);
                mHelper.queryInventoryAsync(mGotInventoryListener);
                return;
            }

            for(int i = 0; i < LigniteInfo.AMOUNT_OF_APPS; i++) {
                if (purchase.getSku().equals(LigniteInfo.APP_SKUS[i])) {
                    owns_app[i] = true;
                    AppFragment frag = (AppFragment) mSectionsPagerAdapter.getItem(i);
                    frag.setPurchased(true);
                }
            }

            new AlertDialog.Builder(AppsActivity.this).setMessage(getString(R.string.purchase_thanks)).show();
            mHelper.queryInventoryAsync(mGotInventoryListener);
        }
    };

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {
                // handle error here
            }
            else {
                for(int i = 0; i < LigniteInfo.AMOUNT_OF_APPS; i++) {
                    owns_app[i] = inventory.hasPurchase(LigniteInfo.APP_SKUS[i]);
                    AppFragment frag = (AppFragment) mSectionsPagerAdapter.getItem(i);
                    frag.setPurchased(true);
                }
            }
        }
    };

    Drawer.OnDrawerItemClickListener appClickedListener = new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
            mViewPager.setCurrentItem(i-1);
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoaVB29ms0bDJPU4fmcArVHWPS9" +
                "21mid0eRwu2plwX/cMELfCrkZU7IuSp+wa4JZroo/olQHPayrBdbxbpO39ZD7lolGa50+PCFSi5CqPkec6pGeZQtyfgEPN" +
                "yzduOJn6hIxNjps2qdTbsmhiBbe0lDmQWtIdXan+pE49wgtqt6GB2UPEZdU7H9dEOnUK1tE0DJBxEt1TsvP3jui1EYsMtB" +
                "RnEr2AJBX/MfS0NKOqstu8LYDFPnAj3k4vBbo77TuwSJ2pqeCH+ecNY/eGwOJDKyAod+Q8AXfxSPDhgJaMUeanyyYXA8Ht" +
                "lo83yVx1ngxrjxB5HtL+TG+9CLqLogaa3QIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("AppsActivity", "Problem setting up In-app Billing: " + result);
                    if(DataFramework.getUserIsBacker(getApplicationContext())){
                        for (int i = 0; i < LigniteInfo.AMOUNT_OF_APPS; i++) {
                            owns_app[i] = true;
                            AppFragment frag = (AppFragment) mSectionsPagerAdapter.getItem(i);
                            frag.setPurchased(true);
                        }
                    }
                    return;
                }
                // Hooray, IAB is fully set up!
                if (!DataFramework.getUserIsBacker(getApplicationContext())) {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } else {
                    for (int i = 0; i < LigniteInfo.AMOUNT_OF_APPS; i++) {
                        owns_app[i] = true;
                        AppFragment frag = (AppFragment) mSectionsPagerAdapter.getItem(i);
                        frag.setPurchased(true);
                    }
                }
            }
        });

        String name = getIntent().getStringExtra("name");
        if(name != null && DataFramework.getUserIsBacker(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), getString(R.string.welcome_back) + " " + name + "!", Toast.LENGTH_SHORT).show();
        }

        String accessToken = DataFramework.getAccessToken(getApplicationContext());
        if(accessToken.equals("nothing") && DataFramework.getUserIsBacker(getApplicationContext())){
            //Run fixing process
            new AlertDialog.Builder(AppsActivity.this)
                .setMessage(R.string.broken_token_error)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loginTokenFix = true;
                        previousUsername = DataFramework.getAccessCode(getApplicationContext());
                        logoutListener.onClick(null, 0);
                    }
                }).show();
        }

        new ContextManager(getApplicationContext());

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //actionBar.setSelectedNavigationItem(position);
                setDrawerListToPosition(position);
            }
        });

        String[] array = getResources().getStringArray(R.array.app_names);

        ArrayList<String> arrayList = new ArrayList<>();
        for(int i = 0; i < array.length; i++){
            arrayList.add(array[i]);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setNavigationBarColor(Color.parseColor("#D32F2F"));
        }

        for (int i = 0; i < LigniteInfo.AMOUNT_OF_APPS; i++) {
            AppFragment frag = (AppFragment) mSectionsPagerAdapter.getItem(i);
            frag.sourceActivity = this;
        }

        Typeface helveticaNeue = Typeface.createFromAsset(getAssets(), "HelveticaNeue-Regular.ttf");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(getResources().getDrawable(R.drawable.lignite_background))
                .addProfiles(new ProfileDrawerItem()
                        .withName("Edwin Finch")
                        .withEmail("contact@edwinfinch.com")
                        .withTextColor(Color.BLACK)
                        .withIcon(getResources().getDrawable(R.drawable.lines_time)))
                .withTypeface(helveticaNeue)
                .build();

        ArrayList<IDrawerItem> apps = new ArrayList<>();
        for(int i = 0; i < LigniteInfo.AMOUNT_OF_APPS; i++){
            IDrawerItem item = new PrimaryDrawerItem().withName(arrayList.get(i));
            apps.add(item);
        }

        navigationDrawer = new DrawerBuilder().withActivity(this)
                .withTranslucentStatusBar(false)
                .withAccountHeader(header)
                .withDrawerItems(apps)
                .withOnDrawerItemClickListener(appClickedListener)
                .withActionBarDrawerToggle(true)
                .build();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, navigationDrawer.getDrawerLayout(), R.string.drawer_open, R.string.drawer_closed);
        navigationDrawer.setActionBarDrawerToggle(toggle);

        if(!loginTokenFix && DataFramework.getUserIsBacker(getApplicationContext())) {
            DataFramework framework = new DataFramework();
            framework.verifyAccessToken(getApplicationContext(), this, navigationDrawer.getRecyclerView());
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (mHelper != null){
            mHelper.dispose();
        }
        mHelper = null;

        if(logoutDialog != null) {
            logoutDialog.dismiss();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(DataFramework.getUserIsBacker(getApplicationContext())) {
            inflater.inflate(R.menu.menu_apps, menu);
        }
        else{
            inflater.inflate(R.menu.menu_apps_non_backer, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(navigationDrawer.getActionBarDrawerToggle().onOptionsItemSelected(item)){
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Lignite settings temporarily disabled.", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.action_backer_login){
            Intent intent = new Intent(AppsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id == R.id.action_credits){
            Intent intent = new Intent(AppsActivity.this, CreditsActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.action_feedback){
            feedback_click(null);
        }
        else if(id == R.id.action_logout){
            new AlertDialog.Builder(AppsActivity.this)
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.okay, logoutListener)
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void installApp(View view){
        int current = mViewPager.getCurrentItem();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("pebble://appstore/" + LigniteInfo.APPLICATION_LOCATION[current]));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void openSettings(View view){
        int current = mViewPager.getCurrentItem();
        PebbleKit.startAppOnPebble(ContextManager.ctx, UUID.fromString(LigniteInfo.UUID[current]));

        if(owns_app[current]) {
            Intent intent = new Intent(AppsActivity.this, JSONSettingsActivity.class);
            intent.putExtra("app_name", LigniteInfo.NAME[current]);
            startActivity(intent);
        }
        else{
            if (mHelper != null) mHelper.flagEndAsync();
            try {
                mHelper.launchPurchaseFlow(this, LigniteInfo.APP_SKUS[current], 10001, mPurchaseFinishedListener); //it won't crash don't worry
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void feedback_click(View v) {
        Intent launch = new Intent(AppsActivity.this, FeedbackActivity.class);
        AppsActivity.this.startActivity(launch);
    }

    public void setDrawerListToPosition(int pos){
        System.out.println("Setting to " + pos);
        navigationDrawer.setSelection(pos, false);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab t, FragmentTransaction tr){ /* dust */ }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        setDrawerListToPosition(tab.getPosition());
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        AppFragment fragments[] = new AppFragment[LigniteInfo.AMOUNT_OF_APPS+1];

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(fragments[position] == null) {
                fragments[position] = (AppFragment)PlaceholderFragment.newInstance(position + 1, owns_app[position]);
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            return LigniteInfo.AMOUNT_OF_APPS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] app_names = getResources().getStringArray(R.array.app_names);
            return app_names[position];
        }
    }

    public static class PlaceholderFragment extends Fragment {
        public static AppFragment currentFragment;

        public static Fragment newInstance(int sectionNumber, boolean owns_app) {
            System.out.println("Creating new PlaceholderFragment at " + sectionNumber);
            currentFragment = new AppFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("t", sectionNumber);
            bundle.putBoolean("purchased", owns_app);
            currentFragment.setArguments(bundle);
            return currentFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_apps, container, false);
            Log.i(TAG, "Creating new view");
            return rootView;
        }
    }

}
