package com.gmail.st94.dev.airwatch;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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
            getAverageSymptomLevels();

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

    private void getAverageSymptomLevels() {
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            // fetch data
            JsonObjectRequest gcmNotificationRequest = new JsonObjectRequest(
                    Constants.SERVER_URL + Constants.SERVER_COMPLETE_GET_LOCAL_SYMPTOMS,
                    null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "onResponse: Transaction Complete GCM Message" + response.toString());
                            Gson gson = new Gson();
                            mSymptomLevels = gson.fromJson (response.toString(), SymptomLevels.class);
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
                            mTemperatureText.setText(mAirQuality.temp);

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
