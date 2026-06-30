package com.example.weatherapp.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.weatherapp.R;
import com.example.weatherapp.utils.WeatherUtils;
import com.example.weatherapp.worker.WeatherWorker;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup rgTemperature, rgWindSpeed;
    private RadioButton rbCelsius, rbFahrenheit, rbKmh, rbMph;
    private com.google.android.material.materialswitch.MaterialSwitch switchDarkMode, switchNotification;
    private SharedPreferences sharedPreferences;

    public static final String SETTINGS_PREFS = "WeatherSettingsPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ nút quay lại gán kết thúc màn hình
        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Ánh xạ các thành phần giao diện
        rgTemperature = findViewById(R.id.rgTemperature);
        rgWindSpeed = findViewById(R.id.rgWindSpeed);
        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);
        rbKmh = findViewById(R.id.rbKmh);
        rbMph = findViewById(R.id.rbMph);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotification = findViewById(R.id.switchNotification);

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);


        // 1. Đọc dữ liệu cũ để hiển thị đúng trạng thái giao diện khi vừa vào màn hình
        setupCurrentSettingsView();

        // 2. Lắng nghe sự kiện thay đổi của Nhóm Nhiệt độ
        rgTemperature.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCelsius) {
                sharedPreferences.edit().putBoolean("is_celsius", true).apply();
            } else if (checkedId == R.id.rbFahrenheit) {
                sharedPreferences.edit().putBoolean("is_celsius", false).apply();
            }
        });

        // 3. Lắng nghe sự kiện thay đổi của Nhóm Tốc độ gió
        rgWindSpeed.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbKmh) {
                sharedPreferences.edit().putBoolean("is_kmh", true).apply();
            } else if (checkedId == R.id.rbMph) {
                sharedPreferences.edit().putBoolean("is_kmh", false).apply();
            }
        });

        // 4. Lắng nghe chế độ tối
        switchDarkMode.setOnClickListener(v -> {
            boolean isChecked = switchDarkMode.isChecked();
            sharedPreferences.edit().putBoolean("is_dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // 5. LẮNG NGHE SỰ KIỆN BẬT/TẮT THÔNG BÁO CHẠY NGẦM
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lưu trạng thái lựa chọn của người dùng vào máy
            sharedPreferences.edit().putBoolean("is_notification_enabled", isChecked).apply();

            if (isChecked) {
                // 1. TÍNH TOÁN THỜI GIAN CHỜ ĐỂ BẮN THÔNG BÁO VÀO ĐÚNG 7 GIỜ SÁNG
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                java.util.Calendar dueDate = java.util.Calendar.getInstance();

                // Thiết lập khung giờ vàng bạn muốn: 7 giờ 00 phút 0 giây sáng
                //Có thể chỉnh sửa khung giờ để Test
                dueDate.set(java.util.Calendar.HOUR_OF_DAY, 7);
                dueDate.set(java.util.Calendar.MINUTE, 0);
                dueDate.set(java.util.Calendar.SECOND, 0);

                // Nếu thời điểm hiện tại đã qua 7 giờ sáng rồi, thì phải hẹn vào 7 giờ sáng NGÀY MAI
                if (dueDate.before(currentDate)) {
                    dueDate.add(java.util.Calendar.HOUR_OF_DAY, 24);
                }

                // Tính số mili-giây chênh lệch cần phải chờ (Delay)
                long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

                androidx.work.Constraints constraints = new androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED) // Chỉ chạy khi có mạng
                        .build();
                // 2. THIẾT LẬP LỊCH CHẠY ĐỊNH KỲ MỖI 24 TIẾNG (MỖI NGÀY 1 LẦN)
                PeriodicWorkRequest weatherWorkRequest =
                        new PeriodicWorkRequest.Builder(WeatherWorker.class, 24, TimeUnit.HOURS)
                                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS) // Ép chờ đến đúng 7:00 sáng mới chạy
                                .setConstraints(constraints)
                                .build();

                // Đăng ký vào hệ thống
                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                        "WeatherPeriodicCheck",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        weatherWorkRequest
                );
                //dùng để test: Bật là thông báo ngay
//                androidx.work.OneTimeWorkRequest instantRequest =
//                        new androidx.work.OneTimeWorkRequest.Builder(WeatherWorker.class)
//                                .build();
//                WorkManager.getInstance(this).enqueue(instantRequest);

                Toast.makeText(this, "Đã hẹn lịch thông báo vào 7:00 sáng hàng ngày!", Toast.LENGTH_SHORT).show();
            } else {
                // Người dùng TẮT -> Hủy toàn bộ lịch chạy
                WorkManager.getInstance(this).cancelUniqueWork("WeatherPeriodicCheck");
                Toast.makeText(this, "Đã tắt thông báo thời tiết!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupCurrentSettingsView() {
        // Đơn vị C hay F
        if (WeatherUtils.isCelsius(this)) {
            rbCelsius.setChecked(true);
        } else {
            rbFahrenheit.setChecked(true);
        }

        // Đơn vị Tốc độ gió: km/h hay mph
        if (WeatherUtils.isKmH(this)) {
            rbKmh.setChecked(true);
        } else {
            rbMph.setChecked(true);
        }

        // Kiểm tra trực tiếp xem hệ thống đang thực sự ở chế độ tối hay không
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isNowDark = (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);

        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            // Nếu chưa cấu hình gì, kiểm tra theo cấu hình máy điện thoại
            int sysMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            isNowDark = (sysMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);
        }

        // Đọc từ bộ nhớ đã lưu, nếu không có thì lấy theo trạng thái đang hiển thị thực tế
        boolean isDarkModeSaved = sharedPreferences.getBoolean("is_dark_mode", isNowDark);

        // Ép nút Switch hiển thị đúng 100% theo trạng thái thực tế
        switchDarkMode.setChecked(isDarkModeSaved);

        // Xử lý nút thông báo
        switchNotification.setChecked(sharedPreferences.getBoolean("is_notification_enabled", false));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
