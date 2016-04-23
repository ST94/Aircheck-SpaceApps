package com.gmail.st94.dev.airwatch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class FormActivity extends AppCompatActivity {

    private String[] colourArray = {"009688", "188D7F", "308577", "497D6F", "617467", "7A6C5F", "926456", "AA5b4E", "C35346", "DB4B3E", "F44335"};
    DiscreteSeekBar coughingSliderBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        coughingSliderBar = (DiscreteSeekBar) findViewById(R.id.coughing_slider);
        coughingSliderBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

    }




}
