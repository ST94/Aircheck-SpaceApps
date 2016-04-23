package com.gmail.st94.dev.airwatch.Models;

import java.io.Serializable;

/**
 * Created by Shing on 2016-04-23.
 */
public class UserModel implements Serializable {


    // "id": 1
    // ,
    // "cough": 15,
    // "shortnessOfBreath"
    // : 123, "wheezing": 15,
    // "nasalObstruction": 15,
    // "itchyEyes": 15, "country": "Canada", "city": "Toronto", "latitude": "30", "longitude": "-40"
    public int cough = 0;
    public int shortnessOfBreath = 0;
    public int sneezing = 0;
    public int wheezing = 0;
    public int nasalObstruction = 0;
    public int itchyEyes = 0;
    public String country = "";
    public String city = "";
    public String latitude = "";
    public String longitude = "";


    public UserModel () {

    }

    public UserModel (int cough, int breath, int sneeze, int wheeze, int nasal, int eyes, String country, String city, String lat, String longi){
        this.cough = cough;
        shortnessOfBreath = breath;
        sneezing = sneeze;
        wheezing = wheeze;
        nasalObstruction = nasal;
        itchyEyes = eyes;
        this.country = country;
        this.city = city;
        latitude = lat;
        longitude = longi;
    }


}
