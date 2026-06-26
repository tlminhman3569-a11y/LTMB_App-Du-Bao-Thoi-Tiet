package com.example.weatherapp.api;

import com.example.weatherapp.models.common.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HomeApiService {

    // Đường dẫn phụ nối vào Base URL: /weather
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,           // Vĩ độ
            @Query("lon") double lon,           // Kinh độ
            @Query("appid") String apiKey,      // Chìa khóa API
            @Query("units") String units,       // Đơn vị đo (metric = độ C)
            @Query("lang") String lang          // Ngôn ngữ (vi = Tiếng Việt)
    );
}