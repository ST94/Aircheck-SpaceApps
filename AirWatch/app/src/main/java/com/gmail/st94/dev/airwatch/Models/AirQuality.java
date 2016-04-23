package com.gmail.st94.dev.airwatch.Models;

/**
 * Created by Shing on 2016-04-23.
 */
public class AirQuality {
    public String humidity = "";
    public String airQuality ="";
    public String temp = "";

    public AirQuality(){

    }

    public AirQuality (String quality, String temperature, String humid){
        airQuality = quality;
        temp = temperature;
        humidity = humid;
    }
}
