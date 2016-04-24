package com.gmail.st94.dev.airwatch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.st94.dev.airwatch.Models.UserModel;
import com.google.gson.Gson;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

public class FormActivity extends AppCompatActivity {

    private static final String TAG ="FormActivity";
    private String[] colourArray = {"#FF009688", "#FF188D7F", "#FF308577", "#FF497D6F", "#FF617467", "#FF7A6C5F", "#FF926456", "#FFAA5b4E", "#FFC35346", "#FFDB4B3E", "#FFF44335"};
    DiscreteSeekBar coughingSliderBar;
    DiscreteSeekBar breathSliderBar;
    DiscreteSeekBar wheezingSliderBar;
    DiscreteSeekBar sneezingSliderBar;
    DiscreteSeekBar nasalSliderBar;
    DiscreteSeekBar eyesSliderBar;
    Button mCreateUserButton;
    UserModel mUser = new UserModel();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Intent i = getIntent();
        mUser = (UserModel) i.getSerializableExtra("mUser");
        


        coughingSliderBar = (DiscreteSeekBar) findViewById(R.id.coughing_slider);
        coughingSliderBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setSeekBarValues(seekBar, value);
                mUser.cough = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        breathSliderBar = (DiscreteSeekBar) findViewById(R.id.breath_slider);
        breathSliderBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setSeekBarValues(seekBar, value);
                mUser.shortnessOfBreath = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        wheezingSliderBar = (DiscreteSeekBar) findViewById(R.id.wheezing_slider);
        wheezingSliderBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setSeekBarValues(seekBar, value);
                mUser.wheezing = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        sneezingSliderBar = (DiscreteSeekBar) findViewById(R.id.sneezing_slider);
        sneezingSliderBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setSeekBarValues(seekBar, value);
                mUser.sneezing = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        nasalSliderBar = (DiscreteSeekBar) findViewById(R.id.nasal_slider);
        nasalSliderBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setSeekBarValues(seekBar, value);
                mUser.nasalObstruction = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        eyesSliderBar = (DiscreteSeekBar) findViewById(R.id.eyes_slider);
        eyesSliderBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setSeekBarValues(seekBar, value);
                mUser.itchyEyes = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        mCreateUserButton = (Button) findViewById(R.id.send_form_button);
        mCreateUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected())
                {
                    // fetch data

                    Gson gson = new Gson();
                    String jsonStringUser = gson.toJson(mUser);
                    JSONObject jsonUser = new JSONObject();
                    try {
                        jsonUser = new JSONObject(jsonStringUser);
                    }
                    catch (JSONException jex) {
                        Log.e (TAG, jex.toString());
                    }

                    JsonObjectRequest gcmNotificationRequest = new JsonObjectRequest(
                            Constants.SERVER_URL + Constants.SERVER_COMPLETE_REGISTER_API_CALL,
                            jsonUser,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i(TAG, "onResponse: Transaction Complete GCM Message" + response.toString());
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
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("submit_flag", true);
                startActivity (i);

            }
        });

    }

    private void setSeekBarValues (DiscreteSeekBar seekBar, int value)
    {
        seekBar.setScrubberColor(Color.parseColor(colourArray[value]));
        seekBar.setThumbColor(Color.parseColor(colourArray[value]), Color.parseColor(colourArray[value]));
    }




}
