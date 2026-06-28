package com.example.weatherapp.models.common;

import com.google.gson.annotations.SerializedName;

public class Sys {
    @SerializedName("country")
    private String country;

    public String getCountry() { return country != null ? country : ""; }
}