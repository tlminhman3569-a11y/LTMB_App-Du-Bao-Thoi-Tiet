package com.example.weatherapp.utils;

public class AppConfig {
    //Cấu hình API:
    public static final String API_KEY = "021b9aa15fd0ca5d671eca611de75ec2";
    public static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String DEFAULT_LANG = "vi";

    //Toạ độ mặc định, nếu GPS chưa xác định được vị trí thì lấy Hà Nội làm TP mặc định
    public static final float DEFAULT_LAT = 10.7626f;
    public static final float DEFAULT_LON = 106.6601f;

    //Khung giờ gửi thông báo thời tiết đến người dùng
    public static final int NOTIFICATION_HOUR = 7;
    public static final int NOTIFICATION_MINUTE = 0;
    public static final int NOTIFICATION_SECOND = 0;

    public static final String WORK_NAME_PERIODIC = "WeatherPeriodicCheck";
    public static final String PREFS_SETTINGS = "WeatherSettingsPrefs";
}
