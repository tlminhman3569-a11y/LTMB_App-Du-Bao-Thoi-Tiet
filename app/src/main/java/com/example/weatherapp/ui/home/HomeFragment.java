package com.example.weatherapp.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.api.HomeApiService;
import com.example.weatherapp.api.RetrofitClient;
import com.example.weatherapp.models.common.WeatherResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private android.content.SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "WeatherCachePrefs";
    private static final String KEY_WEATHER_JSON = "cached_weather_json";
    private static final String KEY_CACHE_TIMESTAMP = "cache_timestamp";
    private static final long CACHE_DURATION = 15 * 60 * 1000;

    private final String API_KEY = "ec300b0837672f3a17c36026f68a0f00";

    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvCityName, tvTemperature, tvWeatherDescription, tvHumidity, tvWindSpeed;
    private ImageView imgWeatherIcon;
    private View layoutBackground;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if ((fineLocationGranted != null && fineLocationGranted) ||
                        (coarseLocationGranted != null && coarseLocationGranted)) {
                    getDeviceLocation();
                } else {
                    Toast.makeText(getContext(), "Ứng dụng cần quyền vị trí để chạy!", Toast.LENGTH_LONG).show();
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_weather, container, false);

        // Ánh xạ View
        tvCityName           = view.findViewById(R.id.tvCityName);
        tvTemperature        = view.findViewById(R.id.tvTemperature);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        tvHumidity           = view.findViewById(R.id.tvHumidity);
        tvWindSpeed          = view.findViewById(R.id.tvWindSpeed);
        imgWeatherIcon       = view.findViewById(R.id.imgWeatherIcon);
        layoutBackground     = view.findViewById(R.id.layoutBackground);
        swipeRefreshLayout   = view.findViewById(R.id.swipeRefreshLayout);
        sharedPreferences    = requireActivity().getSharedPreferences(PREFS_NAME,
                android.content.Context.MODE_PRIVATE);

        // Khởi tạo công cụ định vị
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Vuốt để làm mới
        swipeRefreshLayout.setOnRefreshListener(this::checkLocationPermissions);

        // Kiểm tra cache trước khi gọi API
        checkWeatherCache();

        return view;
    }

    private void checkWeatherCache() {
        String cachedJson = sharedPreferences.getString(KEY_WEATHER_JSON, null);
        long cacheTime    = sharedPreferences.getLong(KEY_CACHE_TIMESTAMP, 0);
        long currentTime  = System.currentTimeMillis();

        if (cachedJson != null && (currentTime - cacheTime < CACHE_DURATION)) {
            Gson gson = new Gson();
            WeatherResponse cachedData = gson.fromJson(cachedJson, WeatherResponse.class);
            String savedCityName = sharedPreferences.getString("cached_city_name", "Không rõ địa điểm");
            tvCityName.setText(savedCityName);
            updateUI(cachedData);
        } else {
            checkLocationPermissions();
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getDeviceLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    sharedPreferences.edit()
                            .putFloat("last_lat", (float) location.getLatitude())
                            .putFloat("last_lon", (float) location.getLongitude())
                            .apply();

                    fetchCurrentWeather(location.getLatitude(), location.getLongitude());

                    new Thread(() -> {
                        android.location.Geocoder geocoder = new android.location.Geocoder(
                                requireContext(), java.util.Locale.getDefault());
                        try {
                            java.util.List<android.location.Address> addresses =
                                    geocoder.getFromLocation(location.getLatitude(),
                                            location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String cityName = addresses.get(0).getAdminArea();
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> tvCityName.setText(cityName));
                                }
                                sharedPreferences.edit()
                                        .putString("cached_city_name", cityName)
                                        .apply();
                            }
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }).start();

                } else {
                    Toast.makeText(getContext(), "Hãy bật GPS trên thiết bị!", Toast.LENGTH_SHORT).show();
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void fetchCurrentWeather(double lat, double lon) {
        HomeApiService apiService = RetrofitClient.getClient().create(HomeApiService.class);
        Call<WeatherResponse> call = apiService.getCurrentWeather(lat, lon, API_KEY, "metric", "vi");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherData = response.body();

                    Gson gson = new Gson();
                    String jsonToCache = gson.toJson(weatherData);
                    sharedPreferences.edit()
                            .putString(KEY_WEATHER_JSON, jsonToCache)
                            .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                            .apply();

                    updateUI(weatherData);
                } else {
                    Toast.makeText(getContext(), "Lỗi dữ liệu từ máy chủ API!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(getContext(), "Lỗi kết nối mạng: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(WeatherResponse data) {
        int tempInt = (int) Math.round(data.getMain().getTemp());
        tvTemperature.setText(tempInt + (isCelsius() ? "°C" : "°F"));
        tvHumidity.setText(data.getMain().getHumidity() + "%");
        tvWindSpeed.setText(data.getWind().getSpeed() + (isKmH() ? " km/h" : " mph"));

        if (data.getWeather() != null && !data.getWeather().isEmpty()) {
            String desc = data.getWeather().get(0).getDescription();
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
            }
            tvWeatherDescription.setText(desc);

            String iconCode = data.getWeather().get(0).getIcon();
            String iconUrl  = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            Glide.with(this)
                    .load(iconUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(imgWeatherIcon);

            updateDynamicBackground(iconCode);
        }
    }

    private void updateDynamicBackground(String iconCode) {
        if (iconCode == null) return;

        int bgRes = R.drawable.bg_sunny;
        if (iconCode.endsWith("n")) {
            bgRes = R.drawable.bg_night;
        } else {
            if (iconCode.startsWith("01")) {
                bgRes = R.drawable.bg_sunny;
            } else if (iconCode.startsWith("02") || iconCode.startsWith("03")) {
                bgRes = R.drawable.bg_cloudy;
            } else if (iconCode.startsWith("04")) {
                bgRes = R.drawable.bg_overcast;
            } else if (iconCode.startsWith("09") || iconCode.startsWith("10") ||
                    iconCode.startsWith("11")) {
                bgRes = R.drawable.bg_rainy;
            }
        }

        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setBackgroundDrawableResource(bgRes);
        }
        if (layoutBackground != null) {
            layoutBackground.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
    }

    private boolean isCelsius() { return true; }
    private boolean isKmH()     { return true; }
}