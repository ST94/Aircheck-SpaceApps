package com.gmail.st94.dev.airwatch;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.st94.dev.airwatch.Models.AirQuality;
import com.gmail.st94.dev.airwatch.Models.SymptomLevels;
import com.gmail.st94.dev.airwatch.Models.UserModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    String mCountryOutput;
    String mCity;
    UserModel mUser = new UserModel();
    SymptomLevels mSymptomLevels = new SymptomLevels();
    AirQuality mAirQuality = new AirQuality();
    Button mReportSymptomsButton;

    TextView mTemperatureText;
    TextView mHumidityText;
    TextView mAirQualityLevelText;

    TextView mCoughing;
    TextView mBreathing;
    TextView mWheezing;
    TextView mSneezing;
    TextView mNasal;
    TextView mEyes;

    RelativeLayout mMainLayout;

    View mGradientBar;
    TextView mGradientText;
    Geocoder geocoder;

    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
        // Get the location passed to this service through an extra.
        Location location = getLastKnownLocation();
        List<Address> addresses = null;
        String errorMessage = "";
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = addresses.get(0);
            Log.i(TAG, getString(R.string.address_found));
            mCountryOutput = address.getCountryName();
            mCity = address.getLocality();
            TextView locationName = (TextView) findViewById(R.id.country_name);
            locationName.setText(mCity.toUpperCase() + ", " + mCountryOutput.toUpperCase());
            mUser.city = mCity;
            mUser.country = mCountryOutput;
            mUser.longitude = Double.toString(round(location.getLongitude(), 2));
            mUser.latitude = Double.toString(round(location.getLatitude(), 2));
            getAirQualityValues(mUser.latitude, mUser.longitude);
            getAverageSymptomLevels(mUser.latitude, mUser.longitude);

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.no_address_found);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }
        Log.i(TAG, "Found country code: " + mCountryOutput);

        mReportSymptomsButton = (Button) findViewById(R.id.report_symptoms_button);
        mReportSymptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent (getApplicationContext(), FormActivity.class);
                i.putExtra("mUser", mUser);
                startActivity (i);

            }
        });
    }

    @Override
    protected void onResume (){
        super.onResume();

        Intent intent = getIntent();
        boolean messageFlag = intent.getBooleanExtra("submit_flag", false );
//        if (messageFlag) {
//            mMainLayout = (RelativeLayout) findViewById(R.id.main_activity_layout);
//            Snackbar snackbar = Snackbar
//                    .make(mMainLayout, "Welcome to AndroidHive", Snackbar.LENGTH_LONG);
//
//            snackbar.show();
//        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            updateUIWithValues (query);
        }
    }

    private void updateUIWithValues (String searchQuery){

        if(Geocoder.isPresent()){
            try {
                String location = searchQuery;
                Geocoder gc = new Geocoder(this);
                List<Address> addresses= gc.getFromLocationName(location, 5); // get the found Address Objects

                Address address = addresses.get(0);
                Log.i(TAG, getString(R.string.address_found));
                mCountryOutput = address.getCountryName();
                mCity = address.getAdminArea();
                TextView locationName = (TextView) findViewById(R.id.country_name);
                locationName.setText(mCity.toUpperCase() + ", " + mCountryOutput.toUpperCase());
                mUser.city = mCity;
                mUser.country = mCountryOutput;


                List<LatLng> ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                for(Address a : addresses){
                    if(a.hasLatitude() && a.hasLongitude()){
                        ll.add(new LatLng(a.getLatitude(), a.getLongitude()));
                    }
                }

                getAirQualityValues(Double.toString(ll.get(0).latitude), Double.toString(ll.get(0).longitude));
                getAverageSymptomLevels(Double.toString(ll.get(0).latitude), Double.toString(ll.get(0).longitude));

                mUser.latitude = Double.toString(ll.get(0).latitude);
                mUser.longitude = Double.toString(ll.get(0).longitude);

            } catch (IOException e) {
                // handle the exception
                Log.i(TAG, e.toString());
            }
        }
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = null;
            try {
                 l = mLocationManager.getLastKnownLocation(provider);
            }
            catch (SecurityException scex) {
                Log.e (TAG, scex.toString());
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
                Log.i(TAG, "Using provider: " + provider);
            }
        }
        return bestLocation;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void getAverageSymptomLevels(String latitude, String longitude) {
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            // fetch data
            JsonObjectRequest gcmNotificationRequest = new JsonObjectRequest(
                    Constants.SERVER_URL + Constants.SERVER_COMPLETE_GET_LOCAL_SYMPTOMS +"?lat=" + latitude +"&lon="+longitude,
                    null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "onResponse: Transaction Complete GCM Message" + response.toString());
                            Gson gson = new Gson();
                            mSymptomLevels = gson.fromJson (response.toString(), SymptomLevels.class);

                            mCoughing = (TextView) findViewById(R.id.coughing_value);
                            mCoughing.setText(Double.toString(mSymptomLevels.cough));

                            mBreathing = (TextView) findViewById(R.id.breath_value);
                            mBreathing.setText(Double.toString(mSymptomLevels.shortnessOfBreath));

                            mWheezing = (TextView) findViewById(R.id.wheezing_value);
                            mWheezing.setText(Double.toString(mSymptomLevels.wheezing));

                            mSneezing = (TextView) findViewById(R.id.sneezing_value);
                            mSneezing.setText(Double.toString(mSymptomLevels.sneezing));

                            mNasal = (TextView) findViewById(R.id.nasal_value);
                            mNasal.setText(Double.toString(mSymptomLevels.nasalObstruction));

                            mEyes = (TextView) findViewById(R.id.eyes_value);
                            mEyes.setText(Double.toString(mSymptomLevels.itchyEyes));

                            double average = mSymptomLevels.cough + mSymptomLevels.shortnessOfBreath + mSymptomLevels.wheezing
                                    + mSymptomLevels.sneezing + mSymptomLevels.nasalObstruction + mSymptomLevels.itchyEyes;
                            average = round (average / 6, 2);
                            double percentage = round (average / 10, 2);

                            mGradientText = (TextView) findViewById(R.id.colored_bar_text);
                            mGradientText.setText(Double.toString(average));

                            Resources r = getApplicationContext().getResources();
                            int px = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    (float) percentage * 305,
                                    r.getDisplayMetrics()
                            );

                            mGradientBar = findViewById(R.id.colored_bar);


                            setMargins(mGradientBar, px, 0, 0, 0);
                            setMargins(mGradientText, px - 30, 0, 0, 0);
                            //mGradientBar.setLayoutParams(params);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Post request to send gcm token to server failed", error);
                        }
                    });
            mQueue.add(gcmNotificationRequest);
        } else {
            Log.e(TAG, "Failed to connect to server to send gcm token");
            // display error
        }
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    private void getAirQualityValues(String latitude, String longitude) {
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            // fetch data
            Log.i(TAG, "Getting air quality information for Lat: " + latitude + " ,long: " + longitude);
            JsonObjectRequest gcmNotificationRequest = new JsonObjectRequest(
                    Constants.SERVER_URL + Constants.SERVER_COMPLETE_GET_AIR_QUALITY + latitude + "/" + longitude,
                    null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "onResponse: Transaction Complete GCM Message" + response.toString());
                            Gson gson = new Gson();
                            mAirQuality = gson.fromJson (response.toString(), AirQuality.class);

                            mTemperatureText = (TextView) findViewById(R.id.temperature);
                            mTemperatureText.setText(Integer.toString ((int) (Math.round(Double.parseDouble(mAirQuality.temp)))));

                            mHumidityText = (TextView) findViewById(R.id.humidity);
                            mHumidityText.setText(mAirQuality.humidity);

                            mAirQualityLevelText = (TextView) findViewById(R.id.air_quality);
                            mAirQualityLevelText.setText(mAirQuality.airQuality);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Post request to send gcm token to server failed", error);
                        }
                    });
            mQueue.add(gcmNotificationRequest);
        } else {
            Log.e(TAG, "Failed to connect to server to send gcm token");
            // display error
        }
    }

}
