package com.example.weatherapp.models.common;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    // Tên thành phố
    @SerializedName("name")
    private String name;

    // Danh sách thời tiết (vì API trả về mảng)
    @SerializedName("weather")
    private List<Weather> weather;

    // Nhiệt độ, độ ẩm
    @SerializedName("main")
    private Main main;

    // Sức gió
    @SerializedName("wind")
    private Wind wind;

    public String getName() {
        return name;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Wind getWind() {
        return wind;
    }
}