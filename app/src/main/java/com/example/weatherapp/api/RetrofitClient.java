package com.example.weatherapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Địa chỉ gốc của API Thời tiết
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static Retrofit retrofit = null;

    // Hàm khởi tạo Retrofit (Chỉ tạo 1 lần duy nhất để tiết kiệm bộ nhớ)
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Tự động convert JSON sang Class
                    .build();
        }
        return retrofit;
    }
}