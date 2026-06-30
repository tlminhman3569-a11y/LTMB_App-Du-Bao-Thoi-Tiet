package com.example.weatherapp.data;

import android.content.Context;

import com.example.weatherapp.data.local.FavoriteDbHelper;
import com.example.weatherapp.models.favorite.FavoriteCity;

import java.util.List;

public class FavoriteRepository {
    private final FavoriteDbHelper favoriteDbHelper;

    public FavoriteRepository(Context context) {
        favoriteDbHelper = new FavoriteDbHelper(context.getApplicationContext());
    }

    public boolean addFavoriteCity(String cityName, double latitude, double longitude) {
        FavoriteCity city = new FavoriteCity(cityName, latitude, longitude);
        long result = favoriteDbHelper.addFavoriteCity(city);

        return result != -1;
    }

    public List<FavoriteCity> getAllFavoriteCities() {
        return favoriteDbHelper.getAllFavoriteCities();
    }

    public boolean deleteFavoriteCity(FavoriteCity city) {
        if (city == null) {
            return false;
        }

        int deletedRows = favoriteDbHelper.deleteFavoriteCity(city.getId());
        return deletedRows > 0;
    }

    public boolean isFavoriteCity(String cityName, double latitude, double longitude) {
        return favoriteDbHelper.isFavoriteCity(cityName, latitude, longitude);
    }
}