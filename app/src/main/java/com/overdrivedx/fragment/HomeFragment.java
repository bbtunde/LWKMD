package com.overdrivedx.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.overdrivedx.adapter.FeedListAdapter;
import com.overdrivedx.lwkmd2.R;
import com.overdrivedx.models.FeedItem;
import com.overdrivedx.utils.Constants;
import com.overdrivedx.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Activity activity;
    private ListView listView;
    private FeedListAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OnFragmentInteractionListener mListener;
    private TextView splash_message;
    ProgressBar pd;
    private View waiting;
    private int loaded = 0;
    private int current_page = 0;
    private List<FeedItem>  feedItems;

    private AdView mAdView;
    private Button btnLoadMore;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        feedItems = new ArrayList<FeedItem>();

        new GetFeed().execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        listView = (ListView) v.findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        waiting = v.findViewById(R.id.waiting);
        splash_message = (TextView) v.findViewById(R.id.splash_message);
        pd = (ProgressBar)v.findViewById(R.id.progressBar);

        TextView  home = (TextView) v.findViewById(R.id.home);
        TextView  camera = (TextView) v.findViewById(R.id.camera);
        TextView  about = (TextView) v.findViewById(R.id.about);

        Random randPhrase50 = new Random();
        int[] array50 = new int[] { R.string.loading, R.string.show_overlay, R.string.just_a_second, R.string.keep_updated};
        int randPhrase = randPhrase50.nextInt(array50.length - 1);

        splash_message.setText(array50[randPhrase]);

        home.setText(String.valueOf((char) 0xe601));
        camera.setText(String.valueOf((char) 0xe604));
        about.setText(String.valueOf((char) 0xe603));

        home.setTypeface(Typeface.createFromAsset(activity.getAssets(), "icomoon.ttf"));
        camera.setTypeface(Typeface.createFromAsset(activity.getAssets(), "icomoon.ttf"));
        about.setTypeface(Typeface.createFromAsset(activity.getAssets(), "icomoon.ttf"));


        swipeRefreshLayout.setOnRefreshListener(this);

        if (!new Utils().isConnectedToInternet(activity)) {
            pd.setVisibility(View.GONE);
            splash_message.setText("Oops! No internet connection found.");
        }

        btnLoadMore = new Button(activity);
        btnLoadMore.setText("Load More");
        btnLoadMore.setBackgroundResource(R.drawable.green_btn);
        btnLoadMore.setTextColor(Color.parseColor("#ffffff"));

        listView.addFooterView(btnLoadMore);


        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Starting a new async task
                new loadMoreListView().execute();
            }
        });

        mAdView = (AdView)v.findViewById(R.id.ad_view);

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("6AE05576CCEEA0FAE20D69C4380A5FC1")
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        //refreshAd();
        return v;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onHomeFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        feedItems = null;
        mListener = null;
    }

    @Override
    public void onRefresh() {
        feedItems = null;
        feedItems = new ArrayList<FeedItem>();
        current_page = 0;
        new GetFeed().execute();
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
        public void onHomeFragmentInteraction(Uri uri);
    }

    private class GetFeed extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            if (!new Utils().isConnectedToInternet(activity)) {
                onBoxmentError("No internet connection.");
                cancel(true);
            }

            if (isCancelled()) {
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

                RequestQueue rq = Volley.newRequestQueue(activity);

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                        Constants.FEED + "?page=" + current_page, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    Boolean hide_button = Boolean.parseBoolean(response.getString("hide_button"));
                                    JSONArray feedArray = response.getJSONArray("feed");
                                    parseFeed(feedArray, hide_button);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onBoxmentError("Unable to fetch feed.");
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
            //}
            return null;
        }

    }

    private void parseFeed(JSONArray feedArray, Boolean hide_button) throws JSONException {

        if(feedArray.length() > 0) {
            for (int i = 0; i < feedArray.length(); i++) {
                FeedItem item = new FeedItem();
                JSONObject feedObj = (JSONObject) feedArray.get(i);
                item.setId(feedObj.getInt("id"));
                String images = feedObj.isNull("name") ? null : feedObj.getString("name");
                item.setBody(feedObj.getString("caption"));
                item.setImages(images);
                feedItems.add(item);
            }

            waiting.setVisibility(View.INVISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);

            listAdapter = new FeedListAdapter(activity, feedItems);

            listView.setAdapter(listAdapter);
        }
        else{
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.setVisibility(View.GONE);
                    splash_message.setText("Oops! Nothing to show.");
                }
            });
        }

        if(hide_button){
            btnLoadMore.setVisibility(View.GONE);
        }

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

    }

    public void onBoxmentError(String s) {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);

        new Utils().onError(s, activity);
    }

    private void refreshAd() {
        AdLoader adLoader = new AdLoader.Builder(activity, Constants.ADMOB_NATIVE_AD_UNIT_ID)
                .forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                    @Override
                    public void onAppInstallAdLoaded(NativeAppInstallAd appInstallAd) {

                        FrameLayout frameLayout = (FrameLayout)activity.findViewById(R.id.fl_adplaceholder);
                        NativeAppInstallAdView adView = (NativeAppInstallAdView) activity.getLayoutInflater().inflate(R.layout.ad_app_install, null);
                        populateAppInstallAdView(appInstallAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }
                })
                .forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(NativeContentAd contentAd) {

                        FrameLayout frameLayout = (FrameLayout)activity.findViewById(R.id.fl_adplaceholder);
                        NativeContentAdView adView = (NativeContentAdView) activity.getLayoutInflater().inflate(R.layout.ad_content, null);
                        populateContentAdView(contentAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {

                        if (loaded == 0) {
                            refreshAd();
                        }
                    }

                    @Override
                    public void onAdLoaded() {

                        loaded = loaded + 1;
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());

    }

    private void populateAppInstallAdView(NativeAppInstallAd nativeAppInstallAd,
                                          NativeAppInstallAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setImageView(adView.findViewById(R.id.appinstall_image));
        adView.setBodyView(adView.findViewById(R.id.appinstall_body));
        adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setPriceView(adView.findViewById(R.id.appinstall_price));
        adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
        adView.setStoreView(adView.findViewById(R.id.appinstall_store));

        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
        ((TextView) adView.getPriceView()).setText(nativeAppInstallAd.getPrice());
        ((TextView) adView.getStoreView()).setText(nativeAppInstallAd.getStore());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());
        ((ImageView) adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon()
                .getDrawable());
        ((RatingBar) adView.getStarRatingView())
                .setRating(nativeAppInstallAd.getStarRating().floatValue());

        List<NativeAd.Image> images = nativeAppInstallAd.getImages();

        if (images.size() > 0) {
            ((ImageView) adView.getImageView())
                    .setImageDrawable(images.get(0).getDrawable());
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAppInstallAd);
    }

    /**
     * Populates a {@link NativeContentAdView} object with data from a given
     * {@link NativeContentAd}.
     *
     * @param nativeContentAd the object containing the ad's assets
     * @param adView          the view to be populated
     */
    private void populateContentAdView(NativeContentAd nativeContentAd,
                                       NativeContentAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
        adView.setImageView(adView.findViewById(R.id.contentad_image));
        adView.setBodyView(adView.findViewById(R.id.contentad_body));
        adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
        adView.setLogoView(adView.findViewById(R.id.contentad_logo));
        adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if (images != null && images.size() > 0) {
            ((ImageView) adView.getImageView())
                    .setImageDrawable(images.get(0).getDrawable());
        }

        NativeAd.Image logoImage = nativeContentAd.getLogo();

        if (logoImage != null) {
            ((ImageView) adView.getLogoView())
                    .setImageDrawable(logoImage.getDrawable());
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeContentAd);
    }

    private class loadMoreListView extends AsyncTask<Void, Void, Void> {
        Dialog dialog_waiting = new Utils().Waiting(activity);
        @Override
        protected void onPreExecute() {
            // Showing progress dialog before sending http request
            dialog_waiting.show();
        }

        protected Void doInBackground(Void... unused) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    // increment current page
                    current_page += 1;

                    RequestQueue rq = Volley.newRequestQueue(activity);

                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                            Constants.FEED + "?page=" + current_page, null,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {

                                    try {
                                        JSONArray feedArray = response.getJSONArray("feed");
                                        Boolean hide_button = Boolean.parseBoolean(response.getString("hide_button"));
                                        parseFeed(feedArray, hide_button);

                                        int currentPosition = listView.getFirstVisiblePosition();
                                        listView.setSelectionFromTop(current_page *10, 0);


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            onBoxmentError("Unable to fetch feed.");
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


                }
            });
            return (null);
        }

        protected void onPostExecute(Void unused) {
            // closing progress dialog
            dialog_waiting.dismiss();
        }
    }

}
