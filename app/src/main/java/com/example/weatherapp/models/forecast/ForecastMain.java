package com.example.weatherapp.models.forecast;

import com.google.gson.annotations.SerializedName;

public class ForecastMain {
    @SerializedName("temp")
    private double temp;

    @SerializedName("humidity")
    private int humidity;

    @SerializedName("temp_min")
    private double tempMin;

    @SerializedName("temp_max")
    private double tempMax;

    public double getTemp() {
        return temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getTempMin() {
        return tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }
}
