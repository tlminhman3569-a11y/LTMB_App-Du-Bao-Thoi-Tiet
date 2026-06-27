package com.example.weatherapp.models.forecast;

import com.example.weatherapp.models.common.Main;
import com.example.weatherapp.models.common.Weather;
import com.example.weatherapp.models.common.Wind;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastItem {

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("dt_txt")
    private String dtTxt;

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }

    public String getDtTxt() {
        return dtTxt;
    }
}
