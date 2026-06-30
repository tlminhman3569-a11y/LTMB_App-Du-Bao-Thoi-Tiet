package com.example.weatherapp.models.common;

import com.google.gson.annotations.SerializedName;

public class Coord {
    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}