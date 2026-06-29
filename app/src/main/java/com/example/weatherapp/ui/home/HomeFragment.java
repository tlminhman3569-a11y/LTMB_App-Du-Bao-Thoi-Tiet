package com.example.weatherapp.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.weatherapp.utils.WeatherUtils;
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
    private static final long CACHE_DURATION = 15 * 60 * 1000; // 15 phút tính bằng mili-giây

    // API Open Weather (https://home.openweathermap.org/api_keys)
    private final String API_KEY = "ec300b0837672f3a17c36026f68a0f00";

    private FusedLocationProviderClient fusedLocationClient;

    // Các thành phần giao diện UI
    private TextView tvCityName, tvTemperature, tvWeatherDescription, tvHumidity, tvWindSpeed;
    private ImageView imgWeatherIcon;
    private View layoutBackground;

    // Bộ phóng kích hoạt hộp thoại xin quyền của Android
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if ((fineLocationGranted != null && fineLocationGranted) || (coarseLocationGranted != null && coarseLocationGranted)) {
                    // Người dùng đã cho phép quyền chính xác
                    getDeviceLocation();
                } else {
                    // Người dùng từ chối cấp quyền
                    Toast.makeText(getContext(), "Ứng dụng cần quyền vị trí để chạy!", Toast.LENGTH_LONG).show();
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_weather, container, false);

        // 1. Ánh xạ các View từ file XML sang code Java
        tvCityName = view.findViewById(R.id.tvCityName);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed);
        imgWeatherIcon = view.findViewById(R.id.imgWeatherIcon);
        layoutBackground = view.findViewById(R.id.layoutBackground);

        // Ánh xạ swipeRefreshLayout và sharedPreferences
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        // 2. Khởi tạo công cụ định vị của Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 3. Bắt sự kiện vuốt để làm mới
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Khi vuốt, bỏ qua Cache và ép gọi API mới
            checkLocationPermissions();
        });

        // Không gọi API luôn, kiểm tra cache trước
        checkWeatherCache();

        return view;
    }

    // Hàm kiểm tra cache
    private void checkWeatherCache() {
        String cachedJson = sharedPreferences.getString(KEY_WEATHER_JSON, null);
        long cacheTime = sharedPreferences.getLong(KEY_CACHE_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        if (cachedJson != null && (currentTime - cacheTime < CACHE_DURATION)) {
            // Nếu cache còn hạn (chưa quá 15p), dùng Gson biến chuỗi về lại Object và hiển thị ngay
            Gson gson = new Gson();
            WeatherResponse cachedData = gson.fromJson(cachedJson, WeatherResponse.class);

            //  Lấy tên thành phố từ Cache ra gán
            String savedCityName = sharedPreferences.getString("cached_city_name", "Không rõ địa điểm");
            tvCityName.setText(savedCityName);

            updateUI(cachedData);
        } else {
            // Hết hạn hoặc chưa có dữ liệu -> Xin quyền & gọi mạng
            checkLocationPermissions();
        }
    }

    // Hàm kiểm tra xem ứng dụng đã được cấp quyền vị trí chưa
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền, tiến hành lấy vị trí
            getDeviceLocation();
        } else {
            // Chưa có quyền, hiển thị hộp thoại xin quyền
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    // Hàm trực tiếp lấy tọa độ GPS của thiết bị
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
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false); // Tắt loading nếu lỗi
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        }
    }

    // Hàm kết nối mạng gọi API thời tiết thực
    private void fetchCurrentWeather(double lat, double lon) {
        // Tạo đường ống kết nối từ máy bơm chung RetrofitClient
        HomeApiService apiService = RetrofitClient.getClient().create(HomeApiService.class);

        // Cấu hình các tham số truyền lên: tọa độ, key, hệ metric (độ C), ngôn ngữ tiếng Việt
        Call<WeatherResponse> call = apiService.getCurrentWeather(lat, lon, API_KEY, "metric", "vi");

        // Thực hiện xếp hàng gọi ngầm dưới nền (Asynchronous Call) để không gây đơ màn hình app
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                // TẮT HIỆU ỨNG LOADING CỦA SWIPE REFRESH
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherData = response.body();

                    // LƯU DỮ LIỆU MỚI VÀO CACHE TRƯỚC KHI HIỂN THỊ
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
                // Tắt vòng xoay nếu rớt mạng
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                // Lỗi mất kết nối mạng, rớt mạng hoặc sai link gốc
                Toast.makeText(getContext(), "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm đổ dữ liệu vào TextView/ImgView
    private void updateUI(WeatherResponse data) {
//        // Đổ tên thành phố
//        tvCityName.setText(data.getName());

        // Đổ nhiệt độ (Làm tròn thành số nguyên và thêm đơn vị °C dựa theo cài đặt)
        int tempInt = (int) Math.round(data.getMain().getTemp());
        //tvTemperature.setText(tempInt + (isCelsius() ? "°C" : "°F"));

//        if (isCelsius()) {
//            // Nếu là độ C -> Hiển thị bình thường
//            tvTemperature.setText(tempInt + "°C");
//        } else {
//            // Nếu là độ F -> Đổi công thức: F = C * 1.8 + 32
//            int tempInFahrenheit = (int) Math.round(tempInt * 1.8 + 32);
//            tvTemperature.setText(tempInFahrenheit + "°F");
//        }
//
//        // Đổ độ ẩm
//        tvHumidity.setText(data.getMain().getHumidity() + "%");
//
//        // Đổ tốc độ gió
//        //tvWindSpeed.setText(data.getWind().getSpeed() + (isKmH() ? " km/h" : " mph"));
//
//        double windSpeed = data.getWind().getSpeed();
//        if (isKmH()) {
//            // Nếu chọn km/h -> Hiển thị đơn vị km/h
//            tvWindSpeed.setText(String.format("%.1f Km/h", windSpeed));
//        } else {
//            // Nếu chọn mph -> Quy đổi đơn vị: mph = km/h / 1.60934
//            double speedInMph = windSpeed / 1.60934;
//            tvWindSpeed.setText(String.format("%.1f Mph", speedInMph));
//        }

        //Refactor lại code: Gọi WeatherUtils xử lý điều kiện chọn hiển thị độ C hoặc km/h hay không
        if (WeatherUtils.isCelsius(getContext())) {
            tvTemperature.setText(tempInt + "°C");
        } else {
            int tempInFahrenheit = WeatherUtils.convertCelsiusToFahrenheit(tempInt);
            tvTemperature.setText(tempInFahrenheit + "°F");
        }

        // Đổ tốc độ gió (Gọi qua lớp tiện ích)
        double windSpeed = data.getWind().getSpeed();
        if (WeatherUtils.isKmH(getContext())) {
            tvWindSpeed.setText(String.format("%.1f km/h", windSpeed));
        } else {
            double speedInMph = WeatherUtils.convertKmhToMph(windSpeed);
            tvWindSpeed.setText(String.format("%.1f mph", speedInMph));
        }

        // Kiểm tra và đổ phần mô tả thời tiết + tải icon bằng Glide
        if (data.getWeather() != null && !data.getWeather().isEmpty()) {
            // Viết hoa chữ cái đầu của chuỗi mô tả thời tiết cho đẹp
            String desc = data.getWeather().get(0).getDescription();
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
            }
            tvWeatherDescription.setText(desc);

            // Tải icon thời tiết động từ link của OpenWeatherMap thông qua Glide
            String iconCode = data.getWeather().get(0).getIcon();
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

            Glide.with(this)
                    .load(iconUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image) // Ảnh hiển thị tạm lúc đang tải
                    .into(imgWeatherIcon);

            // MỚI THÊM: Gọi hàm đổi màu nền
            updateDynamicBackground(iconCode);
        }
    }


    // Hàm xử lý logic đổi ảnh nền động theo thời tiết
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

    //UC-05: Chuyển đổi đơn vị đo độ ẩm độ C:
//    private boolean isCelsius() {
//        if (getActivity() == null) return true;
//        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("WeatherSettingsPrefs", android.content.Context.MODE_PRIVATE);
//        return prefs.getBoolean("is_celsius", true);
//    }
//
//    private boolean isKmH() {
//        if (getActivity() == null) return true;
//        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("WeatherSettingsPrefs", android.content.Context.MODE_PRIVATE);
//        return prefs.getBoolean("is_kmh", true);
//    }
    @Override
    public void onResume() {
        super.onResume();
        checkWeatherCache();
    }
}