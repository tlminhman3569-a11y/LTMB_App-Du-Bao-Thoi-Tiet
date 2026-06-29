package com.example.weatherapp.models.common;

public class SearchResultItem {
    private String cityName;
    private String country;
    private double temperature;
    private String weatherDesc;
    private String iconCode;
    private boolean isFavorite = false;
    private double latitude;
    private double longitude;

    public SearchResultItem(String cityName, String country, double temperature,
                            String weatherDesc, String iconCode,
                            double latitude, double longitude) {
        this.cityName = cityName;
        this.country = country;
        this.temperature = temperature;
        this.weatherDesc = weatherDesc;
        this.iconCode = iconCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCityName() { return cityName; }
    public String getCountry() { return country; }
    public double getTemperature() { return temperature; }
    public String getWeatherDesc() { return weatherDesc; }
    public String getIconCode() { return iconCode; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}