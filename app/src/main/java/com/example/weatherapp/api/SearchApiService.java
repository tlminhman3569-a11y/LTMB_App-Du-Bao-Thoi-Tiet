package com.example.weatherapp.api;

import com.example.weatherapp.models.common.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchApiService {

    @GET("weather")
    Call<WeatherResponse> searchCityWeather(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );
}