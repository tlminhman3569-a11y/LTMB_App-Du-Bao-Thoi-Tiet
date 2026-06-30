package com.example.weatherapp.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.weatherapp.data.FavoriteRepository;
import com.example.weatherapp.models.favorite.FavoriteCity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSyncManager {

    private final Context context;
    private final FavoriteRepository favoriteRepository;
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public FirebaseSyncManager(Context context) {
        this.context = context;
        // Tái sử dụng trọn vẹn code Database cục bộ của Thành viên 4
        this.favoriteRepository = new FavoriteRepository(context);
        this.mAuth = FirebaseAuth.getInstance();
        // Gọi đến nhánh gốc của Firebase Realtime Database
        this.mDatabase = FirebaseDatabase.getInstance("https://weatherapp-deb84-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
    }

    // Hàm gọi khi bấm nút Đám mây (Cloud Sync)
    public void syncData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Vui lòng đăng nhập trong Cài đặt để đồng bộ Cloud!", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Đang đồng bộ dữ liệu...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String userId = user.getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(userId).child("Favorites");

        // BƯỚC A: Kéo dữ liệu từ mây về (Restore)
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        for (DataSnapshot citySnap : snapshot.getChildren()) {
                            String cityName = citySnap.child("cityName").getValue(String.class);

                            // Lấy dưới dạng Object chung chung
                            Object latObj = citySnap.child("latitude").getValue();
                            Object lonObj = citySnap.child("longitude").getValue();

                            if (cityName != null && latObj != null && lonObj != null) {
                                // Biến thành String rồi mới ép sang Double
                                double lat = Double.parseDouble(String.valueOf(latObj));
                                double lon = Double.parseDouble(String.valueOf(lonObj));

                                favoriteRepository.addFavoriteCity(cityName, lat, lon);
                            }
                        }
                    }

                    // BƯỚC B: Đẩy toàn bộ dữ liệu SQLite hiện tại lên mây (Backup)
                    List<FavoriteCity> localCities = favoriteRepository.getAllFavoriteCities();
                    Map<String, Object> cloudData = new HashMap<>();

                    for (FavoriteCity city : localCities) {
                        String key = city.getCityName().replaceAll("[\\.#$\\[\\]]", "_");
                        Map<String, Object> cityData = new HashMap<>();
                        cityData.put("cityName", city.getCityName());
                        cityData.put("latitude", city.getLatitude());
                        cityData.put("longitude", city.getLongitude());
                        cloudData.put(key, cityData);
                    }

                    // Xử lý ghi dữ liệu...
                    if (cloudData.isEmpty()) {
                        userRef.removeValue().addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Đã đồng bộ (Dữ liệu trống)", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        userRef.setValue(cloudData).addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Đồng bộ Cloud 2 chiều thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Lỗi ghi dữ liệu lên Firebase", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Lỗi ngầm: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(context, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}