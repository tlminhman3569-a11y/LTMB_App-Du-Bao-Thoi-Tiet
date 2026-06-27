package com.example.weatherapp.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.weatherapp.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup rgTemperature, rgWindSpeed;
    private RadioButton rbCelsius, rbFahrenheit, rbKmh, rbMph;
    private SwitchMaterial switchDarkMode;
    private SharedPreferences sharedPreferences;

    public static final String SETTINGS_PREFS = "WeatherSettingsPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ nút quay lại gán kết thúc màn hình
        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Ánh xạ các thành phần chọn đơn vị
        rgTemperature = findViewById(R.id.rgTemperature);
        rgWindSpeed = findViewById(R.id.rgWindSpeed);
        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);
        rbKmh = findViewById(R.id.rbKmh);
        rbMph = findViewById(R.id.rbMph);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);

        // 1. Đọc dữ liệu cũ để hiển thị dấu tích (Check) vào đúng ô
        boolean isCelsius = sharedPreferences.getBoolean("is_celsius", true);
        if (isCelsius) {
            rbCelsius.setChecked(true);
        } else {
            rbFahrenheit.setChecked(true);
        }

        boolean isKmh = sharedPreferences.getBoolean("is_kmh", true);
        if (isKmh) {
            rbKmh.setChecked(true);
        } else {
            rbMph.setChecked(true);
        }

        switchDarkMode.setChecked(sharedPreferences.getBoolean("is_dark_mode", false));

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
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("is_dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
