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

    @SerializedName("sys")
    private Sys sys;

    public String getName() { return name; }
    public List<Weather> getWeather() { return weather; }
    public Main getMain() { return main; }
    public Wind getWind() { return wind; }
    public Sys getSys() { return sys; }

    public static class Main {
        @SerializedName("temp")
        private double temp;

        @SerializedName("humidity")
        private int humidity;

        public double getTemp() { return temp; }

        public int getHumidity() { return humidity; }
    }

    public static class Weather {
        @SerializedName("description")
        private String description;
        @SerializedName("icon")
        private String icon;

        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
}