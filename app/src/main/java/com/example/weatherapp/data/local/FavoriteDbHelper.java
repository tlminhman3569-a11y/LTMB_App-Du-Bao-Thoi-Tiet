package com.example.weatherapp.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.weatherapp.models.favorite.FavoriteCity;

import java.util.ArrayList;
import java.util.List;

public class FavoriteDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weather_app.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_FAVORITE_CITIES = "favorite_cities";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CITY_NAME = "city_name";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    private static final String CREATE_TABLE_FAVORITE_CITIES =
            "CREATE TABLE " + TABLE_FAVORITE_CITIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                    COLUMN_LATITUDE + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE + " REAL NOT NULL, " +
                    "UNIQUE(" + COLUMN_CITY_NAME + ", " + COLUMN_LATITUDE + ", " + COLUMN_LONGITUDE + ")" +
                    ");";

    public FavoriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FAVORITE_CITIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE_CITIES);
        onCreate(db);
    }

    public long addFavoriteCity(FavoriteCity city) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_NAME, city.getCityName());
        values.put(COLUMN_LATITUDE, city.getLatitude());
        values.put(COLUMN_LONGITUDE, city.getLongitude());

        return db.insertWithOnConflict(
                TABLE_FAVORITE_CITIES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public List<FavoriteCity> getAllFavoriteCities() {
        List<FavoriteCity> favoriteCities = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_FAVORITE_CITIES,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String cityName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY_NAME));
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE));

                    favoriteCities.add(new FavoriteCity(id, cityName, latitude, longitude));
                }
            } finally {
                cursor.close();
            }
        }

        return favoriteCities;
    }

    public int deleteFavoriteCity(int cityId) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_FAVORITE_CITIES,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(cityId)}
        );
    }

    public boolean isFavoriteCity(String cityName, double latitude, double longitude) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_FAVORITE_CITIES,
                new String[]{COLUMN_ID},
                COLUMN_CITY_NAME + " = ? AND " + COLUMN_LATITUDE + " = ? AND " + COLUMN_LONGITUDE + " = ?",
                new String[]{cityName, String.valueOf(latitude), String.valueOf(longitude)},
                null,
                null,
                null
        );

        boolean exists = false;
        if (cursor != null) {
            try {
                exists = cursor.moveToFirst();
            } finally {
                cursor.close();
            }
        }

        return exists;
    }
}