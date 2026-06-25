package com.example.weatherapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.weatherapp.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện fragment_current_weather vào đây
        View view = inflater.inflate(R.layout.fragment_current_weather, container, false);

        // (Các đợt push sau sẽ ánh xạ TextView/ImageView và code logic gọi API tại đây)

        return view;
    }

    // Phần giả lập trước cho UC 5 (Setting)
    private boolean isCelsius() {
        return true; // Mặc định luôn trả về true (Độ C) để chạy test giao diện
    }

    private boolean isKmH() {
        return true; // Mặc định luôn trả về true (km/h)
    }
}