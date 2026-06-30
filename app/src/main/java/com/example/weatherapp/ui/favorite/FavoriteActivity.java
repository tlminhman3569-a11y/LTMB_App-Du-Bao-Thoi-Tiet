package com.example.weatherapp.ui.favorite;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.data.FavoriteRepository;
import com.example.weatherapp.models.favorite.FavoriteCity;
import com.example.weatherapp.api.HomeApiService;
import com.example.weatherapp.api.RetrofitClient;
import com.example.weatherapp.api.Constants;
import com.example.weatherapp.models.common.WeatherResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity {
    private FavoriteRepository favoriteRepository;
    private FavoriteAdapter favoriteAdapter;

    private RecyclerView rvFavoriteCities;
    private TextView tvEmptyFavorites;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        favoriteRepository = new FavoriteRepository(this);

        initViews();
        setupRecyclerView();
        setupEvents();
        loadFavoriteCities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteCities();
    }

    private void initViews() {
        rvFavoriteCities = findViewById(R.id.rvFavoriteCities);
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        favoriteAdapter = new FavoriteAdapter();
        favoriteAdapter.setOnFavoriteActionListener(this::deleteFavoriteCity);
        favoriteAdapter.setOnItemClickListener(city -> {
            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra("city_name", city.getCityName());
            resultIntent.putExtra("city_lat", city.getLatitude());
            resultIntent.putExtra("city_lon", city.getLongitude());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        rvFavoriteCities.setLayoutManager(new LinearLayoutManager(this));
        rvFavoriteCities.setAdapter(favoriteAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (position == RecyclerView.NO_POSITION) {
                    return;
                }

                deleteFavoriteCity(position);
            }
        });

        itemTouchHelper.attachToRecyclerView(rvFavoriteCities);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadFavoriteCities() {
        List<FavoriteCity> favoriteCities = favoriteRepository.getAllFavoriteCities();
        favoriteAdapter.setData(favoriteCities);
        updateEmptyState(favoriteCities == null || favoriteCities.isEmpty());
        fetchWeatherForFavorites(favoriteCities);
    }

    private void fetchWeatherForFavorites(List<FavoriteCity> cities) {
        if (cities == null || cities.isEmpty()) return;
        HomeApiService apiService = RetrofitClient.getClient().create(HomeApiService.class);
        for (int i = 0; i < cities.size(); i++) {
            final int index = i;
            FavoriteCity city = cities.get(i);
            apiService.getCurrentWeather(city.getLatitude(), city.getLongitude(), Constants.API_KEY, "metric", "vi")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weather = response.body();
                            city.setTemperature(weather.getMain().getTemp());
                            if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
                                city.setDescription(weather.getWeather().get(0).getDescription());
                                city.setIconCode(weather.getWeather().get(0).getIcon());
                            }
                            if (weather.getSys() != null) {
                                city.setCountry(weather.getSys().getCountry());
                            }
                            favoriteAdapter.notifyItemChanged(index);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        android.util.Log.e("FavoriteActivity", "Lỗi tải thời tiết cho: " + city.getCityName(), t);
                    }
                });
        }
    }

    private void deleteFavoriteCity(int position) {
        FavoriteCity city = favoriteAdapter.getItem(position);

        if (city == null) {
            favoriteAdapter.notifyItemChanged(position);
            return;
        }

        boolean deleted = favoriteRepository.deleteFavoriteCity(city);

        if (deleted) {
            favoriteAdapter.removeItem(position);
            updateEmptyState(favoriteAdapter.getItemCount() == 0);
            Toast.makeText(this, "Đã xóa " + city.getCityName(), Toast.LENGTH_SHORT).show();
        } else {
            favoriteAdapter.notifyItemChanged(position);
            Toast.makeText(this, "Không thể xóa địa điểm", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmptyFavorites.setVisibility(View.VISIBLE);
            rvFavoriteCities.setVisibility(View.GONE);
        } else {
            tvEmptyFavorites.setVisibility(View.GONE);
            rvFavoriteCities.setVisibility(View.VISIBLE);
        }
    }
}
