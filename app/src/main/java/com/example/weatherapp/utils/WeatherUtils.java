package com.example.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
public class WeatherUtils {
    private static final String SETTINGS_PREFS = "WeatherSettingsPrefs";

    // 1. Kiểm tra xem người dùng đang chọn độ C hay không
    public static boolean isCelsius(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean("is_celsius", true);
    }

    // 2. Kiểm tra xem người dùng đang chọn km/h hay không
    public static boolean isKmH(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean("is_kmh", true);
    }

    // 3. Hàm quy đổi từ độ C sang độ F (Làm tròn thành số nguyên)
    public static int convertCelsiusToFahrenheit(double celsius) {
        return (int) Math.round(celsius * 1.8 + 32);
    }

    // 4. Hàm quy đổi từ độ F ngược về độ C (Dùng cho phần so sánh logic thông báo)
    public static int convertFahrenheitToCelsius(double fahrenheit) {
        return (int) Math.round((fahrenheit - 32) / 1.8);
    }

    // 5. Hàm quy đổi tốc độ gió từ km/h sang mph
    public static double convertKmhToMph(double kmh) {
        return kmh / 1.60934;
    }
}
