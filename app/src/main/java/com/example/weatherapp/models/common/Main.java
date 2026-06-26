package com.example.weatherapp.models.common;

import com.google.gson.annotations.SerializedName;

public class Main {
    // @SerializedName giúp GSON hiểu được tên biến trong chuỗi JSON
    @SerializedName("temp")
    private double temp;

    @SerializedName("humidity")
    private int humidity;

    public double getTemp() {
        return temp;
    }

    public int getHumidity() {
        return humidity;
    }
}