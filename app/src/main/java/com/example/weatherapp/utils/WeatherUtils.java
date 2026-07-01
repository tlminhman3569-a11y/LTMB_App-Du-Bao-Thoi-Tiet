package com.example.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
public class WeatherUtils {
    private static final String SETTINGS_PREFS = "WeatherSettingsPrefs";

    //Kiểm tra xem người dùng đang chọn độ C hay không
    public static boolean isCelsius(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean("is_celsius", true);
    }

    //Kiểm tra xem người dùng đang chọn km/h hay không
    public static boolean isKmH(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean("is_kmh", true);
    }

    //Hàm quy đổi từ độ C sang độ F (Làm tròn thành số nguyên)
    public static int convertCelsiusToFahrenheit(double celsius) {
        return (int) Math.round(celsius * 1.8 + 32);
    }

    //Hàm quy đổi từ độ F ngược về độ C
    public static int convertFahrenheitToCelsius(double fahrenheit) {
        return (int) Math.round((fahrenheit - 32) / 1.8);
    }

    //Hàm quy đổi tốc độ gió từ km/h sang mph
    public static double convertKmhToMph(double kmh) {
        return kmh / 1.60934;
    }
}
