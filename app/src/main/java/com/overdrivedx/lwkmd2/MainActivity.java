package com.overdrivedx.lwkmd2;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.overdrivedx.fragment.AboutFragment;
import com.overdrivedx.fragment.HomeFragment;
import com.overdrivedx.fragment.UpdateFragment;
import com.overdrivedx.fragment.UploadFragment;
import com.overdrivedx.utils.Constants;
import com.overdrivedx.utils.FontsOverride;
import com.overdrivedx.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements
        HomeFragment.OnFragmentInteractionListener,
        AboutFragment.OnFragmentInteractionListener,
        UploadFragment.OnFragmentInteractionListener{

    private  SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    InterstitialAd mInterstitialAd;
    public Handler handler;
    private int loaded = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FontsOverride.setDefaultFont(this, "MONOSPACE", "Lato-Regular.ttf");
        setContentView(R.layout.activity_main);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1, true);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(Constants.ADMOB_INTERSTITIAL_AD_UNIT_ID);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        handler = new Handler();

        handler.postDelayed(runnable, 20000);

        requestNewInterstitial();

        new CheckUpdate().execute();
    }

    public final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (mInterstitialAd.isLoaded()) {
                loaded = loaded+1;

                if(loaded == 1) {
                    mInterstitialAd.show();
                }
            }

            handler.postDelayed(runnable, 20000);
        }
    };

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6AE05576CCEEA0FAE20D69C4380A5FC1")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onHomeFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSettingsFragmentInteraction(Uri uri) {

    }

    @Override
    public void onUploadFragmentInteraction(Uri uri) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position ==1){
                return HomeFragment.newInstance("", "");
            }
            else if(position == 2){
                return UploadFragment.newInstance("", "");
            }
            else{
                return AboutFragment.newInstance("", "");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        View dV = getWindow().getDecorView();
        int uiOpts = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //dV.setSystemUiVisibility(uiOpts);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void showHomeFragment(View v){
        mViewPager.setCurrentItem(1, true);
    }

    public void showAboutFragment(View v){
        mViewPager.setCurrentItem(0, true);
    }

    public void showUploadFragment(View v){
        mViewPager.setCurrentItem(2,true);
    }

    private class CheckUpdate extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            if (!new Utils().isConnectedToInternet(MainActivity.this)) {
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue rq = Volley.newRequestQueue(MainActivity.this);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    Constants.CHECK_FOR_UPDATES, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                Boolean old = Boolean.parseBoolean(response.getString("old"));
                                String message = response.getString("message");

                                if(old) {
                                    showUpdateDialog(message);
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    cancel(true);

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    return null;
                }

            };

            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            rq.add(jsonObjReq);

            return null;
        }

    }

    private void showUpdateDialog(String msg){
        UpdateFragment newFragment = UpdateFragment.newInstance(msg);
        FragmentTransaction ft =getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        newFragment.show(ft, "dialog");
    }



}