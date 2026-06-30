package com.example.weatherapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.weatherapp.R;
import com.example.weatherapp.ui.forecast.ForecastFragment;
import com.example.weatherapp.ui.search.SearchActivity;
import com.example.weatherapp.ui.favorite.FavoriteActivity;
import com.example.weatherapp.ui.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView btnSearch, btnFavorite, btnSettings;

    // Bộ phóng nhận lại kết quả từ SearchActivity khi người dùng chọn 1 thành phố
    private final ActivityResultLauncher<Intent> searchLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String cityName = data.getStringExtra("city_name");
                    double lat = data.getDoubleExtra("city_lat", 0);
                    double lon = data.getDoubleExtra("city_lon", 0);

                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentById(R.id.container_current_weather);
                    if (fragment instanceof HomeFragment) {
                        ((HomeFragment) fragment).loadCityWeather(lat, lon, cityName);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Nap HomeFragment vao khung tren
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

        // Mở SearchActivity và chờ nhận lại thành phố đã chọn
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            searchLauncher.launch(intent);
        });

        // Chỗ cho TV4 ghép code FavoriteActivity
        btnFavorite.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });

        // Chỗ cho TV5 ghép code SettingsActivity
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "[TV5] Sẽ chuyển sang SettingsActivity", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}