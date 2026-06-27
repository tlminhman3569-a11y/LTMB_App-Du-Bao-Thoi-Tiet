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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

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

        // 2. Khởi tạo công cụ định vị của Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 3. Kiểm tra quyền và kích hoạt chuỗi xử lý mạng
        checkLocationPermissions();

        return view;
    }

    // Hàm kiểm tra xem ứng dụng đã được cấp quyền vị trí chưa
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền, tiến hành lấy vị trí luôn
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
                    // 1. Lấy tọa độ thành công -> Bắn tọa độ sang hàm gọi API mạng
                    fetchCurrentWeather(location.getLatitude(), location.getLongitude());

                    // 2. Dùng Geocoder để lấy tên địa phương chuẩn xác NGAY TẠI ĐÂY
                    android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());
                    try {
                        java.util.List<android.location.Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String cityName = addresses.get(0).getAdminArea(); // Lấy tên Tỉnh/Thành phố
                            tvCityName.setText(cityName); // Gắn tên lên giao diện
                        }
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getContext(), "Hãy bật GPS trên thiết bị!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
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
                if (response.isSuccessful() && response.body() != null) {
                    // Đã lấy được dữ liệu thành công từ Internet về máy!
                    WeatherResponse weatherData = response.body();
                    updateUI(weatherData);
                } else {
                    Toast.makeText(getContext(), "Lỗi dữ liệu từ máy chủ API!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
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
        tvTemperature.setText(tempInt + (isCelsius() ? "°C" : "°F"));

        // Đổ độ ẩm
        tvHumidity.setText(data.getMain().getHumidity() + "%");

        // Đổ tốc độ gió
        tvWindSpeed.setText(data.getWind().getSpeed() + (isKmH() ? " km/h" : " mph"));

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
        if (iconCode == null || layoutBackground == null) return;

        if (iconCode.endsWith("n")) {
            // Ảnh ban đêm
            layoutBackground.setBackgroundResource(R.drawable.bg_night);
        } else {
            if (iconCode.startsWith("01")) {
                // Ảnh trời nắng trong xanh
                layoutBackground.setBackgroundResource(R.drawable.bg_sunny);
            } else if (iconCode.startsWith("02") || iconCode.startsWith("03")) {
                // Ảnh trời mây nhẹ, mây trắng bồng bềnh
                layoutBackground.setBackgroundResource(R.drawable.bg_cloudy);
            } else if (iconCode.startsWith("04")) {
                //  Ảnh bầu trời mây đen u ám, xám xịt
                layoutBackground.setBackgroundResource(R.drawable.bg_overcast);
            } else if (iconCode.startsWith("09") || iconCode.startsWith("10") || iconCode.startsWith("11")) {
                // Ảnh trời mưa bão
                layoutBackground.setBackgroundResource(R.drawable.bg_rainy);
            } else {
                // Mặc định
                layoutBackground.setBackgroundResource(R.drawable.bg_sunny);
            }
        }
    }

    // Phần giả lập trước cho UC 5 (Setting)
    private boolean isCelsius() { return true; }
    private boolean isKmH() { return true; }
}