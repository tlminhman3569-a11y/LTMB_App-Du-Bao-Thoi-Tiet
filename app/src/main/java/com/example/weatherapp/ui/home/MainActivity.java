package com.example.weatherapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.weatherapp.R;
import com.example.weatherapp.ui.forecast.ForecastFragment;

public class MainActivity extends AppCompatActivity {

    private ImageView btnSearch, btnFavorite, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Việc của TV1 Nạp mảnh ghép HomeFragment vào khung trên
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_current_weather, new HomeFragment())
                    .replace(R.id.container_forecast, new ForecastFragment())
                    .commit();
        }

        // Sự kiện chuyển màn hình của TV 3,4,5
        btnSearch = findViewById(R.id.btnSearch);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnSettings = findViewById(R.id.btnSettings);

        // Chỗ cho TV3 ghép code SearchActivity
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "[TV3] Sẽ chuyển sang SearchActivity", Toast.LENGTH_SHORT).show();
            // Bỏ comment 2 dòng dưới khi TV3 làm xong
            // Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            // startActivity(intent);
        });

        // Chỗ cho TV4 ghép code FavoriteActivity
        btnFavorite.setOnClickListener(v -> {
            Toast.makeText(this, "[TV4] Sẽ chuyển sang FavoriteActivity", Toast.LENGTH_SHORT).show();
            // Bỏ comment 2 dòng dưới khi TV4 làm xong
            // Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            // startActivity(intent);
        });

        // Chỗ cho TV5 ghép code SettingsActivity
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "[TV5] Sẽ chuyển sang SettingsActivity", Toast.LENGTH_SHORT).show();
            // Bỏ comment 2 dòng dưới khi TV5 làm xong
            // Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            // startActivity(intent);
        });
    }
}