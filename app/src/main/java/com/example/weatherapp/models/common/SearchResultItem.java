package com.example.weatherapp.models.common;

public class SearchResultItem {
    private String cityName;
    private String country;
    private double temperature;
    private String weatherDesc;
    private String iconCode;

    public SearchResultItem(String cityName, String country, double temperature,
                            String weatherDesc, String iconCode) {
        this.cityName = cityName;
        this.country = country;
        this.temperature = temperature;
        this.weatherDesc = weatherDesc;
        this.iconCode = iconCode;
    }

    public String getCityName() { return cityName; }
    public String getCountry() { return country; }
    public double getTemperature() { return temperature; }
    public String getWeatherDesc() { return weatherDesc; }
    public String getIconCode() { return iconCode; }
}