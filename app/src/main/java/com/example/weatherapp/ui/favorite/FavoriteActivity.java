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

import java.util.List;

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
